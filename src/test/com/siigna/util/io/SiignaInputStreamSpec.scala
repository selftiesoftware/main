package com.siigna.util.io

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import org.ubjson.io.{ByteArrayInputStream, ByteArrayOutputStream}
import version.IOVersion

/**
 * Tests the SiignaInputStream class.
 */
class SiignaInputStreamSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  var out : SiignaOutputStream = null
  var in : SiignaInputStream = null

  before {
    val arr = new ByteArrayOutputStream()
    out = new SiignaOutputStream(arr, IOVersion(IOVersion.Current))
    in = new SiignaInputStream(new ByteArrayInputStream(arr.getArray), IOVersion(IOVersion.Current))
  }

  describe("SiignaInputStream") {

    it ("can read a null") {
      out.writeNull()
      in.readObject[Null]
    }

    it ("can read a byte") {
      out.writeByte(16)
      in.readObject[Byte] should equal (16)
    }

    it ("can read a double") {
      val d = 123913.21832d
      out.writeDouble(d)
      in.readObject[Double] should equal(123913.21832d)
    }

    it ("can read a boolean") {
      out.writeBoolean(true)
      out.writeBoolean(false)
      in.readObject[Boolean] should equal(true)
      in.readObject[Boolean] should equal(false)
    }

    it ("can read a float") {
      val f : Float = 13.2919321f
      out.writeFloat(f)
      in.readObject[Float] should equal(f)
    }

    it ("can read an int") {
      val i = -1234
      out.writeObject(i)
      in.readObject[Int] should equal(i)
    }

    it ("can write a long") {
      val l = 123656781923L
      out.writeObject(l)
      in.readObject[Long] should equal(l)
    }

    it ("can write a string") {
      val s = "Hello world! ÆØÅ - §!\"#¤%&/()=? _ @£$€{[]} | ^\\ - ça va?"
      out.writeObject(s)
      in.readObject[String] should equal(s)
    }

  }

}
