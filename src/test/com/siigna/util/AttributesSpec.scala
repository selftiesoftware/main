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

package com.siigna.util

import collection.Attributes
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests the Attributes class
 */
class AttributesSpec extends FunSpec with ShouldMatchers {

  val populatedAttributes = Attributes("Color" -> "#4568AB")

  describe("Attributes") {

    it ("can be serialized and de-serialized") {
      {
        import java.io._
        val b = new ByteArrayOutputStream()
        val o = new ObjectOutputStream(b)
        o.writeObject(populatedAttributes)
        o.flush()
        val bytes = b.toByteArray
        val bi = new ByteArrayInputStream(bytes)
        val oi = new ObjectInputStream(bi)
        oi.readObject() should equal (populatedAttributes)
      }
    }

  }

}
