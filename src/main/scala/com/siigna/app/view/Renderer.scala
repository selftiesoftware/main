package com.siigna.app.view

import java.awt.image.BufferedImage
import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.Rectangle2D

/**
 * A renderer can render different stuff for Siigna like shapes, backgrounds etc. and draw them whenever the
 * [[com.siigna.app.view.View]] asks it to. We strongly recommend using caching to avoid any performance issues.
 *
 * <p>
 * The trait has been made to allow overriding if modules wishes to alter the behavior on how to graphically
 * represent the [[com.siigna.app.model.Drawing]]. The renderer can be replaced by implementing the trait in a
 * class and replace the [[com.siigna.app.view.siigna.SiignaRenderer]] in the view like so:
 * {{{
 *   object MyOwnRenderer extends Renderer { ... }
 *   View.renderer = MyOwnRenderer
 * }}}
 * </p>
 *
 * <p>
 *   To implement the Renderer we really encourage you to use the listeners available in the
 *   [[com.siigna.app.view.View]] to receive updates on zoom and resize. This is particularly efficient for caching
 *   purposes (hint, hint)...
 * </p>
 * @todo Introduce interfaces for selections and displays.
 */
trait Renderer {

  /**
   * Renders the rendered contents of the renderer (...). This method is not called <code>render</code> to illustrate
   * the fact that this method does not generate an image, only transfers the information from the renderer to the
   * given graphics object. Note that the method does not have any shapes or [[com.siigna.app.model.Drawing]] as
   * a parameter, since this should already have been defined and drawn in the <code>renderModel</code> method,
   * independently of the paint method.
   * @param graphics  The [[com.siigna.app.view.Graphics]] object to draw on the underlying screen.
   */
  def paint(graphics : Graphics)

  /**
   * Renders a background for a screen with the given dimensions. This method have been introduced to give you
   * developers a strong hint to use caching whenever possible..
   * @param screen  A rectangle representing the screen of the user, that is the size of the java-frame/applet from
   *                (0, 0) to (width, height).
   * @return  A buffered image where the background has been drawn.
   */
  protected def renderBackground(screen : Rectangle2D) : BufferedImage

  /**
   * Renders a number of shape by drawing them onto the given buffered image. This method have been introduced to give
   * you developers a strong hint to use caching whenever possible.
   * @param shapes  The shapes to draw on the image.
   * @return  A BufferedImage on which the shapes have been drawn.
   */
  protected def renderModel(shapes : Iterable[Shape]) : BufferedImage

}
