/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.app.view.native

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.siigna.app.view.View
import com.siigna.util.geom.{Vector2D, SimpleRectangle2D}
import com.siigna.app.model.Drawing

/**
 * Tests the [[com.siigna.app.view.native.TilePainter]].
 */
class TilePainterSpec extends TilePainter with FunSpec with ShouldMatchers {

  protected val drawing = new Drawing { def boundary = SimpleRectangle2D(0, 0, 1, 1) }

  protected def update() : TilePainter = this

  protected def view = new View {
    def paint(screenGraphics: java.awt.Graphics, drawing: Drawing, interface: Option[com.siigna.app.view.Interface]) {}

    def screen: SimpleRectangle2D = SimpleRectangle2D(0, 0, 1, 1)
  }

  def paint(graphics: com.siigna.app.view.Graphics) {}

  def onComplete[U](func: (util.Try[TilePainter]) => U) {}

  def interrupt: Boolean = false


  describe ("A TilePainter") {

    it ("can start with a zero pan") {
      panX should equal (0)
      panY should equal (0)
    }

    it ("can pan correctly") {
      pan(Vector2D(1, 0))
      panX should equal (1)
      panY should equal (0)

      pan(Vector2D(-1, 0))
      panX should equal (0)
      panY should equal (0)

      pan(Vector2D(0, 1))
      panX should equal (0)
      panY should equal (1)

      pan(Vector2D(0, -1))
      panX should equal (0)
      panY should equal (0)
    }

  }
}
