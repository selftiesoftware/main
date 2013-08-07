/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.event

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import com.siigna.util.geom.Vector2D
import com.siigna.app.model.shape.PolylineShape
import com.siigna.app.view.View

/**
 * Test end point snap.
 */
class EndPointsSpec extends FunSuite with ShouldMatchers {
  //simulate points being clicked on the screen (device)
  def deviceCoords = View.drawingTransformation

  test("snap to endpoints in scale 1:1") {
    val mousePt1 = Vector2D(49.9,-99.9).transform(deviceCoords) //a point in deviceCoordinates
    val mousePt2 = Vector2D(20,20).transform(deviceCoords)
    val mousePt3 = Vector2D(4,4).transform(deviceCoords)
    val p = Traversable(PolylineShape(Vector2D(50, -100), Vector2D(0, 0)))

    //the points to be evaluated should be in device coordinates, in order to emulate a point shown on the screen.
    //EndPoints.snap(mousePt1, p) should equal(Vector2D(50,-100).transform(deviceCoords)) //inside snap distance
    //EndPoints.snap(mousePt2, p) should equal(Vector2D(20,20).transform(deviceCoords)) //outside snap distance
    //EndPoints.snap(mousePt3, p) should equal(Vector2D(0,0).transform(deviceCoords)) //inside snap distance
  }

  test("snap to endpoints in scale 1:1000") {
    View.zoom = 0.001
    val mousePt1 = Vector2D(49900,-99900).transform(deviceCoords)
    val mousePt2 = Vector2D(20000,20000).transform(deviceCoords)
    val mousePt3 = Vector2D(4000,4000).transform(deviceCoords)
    val p = Traversable(PolylineShape(Vector2D(50000, -100000), Vector2D(0, 0)))

    EndPoints.snap(mousePt3, p) should equal(Vector2D(0,0).transform(deviceCoords)) //inside snap distance
    EndPoints.snap(mousePt1, p) should equal(Vector2D(50000,-100000).transform(deviceCoords)) //inside snap distance
    EndPoints.snap(mousePt2, p) should equal(Vector2D(20000,20000).transform(deviceCoords)) //outside snap distance
  }
}
