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

package com.siigna.app.model

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.app.model.action.CreateShape
import com.siigna.app.controller.remote.UpdateLocalActions
import com.siigna.app.model.shape.LineShape

/**
 * Created with IntelliJ IDEA.
 * User: JensEgholm
 * Date: 15-12-13
 * Time: 22:31
 * To change this template use File | Settings | File Templates.
 */
class DrawingSpec extends FunSpec with ShouldMatchers {

  val d = new Drawing() {
    def boundary: SimpleRectangle2D = SimpleRectangle2D(0, 0, 10, 10)
  }

  describe("A Drawing") {

    it("can fail when requesting non-existing local ids") {
      intercept[Throwable] {
        d(-1)
      }
    }

    it("can find a local id after being updated") {
      val l = LineShape(0, 0, 1, 1)
      d.execute(CreateShape(-1, l))
      d(-1) should equal(l)
      d.execute(UpdateLocalActions(Map(-1 -> 1)))
      d(1) should equal(l)
      d(-1) should equal(l)
    }

  }

}
