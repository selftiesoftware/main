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

import org.scalatest.{GivenWhenThen, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import com.siigna.app.model.action.{RemoteAction, CreateShape}
import com.siigna.app.model.ActionModel
import com.siigna.app.model.shape.LineShape
import com.sun.net.httpserver.{HttpServer, HttpExchange, HttpHandler}
import com.siigna.util.io.{Marshal, Unmarshal}
import java.net.InetSocketAddress
import scala.collection.immutable.BitSet

/**
 * Tests the remote actor.
 */
class RemoteControllerSpec extends RemoteController(new ActionModel {}, new RESTEndpoint("app.siigna.com", 80), 0)
                              with FunSpec with ShouldMatchers with GivenWhenThen {

  describe("The Remote Controller") {

//    it("Can keep track of the shape ids") {
//      val startTime = System.currentTimeMillis()
//      val actions = (1 to 2).map(i => CreateShape(-i, LineShape(0, 0, 10, 10)))
//      Given(s"${actions.size} shapes")
//      When("Synchronizing them with the server")
//      actions.foreach(a => controller.sendActionToServer(a, undo = false))
//      while(controller.isSynchronising){}
//
//      Then(s"The id should be incremented by ${actions.size}")
//      println(controller.actionIndices)
//      controller.actionIndices should equal(BitSet(0, 1))
//      controller.localIdMap should equal(Map(-1 -> 0, -2 -> 1))
//      Then(s"Taking ${System.currentTimeMillis() - startTime}ms")
//      server.stop()
//    }

  }
}