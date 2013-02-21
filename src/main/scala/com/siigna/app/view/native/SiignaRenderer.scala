package com.siigna.app.view.native

import com.siigna.app.view.{Graphics, View, Renderer}
import com.siigna.app.model.Drawing
import java.awt.image.BufferedImage
import java.awt.{Color, RenderingHints, Graphics2D}
import com.siigna.app.Siigna
import com.siigna.util.Implicits._
import com.siigna.app.model.shape.{LineShape, PolylineShape}
import com.siigna.util.geom.{Vector2D, TransformationMatrix, Rectangle2D}
import concurrent.{Promise, future, promise}
import scala.concurrent.ExecutionContext.Implicits.global
import com.siigna.util.Log

/**
 * Siignas own implementation of the [[com.siigna.app.view.Renderer]] which draws a chess-checkered background
 * and the shapes using caching tecniques. The SiignaRenderer uses the colors and attributes defined in the
 * [[com.siigna.app.SiignaAttributes]] (accessible via the [[com.siigna.app.Siigna]] object).
 *
 * <p>
 *   This renderer uses tiles to cache the content of the current view in images. Instead of painting the view
 *   at each paint-loop we simply paint the cached images, which drastically improves performance. Unless the drawing
 *   can be contained in one tile (the user has zoomed out to view the entire drawing) we render the center tile of
 *   the view synchronously and queing the other tiles up asynchronously.
 * </p>
 *
 * <p>
 *   The tiles are arranged so the center tile covers the entire view at the time of rendering. If the user pans the
 *   view one or more of the other tiles will be rendered to cover the "gap" left by the pan. If the user pans more
 *   than the width of the view / 2 - that is, the center-point of the view is moved to another tile - we "move" the
 *   center to a new tile. Thus, the center can "move" and maintain a constant of 9 tiles to render the entire view.
 * </p>
 * {{{
 *
 *   +----+----+----+
 *   | NW | N  | NE |
 *   +----+----+----+
 *   | W  | C  | E  |
 *   +----+----+----+
 *   | SW | S  | SE |
 *   +----+----+----+
 *
 * }}}
 *
 */
object SiignaRenderer extends Renderer {

  // Add listeners
  Drawing.addActionListener((_, _) => if (View.renderer == this) clearTiles())
  View.addPanListener(v =>            if (View.renderer == this) onPan(v))
  View.addResizeListener((screen) =>  if (View.renderer == this) onResize())
  View.addZoomListener((zoom) =>      if (View.renderer == this) clearTiles() )

  // A background image that can be re-used to draw as background on the canvas.
  private var cachedBackground : BufferedImage = renderBackground(View.screen)

  // The tiles drawn as 3 * 3 squares over the model.
  private lazy val cachedTiles = Array.fill[Option[BufferedImage]](9)(None)

  // Constants for the different tiles and their directions around a given center
  private val C  = 4; private val vC  = Vector2D( 0, 0)
  private val E  = 5; private val vE  = Vector2D( 1, 0)
  private val SE = 8; private val vSE = Vector2D( 1, 1)
  private val S  = 7; private val vS  = Vector2D( 0, 1)
  private val SW = 6; private val vSW = Vector2D(-1, 1)
  private val W  = 3; private val vW  = Vector2D(-1, 0)
  private val NW = 0; private val vNW = Vector2D(-1,-1)
  private val N  = 1; private val vN  = Vector2D( 0,-1)
  private val NE = 2; private val vNE = Vector2D( 1,-1)

  // A boolean value to indicate that the view is zoomed out enough to only need one single tile
  private var isSingleTile = true

  // The distance to the top left corner of the image to render, from the top left corner of the screen
  // Two scenarios: Model < Screen   - The model should be placed at the top left corner of the drawing
  //                Model >= Screen  - The model should be placed at the top left corner of the screen
  private var renderedDelta : Vector2D = Vector2D(0, 0)

  // The pan at the time of rendering
  private var renderedPan : Vector2D = Vector2D(0, 0)

