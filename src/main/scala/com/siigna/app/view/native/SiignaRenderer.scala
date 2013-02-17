package com.siigna.app.view.native

import com.siigna.app.view.{Graphics, View, Renderer}
import com.siigna.app.model.Drawing
import java.awt.image.BufferedImage
import java.awt.{Color, RenderingHints, Graphics2D}
import com.siigna.app.Siigna
import com.siigna.util.Implicits._
import com.siigna.app.model.shape.{LineShape, PolylineShape}
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D, TransformationMatrix, Rectangle2D}
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Siignas own implementation of the [[com.siigna.app.view.Renderer]] which draws a chess-checkered background
 * and the shapes using caching tecniques. The SiignaRenderer uses the colors and attributes defined in the
 * [[com.siigna.app.SiignaAttributes]] (accessible via the [[com.siigna.app.Siigna]] object).
 */
object SiignaRenderer extends Renderer {

  // Add listeners
  Drawing.addActionListener((_, _) => if (View.renderer == this) onAction())
  View.addPanListener(v =>            if (View.renderer == this) onPan(v))
  View.addResizeListener((screen) =>  if (View.renderer == this) onResize(screen))
  View.addZoomListener((zoom) =>      if (View.renderer == this) onZoom(zoom) )

  // A background image that can be re-used to draw as background on the canvas.
  private var cachedBackground : BufferedImage = renderBackground(View.screen)

  // The tiles drawn as 3 * 3 squares over the model.
  private lazy val cachedTiles = Array.fill[Option[BufferedImage]](9)(None)

  // Constants for the different tiles and their revolving around a given center
  private val C  = 4; private val vC  = Vector2D( 0, 0)
  private val E  = 5; private val vE  = Vector2D( 1, 0)
  private val SE = 8; private val vSE = Vector2D( 1, 1)
  private val S  = 7; private val vS  = Vector2D( 0, 1)
  private val SW = 6; private val vSW = Vector2D(-1, 1)
  private val W  = 3; private val vW  = Vector2D(-1, 0)
  private val NW = 0; private val vNW = Vector2D(-1,-1)
  private val N  = 1; private val vN  = Vector2D( 0,-1)
  private val NE = 2; private val vNE = Vector2D( 1,-1)

  // Retrieve the rendered tile in the direction given by the tile-vector v
  private def tile(v : Vector2D) =
    renderedScreen + renderedDelta + Vector2D(View.screen.width * v.x, View.screen.height * v.y)

  // A boolean value to indicate that the view is zoomed out enough to only need one single tile
  private var isSingleTile = true

  // The distance to the top left corner of the image to render, from the top left corner of the screen
  // Two scenarios: Model < Screen   - The model should be placed at the top left corner of the drawing
  //                Model >= Screen  - The model should be placed at the top left corner of the screen
  private var renderedDelta : Vector2D = Vector2D(0, 0)

  // The pan at the time of rendering
  private var renderedPan : Vector2D = Vector2D(0, 0)

  // The center screen as rendered - may change if focus (center) shifts to a new tile
  private var renderedScreen : SimpleRectangle2D = View.screen

  /**
   * Executed when an action is performed on the model.
   */
  private def onAction() {
    // Set the new delta
    renderedDelta = if (isSingleTile) Drawing.boundary.topLeft.transform(View.drawingTransformation)
                    else View.pan - renderedPan

    // Set the new render screen and pan
    renderedPan    = View.pan
    renderedScreen = if (isSingleTile) Drawing.boundary.transform(View.drawingTransformation)
    else View.screen

    // Render the new model
    renderTiles()
  }

  /**
   * Executed when a pan operation have been done
   * @param pan  The new pan vector
   */
  private def onPan(pan : Vector2D) {
    // Set the new delta
    renderedDelta = if (isSingleTile) Drawing.boundary.topLeft.transform(View.drawingTransformation)
                    else View.pan - renderedPanxc

    // Re-arrange the tiles for a new center, if necessary
    if (!isSingleTile) {
      // The delta panned by the user, relative to the center of the view
      val delta = View.center - renderedDelta

      // Is the center tile still in focus?
      if (!renderedScreen.contains(delta)) {
        val x = delta.x
        val y = delta.y
        val h = View.screen.height
        val w = View.screen.width

        // Set the new center
        if (renderedScreen.xMin > x) renderedScreen -= Vector2D(w, 0) // -X
        if (renderedScreen.xMax < x) renderedScreen += Vector2D(w, 0) // +X
        if (renderedScreen.yMin > y) renderedScreen -= Vector2D(0, h) // -Y
        if (renderedScreen.yMax < y) renderedScreen += Vector2D(0, h) // +Y

        renderTiles()
      }
    }
  }

