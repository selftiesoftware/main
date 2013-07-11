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
import com.siigna.app.model.server.User
import com.siigna.app.model.Model
import scala.util.Random
import com.siigna.app.model.action.{CreateShape, RemoteAction}
import com.siigna.app.model.shape.CircleShape
import com.siigna.util.geom.Vector2D
import com.siigna.util.collection.Attributes

class ServerSpec extends FunSpec with ShouldMatchers with GivenWhenThen {

  val port = 20005
  val address = "http://62.243.118.234"
  //val address = "http://localhost"
  val fqn = address+":"+port
  val ses = Session(4L,User(1L,"John","Johnskey"))

  def client = new Client(fqn)

  describe("Siigna communication Client"){

    it("Can determine if a server is alive"){

      val start = System.currentTimeMillis()
      assert(client.alive)

      val time = System.currentTimeMillis() - start
      Then("It took "+time)
    }

    it("Can get a new drawingId"){

      val start = System.currentTimeMillis()
      Given("Two request for ids")
      val firstId = client.getDrawingId(ses)
      val secondId = client.getDrawingId(ses)

      Then("The latter is exactly one higher then the former")
      assert(secondId-firstId==1)
      val time = System.currentTimeMillis() - start
      Then("It took "+time)
    }

    it("Can get a new drawing"){

      val start = System.currentTimeMillis()
      client.getDrawing(ses) match {
        case Some(m: Model) => {

          val firstId = m.attributes.get("id").get.asInstanceOf[Long]

          client.getDrawing(ses) match {
            case Some(m: Model) => assert(m.attributes.get("id").get.asInstanceOf[Long] - firstId == 1)

            case None => assert(false)
          }
        }
        case None => assert(false)
      }
      val time = System.currentTimeMillis() - start
      Then("It took "+time)
    }

    it("Can get an existing drawing"){
      val start = System.currentTimeMillis()
      val id = client.getDrawingId(ses)

      val drawing = client.getDrawing(id,ses)

      assert(drawing.get.attributes.get("id").get.asInstanceOf[Long] == id)
      val time = System.currentTimeMillis() - start
      Then("It took "+time)
    }

    it("Can get a range of shapes"){

      val start = System.currentTimeMillis()

      val amount = Random.nextInt(10)

      client.getShapeIds(amount,ses) match {
        case Some(r: Range) => r.length == amount
        case _ => assert(false)
      }
      val time = System.currentTimeMillis() - start
      Then("It took "+time)

    }

    it("Can set an action"){

      val start = System.currentTimeMillis()

      val drawingId = client.getDrawingId(ses)

      Given("A session with this drawing id "+drawingId)
      val ses2 = Session(drawingId,ses.user)

      Given("A remoteAction")
      val dummyAction = new RemoteAction(CreateShape(2, CircleShape(new Vector2D(100,100),20,Attributes())))

      val count1 = client.setAction(dummyAction,ses2)
      val count2 = client.setAction(dummyAction,ses2)

      assert(count2-count1==1)
      val time = System.currentTimeMillis() - start
      Then("It took "+time)
    }

    it("Can get a specific action"){

      Given("A new drawing id")
      val start = System.currentTimeMillis()

      val drawingId = client.getDrawingId(ses)

      Given("A session with this drawing id "+drawingId)
      val ses2 = Session(drawingId,ses.user)

      Given("A remoteAction")
      val dummyAction = new RemoteAction(CreateShape(2, CircleShape(new Vector2D(100,100),20,Attributes())))

      When("We set an action on this drawing")
      client.setAction(dummyAction,ses2)

      Then("We should be able to retrieve the same action (must be id 1)")
      client.getAction(1,ses2) match {
        case Some(r: RemoteAction) =>
        case _ => assert(false)
      }
      val time = System.currentTimeMillis() - start
      Then("It took "+time)

    }

    it("Can get the next actionId for a drawing"){
      Given("A new drawing id")

      val start = System.currentTimeMillis()

      val drawingId = client.getDrawingId(ses)

      Given("A session with this drawing id "+drawingId)
      val ses2 = Session(drawingId,ses.user)

      Given("A remoteAction")
      val dummyAction = new RemoteAction(CreateShape(2, CircleShape(new Vector2D(100,100),20,Attributes())))

      When("We ask for the next actionId")
      val firstActionId = client.getActionId(ses2)

      Then("initially then it must be 0")
      assert(firstActionId==0)

      When("We set an action on the drawing")
      client.setAction(dummyAction,ses2)

      Then("The action id must be 1")
      val secondActionId = client.getActionId(ses2)
      assert(secondActionId==1)

      val time = System.currentTimeMillis() - start
      Then("It took "+time)
    }
  }

}
