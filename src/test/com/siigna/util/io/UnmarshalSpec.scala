package com.siigna.util.io

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import version.IOVersion
import java.nio.ByteBuffer

/**
 * Tests the marshal object.
 */
class UnmarshalSpec extends FunSpec with ShouldMatchers {

  describe("Unmarshal") {

    it ("can get an input stream from a byte buffer") {
      val a = Marshal(null)
      val r = Unmarshal.getInputStream(ByteBuffer.wrap(a))
      evaluating { r } should produce[SiignaInputStream]
    }

    it ("can read with the right version") {
      val a = Marshal(null)
      val in = Unmarshal.getInputStream(ByteBuffer.wrap(a))
      in.version should equal(IOVersion.Current)
    }

    it ("can read a type") {
      val a = Marshal(14L)
      Unmarshal[Long](a) should equal (Some(14L))
    }

    it ("can fail when not given a type parameter") {
      evaluating {
        Unmarshal(Array[Byte]())
      } should produce [IllegalArgumentException]
    }

    it ("can return None when type does not match") {
      val a = Marshal(14L)
      Unmarshal[String](a) should equal(None)
    }

    it ("can unmarshal java-types to scala-types") {
      val a = Marshal(14.78123d)
      Unmarshal[Double](a) should equal(Some(a))
    }

    it ("can recognize subtypes") {
      val a = Marshal(14L)
      Unmarshal[AnyVal](a) should equal(Some(a))
    }

  }

}
