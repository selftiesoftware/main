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

package com.siigna.util.event

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.app.model.shape._
import com.siigna.util.geom.Vector2D

/**
 * Tests the [[com.siigna.util.event.CenterPointSnap]].
 */
class CenterPointSnapSpec extends FunSpec with ShouldMatchers {

  describe("CenterPointSnap") {

    it("can snap to centerpoints") {
      val model = Traversable(
        ArcShape(Vector2D(0, 0), Vector2D(10, 10), Vector2D(20, 0)),
        CircleShape(Vector2D(-100, 100), 10),
        LineShape(-20, -20, 0, 0),
        PolylineShape(Vector2D(10, -10), Vector2D(20, -20)),
        RectangleShape(80, 80, 120, 120)
      )

      CenterPointSnap.snap(Vector2D(  11,   0), model) should equal(Vector2D(10, 0))     // Arc
      CenterPointSnap.snap(Vector2D(-100, 101), model) should equal(Vector2D(-100, 100)) // Circle
      CenterPointSnap.snap(Vector2D( 100,  99), model) should equal(Vector2D(100, 100))  // Rectangle

      // Line and PL does not have a center point
      CenterPointSnap.snap(Vector2D( -10,  -9), model) should equal(Vector2D(-10, -9))
      CenterPointSnap.snap(Vector2D(  15, -14), model) should equal(Vector2D(15, -14))
    }

  }

}
