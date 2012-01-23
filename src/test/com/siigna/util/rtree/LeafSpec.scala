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

package com.siigna.util.rtree

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec}
import com.siigna.util.geom.Rectangle2D

/**
 * A test for the Leaf clas.
 */
class LeafSpec extends Spec with ShouldMatchers {

  val elem1 = ("a81958d8-0664-4fac-be9c-bc59c44fb47b", Rectangle2D(15.23, 17.30, 89.3502, 123.273))
  val elem2 = ("c0dd2d18-96ad-439b-b5da-cca31d59823c", Rectangle2D.empty)

  describe("An empty leaf") {                        
    val leaf = new Leaf.EmptyLeaf(8, OrderMinX);

    describe("- has a branch factor and an ordering") {
      leaf.branchFactor should equal (8)
      leaf.ordering should equal (OrderMinX)
    }

    describe("- can add an element") {
      val l = leaf.add(elem1)
      l.size should equal (1)
    }

    describe("- can _not_ remove an element") {
      evaluating {
        leaf.remove(elem1._1)
      } should produce [UnsupportedOperationException]
    }
    
    describe("- can _not_ update an element") {
      evaluating {
        leaf.updated(elem1)
      } should produce [UnsupportedOperationException]
    }

    describe("- can search for a given MBR") {
      leaf(elem1._2) should equal (Traversable.empty[String])
    }

    describe("- can find the optimal MBR") {
      leaf.isBetter(elem1._2) should equal (true)
      leaf.isBetter(elem2._2) should equal (true)
    }
    
    describe("- can find the worst MBR") {
      leaf.worst should equal (Rectangle2D.empty)
    }
    
    describe("- has an empty MBR") {
      leaf.mbr should equal (Rectangle2D.empty)
    }
    
    describe("- has a size of 0") {
      leaf.size should equal (0)
    }

  }

  describe("A leaf with one element") {

    val leaf1 = new Leaf.Leaf1(elem1._1, elem1._2, 8, OrderMinX);
    val leaf2 = new Leaf.Leaf1(elem2._1, elem2._2, 8, OrderMinY);

    describe("- has a branch factor and an ordering") {
      leaf1.branchFactor should equal (8)
      leaf1.ordering should equal (OrderMinX)
      leaf2.ordering should equal (OrderMinY)
    }

    describe("- can add an element") {
      val l = leaf1.add(elem2)
      l.size should equal (2)
    }

    describe("- can remove an element") {
      leaf1.remove(elem1._1) should be a[new Leaf.EmptyLeaf]()
    }

    describe("- can update an element") {
      val l = leaf1.updated((elem1._1, elem2._2))
      l.apply(elem2._2) should equal (Traversable(elem1._1))
    }

    describe("- can search for a given MBR") {
      leaf1(elem1._2) should equal (Traversable(elem1._1))
    }

    describe("- can find the optimal MBR") {
      leaf1.isBetter(elem1._2) should equal (false)
      leaf2.isBetter(elem2._2) should equal (true)
    }

    describe("- can find the worst MBR") {
      leaf1.worst should equal (elem1._2)
      leaf2.worst should equal (Rectangle2D.empty)
    }

    describe("- has an MBR") {
      leaf1.mbr should equal (elem1._2)
      leaf2.mbr should equal (Rectangle2D.empty)
    }

    describe("- has a size of 1") {
      leaf1.size should equal (1)
      leaf2.size should equal (1)
    }
  }
  
}