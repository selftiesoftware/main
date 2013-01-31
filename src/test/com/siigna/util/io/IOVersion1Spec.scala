package com.siigna.util.io

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}
import org.ubjson.io.{ByteArrayInputStream, ByteArrayOutputStream}
import version.{IOVersion1, IOVersion}

/**
 * Tests IO version 1.
 */
class IOVersion1Spec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  var arr : ByteArrayOutputStream = null
  var out : SiignaOutputStream = null
  var in : SiignaInputStream = null

  before {
    arr = new ByteArrayOutputStream()
    out = new SiignaOutputStream(arr, IOVersion1)
    in = new SiignaInputStream(new ByteArrayInputStream(arr.getArray), IOVersion1)
  }

  describe("IO version 1") {

    it ("can read an array") {
      val a = Array[Byte](13, 14, 15, 0, 14)
      out.writeObject(a)
      in.readObject._2 should equal(a)
    }

  }

}
