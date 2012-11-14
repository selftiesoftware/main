package com.siigna.app.controller.remote

import org.scalatest.fixture.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfter
import com.siigna.app.controller.remote.{RemoteConstants => RC, RemoteController => R}

/**
 * Tests the remote actor.
 */
object RemoteControllerSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  // Set all members accessible in RemoteController
  val c = R.getClass
  val m = c.getDeclaredMethods
  val f = c.getDeclaredFields
  (m ++ f).foreach(_.setAccessible(true))
  val server = new Server("localhost", Mode.Production)
  val session = c.getMethod("session").invoke(c)

  before { new MockServer().start() }

  describe("The remote controller") {

    it("can establish connection to a server") {
      server(Get(RC.Drawing, null, session))
    }

  }

}
