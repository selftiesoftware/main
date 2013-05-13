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

package com.siigna.app.model.action

import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.Vector2D
import com.siigna.app.model.shape.{Shape, LineShape}
import collection.parallel.immutable.{ParMap, ParHashMap}
import com.siigna.app.model.{Drawing, Model}
import org.scalatest.{FunSpec, Spec}

/**
 * A test for the Create object and associated classes
 */
class CreateTest extends FunSpec with ShouldMatchers {

  val id = 123142
  val line = LineShape(Vector2D(0, 0), Vector2D(13, 14))
  val model = new Model()

  describe("CreateShape") {
    val action = CreateShape(id, line)

    it("can be initialized with one shape") {
      action.id should equal (id)
      action.shape should equal (line)
    }

    it("can be executed") {
      action.execute(model).shapes should equal (ParHashMap(id -> line))
    }
    
    it("can be undone") {
      // Empty model
      action.undo(model).shapes should equal (model.shapes)
      
      // Match
      val lineModel = model.add(id, line)
      action.execute(lineModel).shapes should equal (ParHashMap(id -> line))
    }
  }

  describe("CreateShapes") {

    val map = Map(id -> line)
    val twoMap = Map(id -> line, 4123 -> LineShape(Vector2D(200, 200), Vector2D(300, 300)))
    val multiModel = model.add(twoMap)
    val action = CreateShapes(map)

    it("can be initialized") {
      // Empty
      evaluating { CreateShapes(Map()) } should produce[IllegalArgumentException]

      // One size
      action.shapes should equal(map)

      // More
      CreateShapes(twoMap).shapes should equal (twoMap)
    }

    it("can be executed") {
      action.execute(model).shapes should equal (map)
    }

    it("can be undone") {
      // Empty
      action.execute(model).shapes.size should equal (1)

      // Match
      val multiAction = CreateShapes(twoMap)
      multiAction.undo(multiModel).shapes.size should equal (0)
    }

  }

  describe("The Create object") {

    it("can create a single shape") {
      Create(line)
      Drawing.shapes should equal(Map(-1 -> line))
    }

  }

}