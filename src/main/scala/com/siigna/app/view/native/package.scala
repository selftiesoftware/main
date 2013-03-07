package com.siigna.app.view

/**
 * <p>
 *   The native view package is Siignas own attempt to implement the view functionality using caching, tiling and
 *   other useful techniques. It can all be overridden though, if you think you can do better - or need something else.
 * </p>
 *
 * <h2>SiignaGraphics</h2>
 * <p>
 *   The [[com.siigna.app.view.native.SiignaGraphics]] class is a class that implements the
 *   [[com.siigna.app.view.Graphics]] trait. This primarily have to do with drawing
 *   [[com.siigna.app.model.shape.Shape]]s but it is also responsible for handling colours and making sure
 *   the drawing is done thread-safe. If multiple threads are allowed to paint at the same time the attributes
 *   of the shapes will mess up, since AWT graphic settings, such as colour, is not thread-safe.
 * </p>
 * <p>
 *  It can be overridden like so:
 *  {{{
 *    import java.awt.{Graphics => AWTGraphics}
 *    import com.siigna.app.view.{Graphics, View}
 *    class MyOwnGraphics(g : AWTGraphics) extends Graphics { ... }
 *
 *    // Set the graphics instance in the View
 *    // - a function that received an awt.Graphics and returns a Siigna Graphics
 *    View.graphics = (g) => new MyOwnGraphics(g)
 *    // Henceforth every paint-tick will use an instance of MyOwnGraphics
 *  }}}
 * </p>
 *
 * <h2>SiignaRenderer</h2>
 * <p>
 *   An object that can render shapes in a non-naïve optimized way. Currently we use tiles to render the canvas,
 *   which is renderes whenever the zoom changes. In this way each tile (image) can be blitted directly on screen
 *   at each paint-tick, so we do not have to re-paint all the time. This is probably the most interesting part
 *   to implement as it gives you complete control of paint-caching, which can be very useful to implement if you
 *   are using Siigna for other things than simple 2D drawing.
 * </p>
 * <p>
 *   The native implementation can be overriden like so:
 *   {{{
 *     import com.siigna.app.view.{Renderer, View}
 *     object MyOwnRenderer extends Renderer { ... }
 *
 *     // Set the renderer instance in the View
 *     View.renderer = MyOwnRenderer
 *     // Et voilà - siigna will henceforth (at every paint-tick) use your renderer
 *   }}}
 * </p>
 */
package object native
