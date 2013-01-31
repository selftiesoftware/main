package com.siigna.app.view.siigna

import com.siigna.app.view.{Graphics, View, Renderer}
import com.siigna.app.model.Drawing
import java.awt.image.BufferedImage
import java.awt.{Color, RenderingHints, Graphics2D}
import com.siigna.app.Siigna
import com.siigna.util.Implicits._
import com.siigna.app.model.shape.{Shape, PolylineShape}
import com.siigna.util.geom.{TransformationMatrix, Rectangle2D, Vector2D}

/**
 * Siigna's own implementation of the [[com.siigna.app.view.Renderer]] which draws a chess-checkered background
 * and draws the shapes using the colors defined in the [[com.siigna.app.SiignaAttributes]] (accessible via
 * the [[com.siigna.app.Siigna]] object).
 */
object SiignaRenderer extends Renderer {

  // Add action listener
  Drawing.addActionListener((_, _) => {
    // Update the cached model
    if (View.renderer == this) {
      cachedModel = renderModel(Drawing.shapes.values)
    }
  })

  // Add resize listener
  View.addResizeListener((screen) => if (View.renderer == this) {
    cachedBackground = renderBackground(screen)
  } )

  // Add zoom listener
  View.addZoomListener((zoom) => if (View.renderer == this) cachedModel = renderModel(Drawing.shapes.values))

  /**
   * A background image that can be re-used to draw as background on the canvas.
   */
  private var cachedBackground : BufferedImage = renderBackground(View.screen)

  /**
   * An image of the model that can be re-used instead of calculating the shapes.
   */
  private var cachedModel : BufferedImage = null

  /**
   * The current zoom level, last seen by this renderer. Used for caching purposes.
   */
  var currentZoom : Double = 0.0

  /**
   * The current pan, last seen by this renderer. Used for caching purposes.
   */
  var currentPan : Vector2D = Vector2D(0,0)

  def paint(graphics : Graphics) {
    // Draw the cached background
    graphics.AWTGraphics drawImage(cachedBackground, 0, 0, null)

    // Draw the paper as a rectangle with a margin to illustrate that the paper will have a margin when printed.
    graphics.AWTGraphics.setBackground(Siigna.color("colorBackground").getOrElse(Color.white))
    graphics.AWTGraphics.clearRect(View.boundary.xMin.toInt, View.boundary.yMin.toInt - View.boundary.height.toInt,
                         View.boundary.width.toInt, View.boundary.height.toInt)

    // Draw the model
    val delta = Drawing.boundary.topLeft.transform(View.drawingTransformation)
    graphics.AWTGraphics drawImage(cachedModel, delta.x.toInt, delta.y.toInt, null)

    // Draw the boundary shape
    graphics draw PolylineShape.apply(View.boundary).setAttribute("Color" -> "#AAAAAA".color)
  }

  /**
   * Renders a background-image consisting of "chess checkered" fields. When done the image is stored in a
   * local variable. If the renderBackground method is called again, we simply return the cached copy
   * unless the dimensions of the view has changed, in which case we need to re-render it.
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

  def renderModel(shapes : Iterable[Shape]) = {
    // Creates a transformation so the top left of the drawing is at (0, 0)
    val scale       = View.drawingTransformation.scaleFactor
    val topLeft     = Drawing.boundary.topLeft
    val bottomRight = Vector2D(-topLeft.x, topLeft.y) * scale
    val transformation = TransformationMatrix(bottomRight, scale).flipY

    def width  = (Drawing.boundary.width * scale + 1).toInt
    def height = (Drawing.boundary.height * scale + 1).toInt

    val image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)  // Create image
    val g = image.getGraphics.asInstanceOf[Graphics2D]  //enable drawing on the image
    val graphics = Graphics(g)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF

    g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    //apply the graphics class to the model with g - (adds the changes to the image)
    Drawing.foreach(tuple => {
      graphics.draw(tuple._2.transform(transformation))
    })
    currentZoom = View.zoom  //store the zoom and pan settings
    currentPan  = View.pan
    image
  }

}
