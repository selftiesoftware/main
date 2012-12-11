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

package com.siigna.util

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import com.siigna._
import app.model.action.{CreateShape, RemoteAction}
import app.model.{Model, RemoteModel}
import java.awt.Color

/**
 * Tests the Serializer.
 */
class SerializerSpec extends FunSpec with ShouldMatchers {

  private def in(o : Array[Byte]) = {
    val b = new ByteArrayInputStream(o)
    new ObjectInputStream(b).readObject()
  }

  private def out(o : Any) = {
    val b = new ByteArrayOutputStream()
    new ObjectOutputStream(b).writeObject(action)
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
      Serializer.writeDrawing(remoteModel) should equal (remoteBytes)
    }

    it ("can read a remote model from a byte array") {
      Serializer.readDrawing(remoteBytes) should equal (remoteModel)
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

  }

}
