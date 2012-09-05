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

package com.siigna.app.model.action.serialization

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io._
import com.siigna.app.model.shape.LineShape
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.model.action.{Create, TransformShapeParts, TransformShape}
import com.siigna.app.model.Drawing

/**
 * Tests whether the TransformShapePartsProxy class does its job.
 */
class TransformShapePartsProxySpec extends FunSpec with ShouldMatchers {

  def writeToByteArray(o : Object) = {
    val byteOut = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(byteOut)
    out.writeObject(o)
    out.flush()
    byteOut.toByteArray
  }
  
  def readFromByteArray(arr : Array[Byte]) : Object = {
    val byteIn = new ByteArrayInputStream(arr)
    val in = new ObjectInputStream(byteIn)
    in.readObject()
  }
  
  describe("TransformShapePartsProxy") {

    val shape  = LineShape(Vector2D(0, 0), Vector2D(10, 10)); Create(shape)
    val part   = shape.getPart(Vector2D(0, 0))
    val t      = TransformationMatrix(Vector2D(10, 0), 1)
    val action = TransformShapeParts(Map(Drawing.shapes.head._1 -> part), t)

    it("can be deserialized into a TransformShapeParts") {
      val proxy = new TransformShapePartsProxy(Map(Drawing.shapes.head._1 -> part), t)
      val bytes = writeToByteArray(proxy)
      val result = readFromByteArray(bytes)
      
      result.asInstanceOf[TransformShapeParts] should equal(action)
    }
    
    it("can be serialized and deserialized from TransformShapeParts") {
      val bytes = writeToByteArray(action)      
      val result = readFromByteArray(bytes)

      result should equal(action)
    }

  }

}