  // The running rendering promise (if any) used to retrieve rendered tiles or cancel the rendering
  // if a new operation occurs
  private var renderedPromise : Option[Promise[Array[Option[BufferedImage]]]] = None

  // The tile deltas for the two axis
  private var tileDeltaX = 0; private var tileDeltaY = 0

  {
    import com.siigna.app.model.action._
    val shapes =
      {for(s <- 0 until 100) yield LineShape(0, s * s, s * s, 0)} ++
        {for(s <- 0 until 100) yield LineShape(100 * 100, s * s, s * s, 100 * 100)}
    Create(shapes)
  }

  /**
   * Executed when a pan operation have been done
   * @param pan  The new pan vector
   */
  private def onPan(pan : Vector2D) {
    // Set the new delta
    renderedDelta = if (isSingleTile) Drawing.boundary.topLeft.transform(View.drawingTransformation)
                    else View.pan - renderedPan

    // Re-arrange the tiles for a new center, if necessary
    if (!isSingleTile) {
      // The panning of the screen, relative to the center
      val pan = View.center - renderedDelta - tileDelta

      // Is the center tile still in focus?
      if (!View.screen.contains(pan)) {

        // Set the new center
        if (pan.x > View.width) { // ----> (Right)
          tileDeltaX += 1
          cachedTiles(NW) = cachedTiles(N)
          cachedTiles(W)  = cachedTiles(C)
          cachedTiles(SW) = cachedTiles(S)
          cachedTiles(N)  = cachedTiles(NE)
          cachedTiles(C)  = cachedTiles(E)
          cachedTiles(S)  = cachedTiles(SE)
          cachedTiles(NE) = None
          cachedTiles(E)  = None
          cachedTiles(SE) = None
        }
        if (pan.x < View.width) { // <---- (Left)
          tileDeltaX -= 1
          cachedTiles(NE) = cachedTiles(N)
          cachedTiles(E)  = cachedTiles(C)
          cachedTiles(SE) = cachedTiles(S)
          cachedTiles(N)  = cachedTiles(NW)
          cachedTiles(C)  = cachedTiles(W)
          cachedTiles(S)  = cachedTiles(SW)
          cachedTiles(NW) = None
          cachedTiles(W)  = None
          cachedTiles(SW) = None
        }
        if (pan.y > View.height) { // Down
          tileDeltaY += 1
          cachedTiles(NW) = cachedTiles(W)
          cachedTiles(N)  = cachedTiles(C)
          cachedTiles(NE) = cachedTiles(E)
          cachedTiles(W)  = cachedTiles(SW)
          cachedTiles(C)  = cachedTiles(S)
          cachedTiles(E)  = cachedTiles(SE)
          cachedTiles(SW) = None
          cachedTiles(S)  = None
          cachedTiles(SE) = None
        }
        if (pan.y < 0) { // Up
          tileDeltaY -= 1
          cachedTiles(SW) = cachedTiles(W)
          cachedTiles(S)  = cachedTiles(C)
          cachedTiles(SE) = cachedTiles(E)
          cachedTiles(W)  = cachedTiles(NW)
          cachedTiles(C)  = cachedTiles(N)
          cachedTiles(E)  = cachedTiles(NE)
          cachedTiles(NW) = None
          cachedTiles(N)  = None
          cachedTiles(NE) = None
        }

        // Render any new tiles that needs content
        renderEmptyTiles()
      }
    }
  }

  /**
   * Executed when a resize operation is performed.
   */
  private def onResize() {
    cachedBackground = renderBackground(View.screen)
    clearTiles()
  }

