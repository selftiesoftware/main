package com.siigna.app.view

/**
 * A renderer can render different stuff for Siigna like shapes, backgrounds etc. and draw them whenever the
 * [[com.siigna.app.view.View]] asks it to. The only method to implement is called paint and is called whenever the
 * hardware of the computer running Siigna is ready. Computing a new image of the [[com.siigna.app.model.Drawing]] is
 * possible, but strongly discouraged. Instead, we strongly recommend using caching to avoid any performance issues.
 * See the native [[com.siigna.app.view.native.SiignaRenderer]] for inspiration, or read the Wikipedia entry on
 * [http://en.wikipedia.org/wiki/Cache_(computing) caching].
 *
 * <p>
 * The trait has been made to allow overriding if modules wishes to alter the behavior on how to graphically
 * represent the [[com.siigna.app.model.Drawing]]. The renderer can be replaced by implementing the trait in a
 * class and replace the [[com.siigna.app.view.native.SiignaRenderer]] in the view like so:
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
   * Examines whether this [[com.siigna.app.view.Renderer]] is active, meaning that it is being used by the
   * [[com.siigna.app.view.View]]. This could be useful for determining if the renderer should continue to cache
   * information whenever external calls, such as callbacks from listeners, arrive.
   * @return  A boolean indicating if the renderer is current in use (true) or not (false).
   */
  def isActive : Boolean = View.renderer.exists(_ == this)

  /**
   * Renders the rendered contents of the renderer (...). This method is not called <code>render</code> to illustrate
   * the fact that this method does not generate an image, only transfers the information from the renderer to the
   * given graphics object. Note that the method does not have any shapes or [[com.siigna.app.model.Drawing]] as
   * a parameter, since this should already have been defined and drawn in the <code>renderModel</code> method,
   * independently of the paint method.
   * @param graphics  The [[com.siigna.app.view.Graphics]] object to draw on the underlying screen.
   */
  def paint(graphics : Graphics)

}
