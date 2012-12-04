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

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import shape.LineShape
import java.awt.Color
import com.siigna.util.collection.Attributes

/**
 * Tests the remote model.
 */
class RemoteModelSpec extends FunSpec with ShouldMatchers {

  val attributes = Attributes("Color" -> Color.orange)
  val immutableModel = new Model(Map(-1 -> LineShape(0, 0, 12, math.Pi)), Nil, Nil)
  val model = new RemoteModel(immutableModel, attributes)

  describe ("The Remote Model") {

    it ("can be serialized and de-serialized with the same informations") {
      {
        import java.io._
        val b = new ByteArrayOutputStream()
        val o = new ObjectOutputStream(b)
        o.writeObject(model)
        o.flush()
        val bytes = b.toByteArray
        val bi = new ByteArrayInputStream(bytes)
        val oi = new ObjectInputStream(bi)
        val remote = new RemoteModel(new Model(Map(), Nil, Nil), Attributes())
        remote.readExternal(oi)
        remote should equal (model)
      }
    }

  }

}