  /**
   * Executed when a zoom operation have been performed.
   */
  private def clearTiles() {
    val drawing = Drawing.boundary.transform(View.drawingTransformation)

    // Set the isSingleTile value
    isSingleTile = View.screen.width > drawing.width && View.screen.height > drawing.height

    // Set the new render screen and pan
    renderedPan  = View.pan

    // Reset the tile delta
    tileDeltaX = 0; tileDeltaY = 0

    // Render the center tile
    if (isSingleTile) {
      cachedTiles(C) = Some(renderModel(Drawing.boundary.transform(View.drawingTransformation)))
    } else {
      // Set the new delta
      renderedDelta = if (isSingleTile) Drawing.boundary.topLeft.transform(View.drawingTransformation)
                      else View.pan - renderedPan

      // Clear the tiles
      cachedTiles.transform(_ => None)

      // Render the empty tiles
      renderEmptyTiles()
    }
  }

  def paint(graphics : Graphics) {
    // Draw the cached background
    def drawTile(boundary : Rectangle2D, tile : BufferedImage) {
      val x = boundary.topLeft.x.toInt
      val y = boundary.bottomLeft.y.toInt
      graphics.AWTGraphics drawImage(tile, x, y, null)
    }

    graphics.AWTGraphics drawImage(cachedBackground, 0, 0, null)

    // Draw the paper as a rectangle with a margin to illustrate that the paper will have a margin when printed.
    graphics.AWTGraphics.setBackground(Siigna.color("colorBackground").getOrElse(Color.white))
    graphics.AWTGraphics.clearRect(View.boundary.xMin.toInt, View.boundary.yMin.toInt - View.boundary.height.toInt,
                         View.boundary.width.toInt, View.boundary.height.toInt)

    if (isSingleTile) {
      // Draw the single center tile if that encases the entire drawing
      cachedTiles(C).foreach(image => graphics.AWTGraphics drawImage(image, renderedDelta.x.toInt, renderedDelta.y.toInt, null))
    } else {
      cachedTiles(C).foreach(image => drawTile(tile(vC), image))

      // Draw the tiles around if the zoom level is high enough
      val d = renderedDelta + tileDelta
      if (d.x < 0)            cachedTiles(E).foreach(t => drawTile(tile(vE), t)) // East
      if (d.x < 0 && d.y < 0) cachedTiles(SE).foreach(t => drawTile(tile(vSE), t)) // SE
      if (d.y < 0)            cachedTiles(S).foreach(t => drawTile(tile(vS), t))  // South
      if (d.x > 0 && d.y < 0) cachedTiles(SW).foreach(t => drawTile(tile(vSW), t)) // SW
      if (d.x > 0)            cachedTiles(W).foreach(t => drawTile(tile(vW), t))  // West
      if (d.x > 0 && d.y > 0) cachedTiles(NW).foreach(t => drawTile(tile(vNW), t)) // NW
      if (d.y > 0)            cachedTiles(N).foreach(t => drawTile(tile(vN), t))  // North
      if (d.x < 0 && d.y > 0) cachedTiles(NE).foreach(t => drawTile(tile(vNE), t)) // NE
    }

    // Draw the boundary shape
    graphics draw PolylineShape.apply(View.boundary).setAttribute("Color" -> "#AAAAAA".color)
  }

  /**
   * Renders a background-image consisting of "chess checkered" fields on an image equal to the size of the given
   * rectangle.
   * Should only be called every time the screen resizes.
   * @param screen  The screen given in device coordinates (from (0, 0) to (width, height)).
   * @return  A buffered image with dimensions equal to the given screen and a chess checkered field drawn on it.
   */
  def renderBackground(screen : Rectangle2D) : BufferedImage = {
    // Create image
    val image = new BufferedImage(screen.width.toInt, screen.height.toInt, BufferedImage.TYPE_4BYTE_ABGR)
    val g = image.getGraphics
    val size = Siigna.int("backgroundTileSize").getOrElse(12)
    var x = 0
    var y = 0

    // Clear background
    g setColor Siigna.color("colorBackgroundDark").getOrElse("#DADADA".color)
    g fillRect (0, 0, screen.width.toInt, screen.height.toInt)
    g setColor Siigna.color("colorBackgroundLight").getOrElse("E9E9E9".color)

    // Draw a chess-board pattern
    var evenRow = false
    while (x < View.width) {
      while (y < View.height) {
        g.fillRect(x, y, size, size)
        y += size << 1
      }
      x += size
      y = if (evenRow) 0 else size
      evenRow = !evenRow
    }
    image
  }

