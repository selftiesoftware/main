/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import collection.GenMap
import collection.parallel.immutable.ParHashMap
import shape.{LineShape, ImmutableShape}
import com.siigna.util.geom.Vector2D

/**
 * Test the Model itself.
 */
class ModelSpec extends Spec with ShouldMatchers {

  describe("the model") {

    val model = new Model(ParHashMap());

    it("has a parallel map of ints and shapes") {
      model.shapes.isInstanceOf[GenMap[Int, ImmutableShape]] should be (true)
    }
    
    it("can build a new model") {
      val shapes = ParHashMap(112 -> LineShape(Vector2D(0, 0), Vector2D(112, 135)))
      model.build(shapes).shapes should equal (shapes)
    }


  }

}
