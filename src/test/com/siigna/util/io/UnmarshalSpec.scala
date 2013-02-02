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

    it ("can read with the right version") {
      val a = Marshal(null)
      val in = Unmarshal.getInputStream(ByteBuffer.wrap(a))
      in.version should equal(IOVersion(IOVersion.Current))
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
      val d = 14.78123d
      val a = Marshal(d)
      Unmarshal[Double](a) should equal(Some(d))
    }

    it ("can recognize subtypes") {
      val a = Marshal(14L)
      Unmarshal[AnyVal](a) should equal(Some(14L))
    }

    it ("can unmarshal a traversable to the correct type") {
      val i : Traversable[Int] = Traversable(314213, 123, 3124, 42)
      val x = Marshal(i)
      val o = Unmarshal[Traversable[Int]](x)
      o.get should equal(i)
    }

    it ("can unmarshal a map to the correct type") {
      val m : Map[Int, String] = Map(3123 -> "Hello world!", -9137 -> "Hello world! ÆØÅ - §!\"#¤%&/()=? _ @£$€{[]} | ^\\ - ça va?")
      val x = Marshal(m)
      val o = Unmarshal[Map[Int, String]](x)
      o.get should equal(m)
    }

  }

}