  /**
   * Executed when a resize operation have been performed.
   * @param screen  The new dimensions for the view-screen (in device coordinates)
   */
  private def onResize(screen : Rectangle2D) {
    cachedBackground = renderBackground(screen)
  }

  /**
   * Executed when a zoom operation have been performed.
   * @param zoom  The new zoom-level.
   */
  private def onZoom(zoom : Double) {
    val drawing = Drawing.boundary.transform(View.drawingTransformation)
    renderedScreen = View.screen

    // Set the isSingleTile value
    isSingleTile = renderedScreen.width > drawing.width && renderedScreen.height > drawing.height

    // Set the new render screen and pan
    renderedPan    = View.pan
    renderedScreen = if (isSingleTile) drawing else View.screen

    // Render the center tile
    if (isSingleTile) {
      cachedTiles(C) = Some(renderModel(renderedScreen))
    } else renderTiles()
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

    if (isSingleTile && cachedTiles(C).isDefined) {
      // Draw the single center tile if that encases the entire drawing
      val tileImage = cachedTiles(C).get
      graphics.AWTGraphics drawImage(tileImage, renderedDelta.x.toInt, renderedDelta.y.toInt, null)
    } else {
      cachedTiles(C).foreach(t => drawTile(tile(vC), t))

      // Draw the tiles around if the zoom level is high enough
      val d = -renderedDelta
      if (d.x > 0)            cachedTiles(E).foreach(t => drawTile(tile(vE), t)) // East
      if (d.x > 0 && d.y > 0) cachedTiles(SE).foreach(t => drawTile(tile(vSE), t)) // SE
      if (d.y > 0)            cachedTiles(S).foreach(t => drawTile(tile(vS), t))  // South
      if (d.x < 0 && d.y > 0) cachedTiles(SW).foreach(t => drawTile(tile(vSW), t)) // SW
      if (d.x < 0)            cachedTiles(W).foreach(t => drawTile(tile(vW), t))  // West
      if (d.x < 0 && d.y < 0) cachedTiles(NW).foreach(t => drawTile(tile(vNW), t)) // NW
      if (d.y < 0)            cachedTiles(N).foreach(t => drawTile(tile(vN), t))  // North
      if (d.x > 0 && d.y < 0) cachedTiles(NE).foreach(t => drawTile(tile(vNE), t)) // NE
    }

    // Draw the boundary shape
    graphics draw PolylineShape.apply(View.boundary).setAttribute("Color" -> "#AAAAAA".color)

    graphics drawCircle(renderedScreen.center + renderedDelta, 10)
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
   */
  private def renderModel(screen : Rectangle2D) = {
    def width  = screen.width.toInt
    def height = screen.height.toInt

    // Create an image with dimensions equal to the width and height of the area
    val image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    val g = image.getGraphics.asInstanceOf[Graphics2D]
    val graphics = View.graphics(g)

    // Create a 'window' of the drawing, as seen through the screen
    val window = screen.transform(View.deviceTransformation)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF

    // Create the transformation matrix to move the shapes to (0, 0) of the image
    val scale       = View.drawingTransformation.scaleFactor
    val transformation = TransformationMatrix(Vector2D(-window.topLeft.x, window.topLeft.y) * scale, scale).flipY

    g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    graphics.draw(LineShape(-1000, -10, 1000, 10).transform(transformation))

    //apply the graphics class to the model with g - (adds the changes to the image)
    Drawing(window).foreach(t => graphics.draw(t._2.transform(transformation)))

    // Return the image
    image
  }

  /**
   * Renders the nine tiles by synchronously rendering the center-tile first (to show the content as quick as possible)
   * and asynchronously rendering the others.
   */
  private def renderTiles() {
    // Clean out old tiles
    cachedTiles.transform(_ => None)

    // Render the center tile
    cachedTiles(C) = Some(renderModel(tile(vC)))

    // If the drawing is big enough for the other tiles to be active, we need to render them as well
    if (!isSingleTile) {
      // Queue the rendering of the 8 other tiles
      future {
        // First render the direct neighbours
        cachedTiles(E) = Some(renderModel(tile(vE)))
        cachedTiles(S) = Some(renderModel(tile(vS)))
        cachedTiles(W) = Some(renderModel(tile(vW)))
        cachedTiles(N) = Some(renderModel(tile(vN)))

        // Render the diagonals
        cachedTiles(SE) = Some(renderModel(tile(vSE)))
        cachedTiles(SW) = Some(renderModel(tile(vSW)))
        cachedTiles(NW) = Some(renderModel(tile(vNW)))
        cachedTiles(NE) = Some(renderModel(tile(vNE)))
      }
    }
  }

}
