/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.dxf

import scala.actors.Debug

import scala.collection.generic.{Addable, Subtractable}

import com.siigna.app.model.shape._
import com.siigna.util.geom.{Vector2D, Vector}

/**
 * A DXF section, represented by list of DXFValues.
 */
case class DXFSection(values : Seq[DXFValue]) extends Addable[DXFValue, DXFSection] with Subtractable[DXFValue, DXFSection] {

  def +(value : DXFValue)     = DXFSection(values.:+(value))
  def +(section : DXFSection) = DXFSection(values ++ section.values)
  def -(value : DXFValue)     = DXFSection(values.filterNot(_ == value))

  def repr = this

  def toShape : Option[Shape] = {
    try {
      values.head match {
        // Arc
        case DXFValue(0, "ARC") => {
          var start, end, centerX, centerY, r : Option[Double] = None
          values.foreach((value : DXFValue) => value match {
            case DXFValue(10, _) => centerX = value.toDouble
            case DXFValue(20, _) => centerY = value.toDouble
            case DXFValue(40, _) => r = value.toDouble
            case DXFValue(50, _) => start = value.toDouble
            case DXFValue(51, _) => end = value.toDouble
            case _ =>
          })
          if (start.isDefined && end.isDefined && centerX.isDefined && centerY.isDefined && r.isDefined) {
            if (r.get > 0 && start.get != end.get)
              Some(ArcShape(Vector(centerX.get, centerY.get), r.get, end.get, start.get)) // DXF counts CW, not CCW
            else None
          } else None
        }
        // Circle
        case DXFValue(0, "CIRCLE") => {
          var x, y, r : Option[Double] = None
          values.foreach((value : DXFValue) => value match {
            case DXFValue(10, _) => x = value.toDouble
            case DXFValue(20, _) => y = value.toDouble
            case DXFValue(40, _) => r = value.toDouble
            case _ =>
          })

          if (x.isDefined && y.isDefined && r.isDefined) {
            Some(CircleShape(Vector(x.get, y.get), Vector(x.get + r.get, y.get)))
          } else None
        }
        // RHINO Polylines
        case DXFValue(0, "POLYLINE") => {
          val newValues = values.updated(0, DXFValue(0, "LWPOLYLINE"))
          val (l1, l)  = newValues.splitAt(newValues.indexOf(DXFValue(10, "0.0")))
          val (l2, l3) = l.splitAt(newValues.indexOf(DXFValue(10, "0.0")))
          DXFSection(l1 ++ (l2 drop 1) ++ (l3 drop 1)).toShape
        }
        // Polylines
        case DXFValue(0, "LWPOLYLINE") => {
          var points = List[Vector2D]()
          var x, y : Option[Double] = None
          values.foreach((value : DXFValue) => value match {
            case DXFValue(10, _) => x = value.toDouble
            case DXFValue(20, _) => {
              y = value.toDouble

              if (x.isDefined && y.isDefined) {
                points = points :+ Vector(x.get, y.get)
                x = None
                y = None
              }
            }
            case _ =>
          })
          if (points.length > 1)
            Some(PolylineShape.fromPoints(points))
          else
            None
        }
        // Other stuff...
        case _ => None
      }
    } catch {
      case e => {
        Debug.warning("DXFSection: Failed to parse section to Shape. Returned error: "+e)
        None
      }
    }
  }

  override def toString = values.mkString

}

/**
 * The companion object to DXFValue providing utility functions to create DXFValues from
 * various objects.
 */
object DXFSection {

  def apply(value : DXFValue) : DXFSection = new DXFSection(List(value))
  def apply(value1 : DXFValue, value2 : DXFValue, values : DXFValue*) : DXFSection = DXFSection(value1) + value2 ++ values

  /**
   * Create a DXFSection from a Vector2D.
   */
  def fromVector(vector : Vector2D) = {
    DXFSection(DXFValue(100, "AcDbPoint"), DXFValue(10, vector.x), DXFValue(20, vector.y))
  }
}
