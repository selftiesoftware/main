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
  * Tests the [[com.siigna.util.event.IntersectionPointSnap]].
  */
class IntersectionPointSnapSpec extends FunSpec with ShouldMatchers {

   describe("IntersectionPointSnap") {

     it("can snap to intersection-points") {
       val model = Traversable(
         LineShape(-20, -20, 20, 20),
         PolylineShape(Vector2D(-20, 20), Vector2D(20, -20)),
         RectangleShape(-10, -10, 10, 10)
       )

       IntersectionPointSnap.snap(Vector2D( 1, 0), model) should equal(Vector2D(0, 0))
       IntersectionPointSnap.snap(Vector2D(11, 9), model) should equal(Vector2D(10, 10))
       IntersectionPointSnap.snap(Vector2D(-9,11), model) should equal(Vector2D(-10, 10))
       IntersectionPointSnap.snap(Vector2D(-9,-9), model) should equal(Vector2D(-10, -10))
       IntersectionPointSnap.snap(Vector2D(11,-9), model) should equal(Vector2D(10, -10))
     }

   }

 }