  /**
   * Renders the [[com.siigna.app.model.Drawing]] in the given area and outputs a image with the same dimensions as
   * the area and with the shapes drawn in the.
   * @param screen  The area of the [[com.siigna.app.model.Drawing]] to render in device coordinates.
   * @return  An image with the same proportions of the given area with the shapes from that area drawn on it.
   * @throws  IllegalArgumentException  if the width or height of the given screen are zero
   */
  private def renderModel(screen : Rectangle2D) = {
    // Create an image with dimensions equal to the width and height of the area
    val image = new BufferedImage(screen.width.toInt, screen.height.toInt, BufferedImage.TYPE_4BYTE_ABGR)
    val g = image.getGraphics.asInstanceOf[Graphics2D]
    val graphics = View.graphics(g)

    // Create a 'window' of the drawing, as seen through the screen
    val window = screen.transform(View.deviceTransformation)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF

    // Create the transformation matrix to move the shapes to (0, 0) of the image
    val scale       = View.drawingTransformation.scaleFactor
    val transformation = TransformationMatrix(Vector2D(-window.topLeft.x.round, window.topLeft.y.round) * scale, scale).flipY

    g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    //apply the graphics class to the model with g - (adds the changes to the image)
    Drawing(window).foreach(t => graphics.draw(t._2.transform(transformation)))

    // Return the image
    image
  }

  /**
   * Renders the empty tiles in the cached tiles asynchronously.
   */
  private def renderEmptyTiles() {
    if (cachedTiles(C).isEmpty) cachedTiles(C) = Some(renderModel(tile(vC)))

    // Make sure the previous promise is done
    renderedPromise.foreach{t =>
      t.tryFailure(new InterruptedException("Stop it!"))
    }

    // Create a new promise
    val p = promise[Array[Option[BufferedImage]]]()

    // Set the callbacks
    p.future.onSuccess {
      case temp : Array[Option[BufferedImage]] => {
        for (i <- 0 until temp.size) {
          if (cachedTiles(i).isEmpty && i != C) {
            cachedTiles(i) = temp(i)
          }
        }
      }
      case x => Log.warning("SiigneRenderer: Expected array from rendering thread, but got: " + x)
    }
    p.future.onComplete { case _ => renderedPromise = None }

    // Start the rendering!
    p.completeWith(future {
      val temp = new Array[Option[BufferedImage]](9)
      // First render the direct neighbours
      if (cachedTiles(E).isEmpty) temp(E) = Some(renderModel(tile(vE)))
      if (cachedTiles(S).isEmpty) temp(S) = Some(renderModel(tile(vS)))
      if (cachedTiles(W).isEmpty) temp(W) = Some(renderModel(tile(vW)))
      if (cachedTiles(N).isEmpty) temp(N) = Some(renderModel(tile(vN)))

      // Render the diagonals
      if (cachedTiles(SE).isEmpty) temp(SE) = Some(renderModel(tile(vSE)))
      if (cachedTiles(SW).isEmpty) temp(SW) = Some(renderModel(tile(vSW)))
      if (cachedTiles(NW).isEmpty) temp(NW) = Some(renderModel(tile(vNW)))
      if (cachedTiles(NE).isEmpty) temp(NE) = Some(renderModel(tile(vNE)))
      temp
    })

    // Set the class variable
    renderedPromise = Some(p)
  }

  // The current distance to the active tile center to the center of the view
  private def tileDelta = Vector2D(tileDeltaX * View.width, tileDeltaY * View.height)

  // Retrieve the rendered tile in the direction given by the tile-vector v
  private def tile(v : Vector2D) =
    (if (isSingleTile) Drawing.boundary.transform(View.drawingTransformation) else View.screen) +
      renderedDelta + Vector2D(View.screen.width * v.x, View.screen.height * v.y) + tileDelta

}
