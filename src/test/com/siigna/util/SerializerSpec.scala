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
/*
package com.siigna.util

import org.scalatest.{GivenWhenThen, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import java.io._
import com.siigna._
import app.model.action.{CreateShape, RemoteAction}
import app.model.shape.CircleShape
import app.model.{Model}
import java.awt.Color
import scala.util.Random
import scala.Some

/**
 * Tests the Serializer.
 */
class SerializerSpec extends FunSpec with ShouldMatchers with GivenWhenThen {

  private def in(o : Array[Byte]) = {
    val b = new ByteArrayInputStream(o)
    new ObjectInputStream(b).readObject()
  }

  private def out(o : Any) = {
    val b = new ByteArrayOutputStream()
    new ObjectOutputStream(b).writeObject(o)
    b.toByteArray
  }

  private def out(o : Externalizable) = {
    val b = new ByteArrayOutputStream()
    o.writeExternal(new ObjectOutputStream(b))
    b.toByteArray
  }

  val attributes = Attributes("Color" -> Color.red)
  val shape      = LineShape(0, 0, 12, math.Pi)
  val shapes     = Map(-1 -> shape)
  val action       = RemoteAction(new CreateShape(-1, shape))
  val actionBytes  = out(action)
  val model        = new Model(shapes, Nil, Nil)
  val remoteModel  = new RemoteModel(model, attributes)
  val remoteBytes  = out(remoteModel)

  describe("The Serializer") {
    it ("can write a remote action to a byte array") {
      Serializer.writeAction(action) should equal(actionBytes)
    }

    it ("can read a remote action from a byte array") {
      Serializer.readAction(actionBytes) should equal (action)
    }

    it ("can fail when serializing a null action") {
      evaluating (
        Serializer.writeAction(null)
      ) should produce[NullPointerException]
    }

    it ("can fail when reading a faulty byte array") {
      evaluating {
        Serializer.readAction(null)
      } should produce[NullPointerException]
    }

    it ("can write a remote model to a byte array") {
      val b1 = Serializer.writeDrawing(remoteModel)
      b1 should equal (remoteBytes)
    }

    it ("can read a remote model from a byte array") {
      val d = Serializer.readDrawing(remoteBytes)

      d.model should equal (remoteModel.model)
      d.attributes should equal (remoteModel.attributes)
    }

    it ("can fail when serializing a null model") {
      evaluating (
        Serializer.writeDrawing(null)
      ) should produce[NullPointerException]
    }

    it ("can fail when reading a null model") {
      evaluating {
        Serializer.readDrawing(null)
      } should produce[NullPointerException]
    }

    it("Should be able to marshal and unmarshal a drawing with attributes"){

      given("A new RemoteModel")
      val rmodel = new RemoteModel()

      given("A random attribute name")
      val attrName = Random.nextString(20)

      given("A random attribute value")
      val attrVal = Random.nextInt

      when("We set some random attribute on it")
      rmodel.setAttribute(attrName,attrVal)

      when("We marshal it")
      val data = Serializer.writeDrawing(rmodel)

      when("we unmarshal it")
      val drawing = Serializer.readDrawing(data)

      then("The same attributes should be set")
      drawing.attributes.get(attrName) match {

        case None => assert(false)

        case Some(attrVal) => assert(true)

        case _ => assert(false)
      }
    }

    it("Should be able to marshal and unmarshal a drawing with actions performed"){

      given("A new remote model")
      val rmodel = new RemoteModel()

      given("A create action")
      val action = CreateShape(1, CircleShape(new Vector2D(0,0), new Vector2D(0,0)))

      when("We execute the action on the remote model's model")
      val newModel = action execute rmodel.model

      then("It should contain a shape")
      assert(newModel.shapes.get(1) != None)
    }

  }

}*/
