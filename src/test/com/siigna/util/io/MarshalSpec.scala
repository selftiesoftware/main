package com.siigna.util.io

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import version.IOVersion
import org.ubjson.io.ByteArrayOutputStream

/**
 * Tests the marshal object.
 */
class MarshalSpec extends FunSpec with ShouldMatchers {

  describe("Marshal") {
    it ("can write a version") {
      val a = Marshal(null)
      a.slice(10, 11) should equal(Array[Byte](IOVersion.Current))
    }

    it ("can marshal an array") {
      Marshal(Array[Int](1, 4, 9, -132))
    }

    it ("can write a siigna object") {
      import com.siigna.app.model.shape.LineShape
      Marshal(LineShape(0, 0, 10, 10))
    }

    it ("can fail when given an unknown object") {
      evaluating {
        Marshal(new ByteArrayOutputStream())
      } should produce[IllegalArgumentException]
    }

  }

}
