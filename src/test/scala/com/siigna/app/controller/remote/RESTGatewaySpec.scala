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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FunSpec}
import com.siigna.app.model.action.{CreateShape, RemoteAction}
import com.siigna.app.model.shape.LineShape
import com.siigna.app.Siigna

/**
 * Tests the REST HTTP Gateway.
 */
class RESTGatewaySpec extends FunSpec with ShouldMatchers with GivenWhenThen {

  //val client  = new RESTGateway("http://app.siigna.com")
  val client  = new RESTGateway("http://localhost:20004")
  val drawingId = client.getNewDrawingId(Session(0, Siigna.user)).left.get
  val session = Session(drawingId, Siigna.user)

  describe("A Gateway") {

    it ("can find out whether the server is alive") {
      client.alive should equal(true)
    }

    it ("can store and retrieve an action and their ids from the server") {
      Given(s"A new drawing with id $drawingId")

      When("Getting a shape id from the server")
      val iRes = client.getShapeIds(1, session)

      Then("It should return an Int")
      val range = iRes.left.get
      range.size should equal (1)
      val id = range.head
      id should be >= 0

      val action = RemoteAction(CreateShape(id, LineShape(0, 0, 1, 1)))
      Given (s"An action $action")

      When("Dispatching it to the gateway")
      val res = client.setAction(action, session)

      Then("The server should return an id")
      val aId = res.left.get
      aId should be >= 0

      When("Fetching the action from the server")
      val aRes = client.getAction(aId, session)

      Then("The received action should equal the sent action")
      aRes.left.get should equal(action)
    }

    it ("can store and receive more than one action") {
      Given(s"A new drawing with id $drawingId")

      When("Getting 6 shape ids from the server")
      val iRes = client.getShapeIds(6, session)

      Then("It should return an Int")
      val range = iRes.left.get
      val size = range.size
      range.size should equal(6)
      Given(s"$size actions")
      val actions = range map (i => RemoteAction(CreateShape(i, LineShape(i, 0, 0, i))))

      When("Dispatching it to the gateway")
      val sRes = client.setActions(actions, session)

      Then(s"The server should return $size new action ids")
      val ids = sRes.left.get
      ids.size should equal(size)

      When("Fetching the actions from the server")
      val aRes = client.getActions(ids, session)

      Then("The received actions should equal the sent actions")
      aRes.left.get.toSet should equal(actions.toSet)
    }


    it ("can get an id for a new drawing") {
      Given(s"A new drawing with id $drawingId")

      When("Getting the current action id")
      val oldRes = client.getActionId(session)

      Then("It should be larger than or equal to 0")
      val oldId = oldRes.left.get
      oldId should be >= 0

      When("Sending an action")
      val action = RemoteAction(CreateShape(7, LineShape(0, 0, 0, 1)))
      val aRes = client.setAction(action, session)

      Then("The action id should be >= 0")
      val aId = aRes.left.get
      aId should be >= 0

      When("Getting the current action id")
      val newRes = client.getActionId(session)

      Then("The new id should equal the id of the recent action")
      val newId = newRes.left.get
      newId should equal(aId)

      Then("The new id should equal the id of old action + 1")
      newId should equal(oldId + 1)
    }

    it ("can get an existing drawing") {
      Given(s"A drawing with id $drawingId")

      When("Getting the existing drawing")
      val iRes = client.getDrawing(drawingId, session)

      Then("The new drawing should be returned")
      val drawing = iRes.left.get
      drawing.attributes.long("id").get should equal(drawingId)
      drawing.attributes.int("lastAction")
      drawing.attributes.char("openness")
    }

  }

}
