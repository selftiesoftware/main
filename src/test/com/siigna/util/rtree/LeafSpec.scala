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
                  /*
package com.siigna.util.rtree

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, Spec}
import com.siigna.util.geom.SimpleRectangle2D

/**
 * A test for the Leaf clas.
 *
class LeafSpec extends FunSpec with ShouldMatchers {

  val elem1 = (10020, SimpleRectangle2D(15.23, 17.30, 89.3502, 123.273))
  val elem2 = (0, SimpleRectangle2D.empty)
  val elem3 = (Int.MaxValue, SimpleRectangle2D(0, 26, -15, -70000))

  describe("An empty leaf") {                        
    val leaf = new Leaf.EmptyLeaf(8, OrderMinX);

    it("has a branch factor and an ordering") {
      leaf.branchFactor should equal (8)
      leaf.ordering should equal (OrderMinX)
    }

    it("can add an element") {
      val l = leaf.add(elem1)
      l.size should equal (1)
    }

    it("can _not_ remove an element") {
      evaluating {
        leaf.remove(elem1._1)
      } should produce [UnsupportedOperationException]
    }
    
    it("can _not_ update an element") {
      evaluating {
        leaf.updated(elem1)
      } should produce [UnsupportedOperationException]
    }

    it("can search for a given MBR") {
      leaf(elem1._2) should equal (Traversable.empty[String])
    }

    it("can find the optimal MBR") {
      leaf.isBetter(elem1._2) should equal (true)
      leaf.isBetter(elem2._2) should equal (true)
    }
    
    it("can find the worst MBR") {
      leaf.worst should equal (SimpleRectangle2D.empty)
    }
    
    it("has an empty MBR") {
      leaf.mbr should equal (SimpleRectangle2D.empty)
    }
    
    it("has a size of 0") {
      leaf.size should equal (0)
    }

  }

  describe("A leaf with one element") {

    val leaf1 = new Leaf.Leaf1(elem1._1, elem1._2, 8, OrderMinX);
    val leaf2 = new Leaf.Leaf1(elem2._1, elem2._2, 8, OrderMinY);

    it("has a branch factor and an ordering") {
      leaf1.branchFactor should equal (8)
      leaf1.ordering should equal (OrderMinX)
      leaf2.ordering should equal (OrderMinY)
    }

    it("can add an element") {
      val l = leaf1.add(elem2)
      l.size should equal (2)
    }

    it("can remove an element") {
      leaf1.remove(elem1._1).isInstanceOf[Leaf.EmptyLeaf] should equal(true)
    }

    it("can update an element") {
      val l = leaf1.updated((elem1._1, elem2._2))
      l.apply(elem2._2) should equal (Traversable(elem1._1))
    }

    it("can search for a given MBR") {
      leaf1(elem1._2) should equal (Traversable(elem1._1))
    }

    it("can find the optimal MBR") {
      leaf1.isBetter(elem1._2) should equal (false)
      leaf1.isBetter(elem2._2) should equal (false)
      leaf2.isBetter(elem1._2) should equal (true)
    }

    it("can find the worst MBR") {
      leaf1.worst should equal (elem1._2)
      leaf2.worst should equal (SimpleRectangle2D.empty)
    }

    it("has an MBR") {
      leaf1.mbr should equal (elem1._2)
      leaf2.mbr should equal (SimpleRectangle2D.empty)
    }

    it("has a size of 1") {
      leaf1.size should equal (1)
      leaf2.size should equal (1)
    }
  }

  describe("A leaf with two elements") {

      val leaf1 = new Leaf.Leaf2(elem1._1, elem1._2, elem3._1, elem3._2, 8, OrderMinX);
      val leaf2 = new Leaf.Leaf2(elem2._1, elem2._2, elem3._1, elem3._2, 8, OrderMinY);

      it("has a branch factor and an ordering") {
        leaf1.branchFactor should equal (8)
        leaf1.ordering should equal (OrderMinX)
        leaf2.ordering should equal (OrderMinY)
      }

      it("can add an element") {
        val l = leaf1.add(elem2)
        l.size should equal (3)
      }

      it("can remove an element") {
        leaf1.remove(elem1._1).isInstanceOf[Leaf.Leaf1] should equal(true)
        leaf1.remove(elem3._1).isInstanceOf[Leaf.Leaf1] should equal(true)
      }

      it("can update an element") {
        val l = leaf1.updated((elem1._1, elem2._2))
        l.apply(elem2._2) should equal (Traversable(elem1._1, elem3._1))
      }

      it("can search for a given MBR") {
        leaf1(elem1._2) should equal (Traversable(elem1._1))
        leaf1(elem3._2) should equal (Traversable(elem3._1))
      }

      it("can find the optimal MBR") {
        leaf1.isBetter(elem1._2) should equal (false)
        leaf1.isBetter(elem2._2) should equal (false)
        leaf2.isBetter(elem1._2) should equal (true)
        leaf2.isBetter(elem3._2) should equal (true)
      }

      it("can find the worst MBR") {
        leaf1.worst should equal (elem1._2)
        leaf2.worst should equal (SimpleRectangle2D.empty)
      }

      it("has an MBR") {
        leaf1.mbr should equal (elem1._2.expand(elem3._2))
        leaf2.mbr should equal (elem3._2)
      }

      it("has a size of 2") {
        leaf1.size should equal (2)
        leaf2.size should equal (2)
      }
    }
  
}
 */