package com.siigna.app.controller.remote

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.app.controller.remote.{RemoteConstants => RC, RemoteController => R}

/**
 * Tests the remote actor.
 */
class RemoteControllerSpec extends FunSpec with ShouldMatchers {

  // Set all members accessible in RemoteController
  val c = R.getClass
  val m = c.getDeclaredMethods
  val f = c.getDeclaredFields
  (m ++ f).foreach(_.setAccessible(true))
  val sink = new Server("localhost", Mode.Production)
  val session = c.getMethod("session").invoke(c).asInstanceOf[Session]


  describe("The remote controller") {

    it ("can establish connection to a server") {
      assert(sink(Get(RC.Drawing, null, session), r => r).isInstanceOf[Right[_, _]])
    }

    it("can receive a drawing") {

    }

  }

}
