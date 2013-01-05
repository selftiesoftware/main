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

package com.siigna.app.controller.remote

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.app.controller.remote.{RemoteConstants => RC}
import com.siigna.app.Siigna
import com.siigna.util.Serializer
import com.siigna.app.model.action.{CreateShape, RemoteAction}
import com.siigna.app.model.shape.LineShape

/**
 * Tests the remote actor.
 */
class RemoteControllerSpec extends FunSpec with ShouldMatchers {

  // Set all members accessible in RemoteController
  val sink = new Server("62.243.118.234", Mode.Production)
  var session : Session = null

  sink(Get(RC.DrawingId, null, Session(0L, Siigna.user)),
    r => session = Session(r.asInstanceOf[Set].value.asInstanceOf[Long], Siigna.user))

  //describe("The Remote Controller Handles") {

    // Test handles

  //}

  describe("The Remote Controller") {
    var range : Range = null

    it ("can fetch a new drawing id") {
      sink(Get(RC.DrawingId, null, Session(0L, Siigna.user)), r => {
        val set = r.asInstanceOf[Set]
        set.name should equal(RC.DrawingId)
        val id = set.value.asInstanceOf[Long]
        id should be > 0L
        session = Session(id, Siigna.user)
      })
    }

    it ("can get the latest action id for a drawing") {
      sink(Get(RC.ActionId, null, session), r => {
        val set = r.asInstanceOf[Set]
        val id = set.value.asInstanceOf[Int]
        id should equal (0)
      })
    }

    it ("can get a drawing") {
      sink(Get(RC.Drawing, session.drawing, session), r => {
        val set = r.asInstanceOf[Set]
        val bytes = set.value.asInstanceOf[Array[Byte]]
        val remote = Serializer.readDrawing(bytes)
        remote.model.shapes.size should equal (0)
      })
    }

    it ("can get shape ids") {
      def getRange(x : Int) {
        sink(Get(RC.ShapeId, x, session), r => {
          val set = r.asInstanceOf[Set]
          range = set.value.asInstanceOf[Range]
          range.size should equal (x)
        })
      }
      getRange(1)
      getRange(12)
    }

    it ("can set an action") {
      // TODO: Find a better solution in coming scalatest versions
      if (range != null) {
        val action = RemoteAction(CreateShape(range.head, LineShape(0, 0, 10, 10)))
        sink(Set(RC.Action, action, session), r => {
          val set = r.asInstanceOf[Set]
          set.value.isInstanceOf[Int] should be (right = true)
        })
      }
    }

  }

}
