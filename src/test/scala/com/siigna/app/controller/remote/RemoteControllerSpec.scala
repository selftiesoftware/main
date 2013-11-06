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
import com.siigna.app.model.action._
import com.siigna.app.model.ActionModel
import com.siigna.app.model.shape.LineShape
import com.siigna.util.geom.{Vector2D, TransformationMatrix}
import com.siigna.app.model.action.TransformShapes
import com.siigna.app.model.action.CreateShape
import com.siigna.app.model.action.DeleteShape

/**
 * Tests the remote actor.
 */
class RemoteControllerSpec extends FunSpec with ShouldMatchers with GivenWhenThen {

  describe("The Remote Controller") {

    val dummyModel = new ActionModel {}
    //def gateway = new RESTGateway("http://app.siigna.com")
    def gateway = new RESTGateway("http://localhost:20004")

    // Use case 1 from https://trello.com/c/RMrddOtd/2-shapes-forsvinder
    it ("can persist a drawing over time") {
      val model = new ActionModel {}
      val c1 = new RemoteController(model, gateway, 20)
      c1.init()
      model.addRemoteListener(c1.sendActionToServer)
      val drawingId = model.attributes.long("id").get
      Given(s"a drawing with id $drawingId")

      When("creating 5 shapes")
      val shapes = (-5 to -1).map (i => i -> LineShape(i, 0, 0, i))
      val actions = shapes.map(t => CreateShape(t._1, t._2))
      actions.foreach(model.execute(_))
      while(c1.isSynchronising){ Thread.sleep(100); }

      Then("the total number of shapes should be 5")
      model.model.shapes.size should equal(5)

      Then("all id's should be positive")
      model.model.shapes.exists(_._1 < 0) should equal(false)

      Then("the connection should still be active")
      c1.isOnline should equal (true)

      Given("a new controller")
      val model2 = new ActionModel {}
      model2.setAttribute("id", drawingId)
      val c2 = new RemoteController(model2, gateway, 50)

      When("synchronising with the server")
      c2.init()

      Then("the new drawing should be the same as the old")
      model.model.shapes should equal(model2.model.shapes)

      When("moving two of the shapes around")
      val newShapes = model.model.shapes.toList
      model.execute(TransformShapes(newShapes.take(2).map(_._1), TransformationMatrix(Vector2D(100, 0))))

      When("deleting a shape")
      val d = newShapes(2)
      model.execute(DeleteShape(d._1, d._2))

      When("Adding three shapes")
      val threeShapes = (-8 to -6).map(i => i -> LineShape(i, 0, 0, i)).toMap
      val threeAction = CreateShapes(threeShapes)
      model.execute(threeAction)

      Then("The total number of shapes should be 7")
      model.model.shapes.size should equal(7)

      When("synchronising with the old model with the server")
      while (c1.isSynchronising) { Thread.sleep(50)}

      Then("all id's should be positive")
      model.model.shapes.exists(_._1 < 0) should equal(false)

      When("synchronising the new model with the server")
      Thread.sleep(1000)
      while(c2.isSynchronising) { Thread.sleep(200) }

      Then("the shapes of the new model should be the same as in the old model")
      model2.model.shapes should equal(model.model.shapes)

      c1.exit()
      c2.exit()
    }
    
    val target = 500
    it (s"can execute $target actions serialized") {

      val model = new ActionModel {}
      val c1 = new RemoteController(model, gateway, 50)
      c1.init()
      model.addRemoteListener(c1.sendActionToServer)
      val drawingId = model.attributes.long("id").get
      Given(s"a drawing with id $drawingId")

      When(s"creating $target shapes")
      val shapes = (-1*target to -1).map (i => i -> LineShape(i, 0, 0, i))
      val actions = shapes.map(t => CreateShape(t._1, t._2))
      actions.foreach(model.execute(_))

      // Wait for the model to be synchronised
      Thread.sleep(50000)
      while (c1.isSynchronising) {
        Thread.sleep(500)
      }

      Then(s"the total number of shapes should be $target")
      model.model.shapes.size should equal(target)

      Then("all id's should be positive")
      model.model.shapes.exists(_._1 < 0) should equal(false)

      Then("It's sequential")
      model.model.shapes.keys.toList.sorted.reduceLeft((a, b) => {
        a should equal (b - 1)
        b
      })

      c1.exit()
    }

    it("Handles key uniqeness")        {
      val target =100
      val model = new ActionModel {}
      val c1 = new RemoteController(model, gateway, 1)
      c1.init()
      model.addRemoteListener(c1.sendActionToServer)
      val drawingId = model.attributes.long("id").get
      Given(s"a drawing with id $drawingId")
      When(s"making a range of $target internal ids")
      val ids = Seq.range(-target-1,-1)
      val remoteIDMap = c1.mapRemoteIDs(ids,c1.session)
      Then (s"remoteIDMap should contain all ids id's")
      ids.size should equal(remoteIDMap.size)
      println(ids+"\n"+remoteIDMap)
      When(s"when you now make $target new id's then you should get ${target*2} out")
      val remoteIDMap2 = c1.mapRemoteIDs(ids,c1.session)
      println(ids+"\n"+remoteIDMap2)
      var keys = remoteIDMap.keys
      keys ++= remoteIDMap2.keys
      val setKeys= keys.toSet
      Then (s" all keys should be uniqe")
      setKeys.size equals keys.size
      c1.exit()
    }
  }

}
