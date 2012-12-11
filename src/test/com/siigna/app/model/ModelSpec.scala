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

package com.siigna.app.model

import org.scalatest.matchers.ShouldMatchers
import collection.parallel.immutable.ParHashMap
import org.scalatest.FunSpec
import com.siigna._

/**
 * Test the model
 */
class ModelSpec extends FunSpec with ShouldMatchers {

  val model = new Model(Map(), Seq(), Seq())
  val populatedModel = new Model(Map(-1 -> LineShape(0, 0, 12, math.Pi)), Nil, Nil)

  private def marshalUnmarshal(model : Model) = {
    {
      import java.io._
      val b = new ByteArrayOutputStream()
      val o = new ObjectOutputStream(b)
      o.writeObject(model)
      o.flush()
      val bytes = b.toByteArray
      val bi = new ByteArrayInputStream(bytes)
      val oi = new ObjectInputStream(bi)
      oi.readObject().asInstanceOf[Model]
    }
  }

  describe("An empty model") {

    it ("can be serialized and de-serialized") {
      marshalUnmarshal(model) should equal (model)
    }

  }

  describe("A empty model") {

    it ("can be serialized and de-serialized") {
      marshalUnmarshal(populatedModel) should equal (populatedModel)
    }

  }
}