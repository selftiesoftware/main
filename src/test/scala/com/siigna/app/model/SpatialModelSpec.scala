/*
 * Copyright (c) 2013. Siigna is released under the creative common license by-nc-sa. You are free
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
import org.khelekore.prtree.{MBRConverter, PRTree}
import com.siigna.app.model.shape.{LineShape, CircleShape, Shape}
import com.siigna.util.geom.Vector2D
import scala.collection.JavaConversions._

/**
 * Tests the spatial model.
 */
class SpatialModelSpec extends FunSpec with ShouldMatchers {

  type T = (Int,Shape)

  def converter = new MBRConverter[T] {

    override def getDimensions = 2

    override def getMin(a:Int, x: T) = {
      if (a==0)
        x._2.geometry.boundary.xMin
      else
        x._2.geometry.boundary.yMin
    }

    override def getMax(a:Int,x: T) = {

      if (a==0)
        x._2.geometry.boundary.xMax
      else
        x._2.geometry.boundary.yMax

    }

  }

  def makeTree = {

    def shapes = Map(
      (1,LineShape(Vector2D(0,10),Vector2D(10,0))),
      (2,LineShape(Vector2D(0,20),Vector2D(20,0))),
      (3,LineShape(Vector2D(0,30),Vector2D(30,0))),
      (4,LineShape(Vector2D(0,40),Vector2D(40,0)))
    )

    val t = new PRTree[T](converter,2)

    t.load(asJavaCollection(shapes))
  }



  val tree = makeTree
  //val tree = makeTree[(Float,Float)]

  describe ("Spatial Model") {

    it ("can calculate the minimum-bounding rectangle") {

    }

    it("can query for shapes that are inside or intersecting the given boundary"){

    }

    it("can query for shapes close to the given point by a given radius"){

    }

  }

}
