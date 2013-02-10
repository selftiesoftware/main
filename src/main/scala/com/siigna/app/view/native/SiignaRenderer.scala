package com.siigna.app.view.native

import com.siigna.app.view.{Graphics, View, Renderer}
import com.siigna.app.model.Drawing
import java.awt.image.BufferedImage
import java.awt.{Color, RenderingHints, Graphics2D}
import com.siigna.app.Siigna
import com.siigna.util.Implicits._
import com.siigna.app.model.shape.PolylineShape
import com.siigna.util.geom.{Vector2D, TransformationMatrix, Rectangle2D}

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

  // A boolean value to indicate that the view is zoomed out enough to only need one single tile
  private var isSingleTile = true

  // The delta to the top left image to render
  private var renderedDelta : Vector2D = Vector2D(0, 0)

  // The pan at the time of rendering
  private var renderedPan : Vector2D = Vector2D(0, 0)

  // The center screen as rendered
  private var renderedScreen : Rectangle2D = View.screen

  /**
   * Arranges the tiles to make sure that the view-bound, illustrated by the pan-vector, is inside the center-tile.
   * If it isn't we need to rearrange the tiles and prepare for caching of the missing tiles.
   * @param pan  The current pan.
   */
  protected def arrangeTiles(pan : Vector2D) {

  }

  /**
   * Executed when an action is performed on the model.
   */
  protected def onAction() { renderTiles() }

  /**
   * Executed when a pan operation have been done
   * @param pan  The new pan vector
   */
  protected def onPan(pan : Vector2D) {
    // Set the new delta
    renderedDelta = if (isSingleTile) Drawing.boundary.topLeft.transform(View.drawingTransformation)
                    else View.pan - renderedPan

    // Arrange the tiles according to the new pan
    arrangeTiles(pan)
  }

  /**
   * Executed when a resize operation have been performed.
   * @param screen  The new dimensions for the view-screen (in device coordinates)
   */
  protected def onResize(screen : Rectangle2D) {
    cachedBackground = renderBackground(screen)
  }

  /**
   * Executed when a zoom operation have been performed.
   * @param zoom  The new zoom-level.
   */
  protected def onZoom(zoom : Double) {
    val drawing = Drawing.boundary.transform(View.drawingTransformation)
    renderedScreen = View.screen

    // Set the isSingleTile value
    isSingleTile = renderedScreen.width > drawing.width && renderedScreen.height > drawing.height

    // Render the center tile
    renderTiles()
  }

  def paint(graphics : Graphics) {
    // Draw the cached background
    graphics.AWTGraphics drawImage(cachedBackground, 0, 0, null)

    // Draw the paper as a rectangle with a margin to illustrate that the paper will have a margin when printed.
    graphics.AWTGraphics.setBackground(Siigna.color("colorBackground").getOrElse(Color.white))
    graphics.AWTGraphics.clearRect(View.boundary.xMin.toInt, View.boundary.yMin.toInt - View.boundary.height.toInt,
                         View.boundary.width.toInt, View.boundary.height.toInt)

    // Define the position where the tile should be placed
    // Two scenarios: Model < Screen   - The model should be placed at the top left corner of the drawing
    //                Model >= Screen  - The model should be placed at (0, 0)

    // Draw the model
    if (cachedTiles(5).isDefined) {
      val tile = cachedTiles(5).get
      graphics.AWTGraphics drawImage(tile, renderedDelta.x.toInt, renderedDelta.y.toInt, null)

      graphics drawRectangle(renderedDelta, renderedDelta + Vector2D(tile.getWidth, tile.getHeight))
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
   * @param area  The area of the [[com.siigna.app.model.Drawing]] to render in device coordinates.
   * @return  An image with the same proportions of the given area with the shapes from that area drawn on it.
   */
  protected def renderModel(area : Rectangle2D) = {
    def width  = area.width.toInt + 1
    def height = area.height.toInt + 1

    // Create an image with dimensions equal to the width and height of the area
    val image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    val g = image.getGraphics.asInstanceOf[Graphics2D]
    val graphics = View.graphics(g)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF

    // Create the transformation matrix to move the shapes to (0, 0) of the image
    val scale       = View.drawingTransformation.scaleFactor
    val topLeft     = area.topLeft
    val bottomRight = Vector2D(-topLeft.x, topLeft.y) * scale
    val transformation = TransformationMatrix(bottomRight, scale).flipY

    g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    //apply the graphics class to the model with g - (adds the changes to the image)
    Drawing(area).foreach(t => graphics.draw(t._2.transform(transformation)))

    // Return the image
    image
  }

  /**
   * Renders the nine tiles by synchronously rendering the center-tile to show the content as quick as possible
   * and asynchronously render the others to allow for concurrent
   */
  protected def renderTiles() {
    // Clean out old tiles
    cachedTiles.transform(_ => None)

    // Set the new render screen and pan
    renderedPan    = View.pan
    renderedScreen = if (isSingleTile) Drawing.boundary.transform(View.drawingTransformation)
                     else View.screen

    // Render the center tile
    cachedTiles(5) = Some(renderModel(renderedScreen))

    // Queue the rendering of the 8 other tiles
    //scala.concurrent.future {

    //}
  }

}
