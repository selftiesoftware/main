package com.siigna.module.IO.dxf

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

import scala.actors.Debug

import scala.collection.generic.{Addable, Subtractable}

import com.siigna.app.model.shape._
import com.siigna.util.geom.{Vector2D, Vector}
//import scala.Option.get
//import com.siigna.module.base.file.fileformats.dxf.DXFValue

/**
 * A DXF section, represented by list of DXFValues.
 */
case class DXFSection(values: Seq[DXFValue]) extends Subtractable[DXFValue, DXFSection] {

  def +(value: DXFValue) = DXFSection(values.:+(value))

  def ++(xs: Traversable[DXFValue]) = DXFSection(values ++ xs)

  def +(section: DXFSection) = DXFSection(values ++ section.values)

  def -(value: DXFValue) = DXFSection(values.filterNot(_ == value))

  def repr = this

  def toShape: Option[ShapeLike] = {
    try {
      values.head match {
        // Arc
        case DXFValue(0, "ARC") => {
          var start, end, centerX, centerY, r: Option[Double] = None
          var width: Option[Double] = None

          values.foreach((value: DXFValue) => value match {
            case DXFValue(10, _) => centerX = value.toDouble
            case DXFValue(20, _) => centerY = value.toDouble
            case DXFValue(40, _) => r = value.toDouble
            case DXFValue(50, _) => start = value.toDouble
            case DXFValue(51, _) => end = value.toDouble
            case DXFValue(370, _) => width = Some(value.toDouble.get / 100)
            case _ =>
          })
          if (start.isDefined && end.isDefined && centerX.isDefined && centerY.isDefined && r.isDefined) {
            if (r.get > 0 && start.get != end.get)
            // DXF counts CW, not CCW
              Some(ArcShape(Vector2D(centerX.get, centerY.get), r.get, end.get, start.get).setAttributes("StrokeWidth" -> width.getOrElse(0.5)))
            else None
          } else None
        }
        // Circle
        case DXFValue(0, "CIRCLE") => {
          var width: Option[Double] = None
          var x, y, r: Option[Double] = None
          values.foreach((value: DXFValue) => value match {
            case DXFValue(10, _) => x = value.toDouble
            case DXFValue(20, _) => y = value.toDouble
            case DXFValue(40, _) => r = value.toDouble
            case DXFValue(370, _) => width = Some(value.toDouble.get / 100)
            case _ =>
          })

          if (x.isDefined && y.isDefined && r.isDefined) {
            Some(CircleShape(Vector2D(x.get, y.get), Vector2D(x.get + r.get, y.get)).setAttributes("StrokeWidth" -> width.getOrElse(0.5)))
          } else None
        }
        // RHINO Polylines
        case DXFValue(0, "POLYLINE") => {
          val newValues = values.updated(0, DXFValue(0, "LWPOLYLINE"))
          val (l1, l) = newValues.splitAt(newValues.indexOf(DXFValue(10, "0.0")))
          val (l2, l3) = l.splitAt(newValues.indexOf(DXFValue(10, "0.0")))
          DXFSection(l1 ++ (l2 drop 1) ++ (l3 drop 1)).toShape
        }
        // Polylines
        case DXFValue(0, "LWPOLYLINE") => {
          var points = List[Vector2D]()
          var width: Option[Double] = None
          var x, y: Option[Double] = None
          values.foreach((value: DXFValue) => value match {
            case DXFValue(10, _) => x = value.toDouble
            case DXFValue(20, _) => {
              y = value.toDouble

              if (x.isDefined && y.isDefined) {
                points = points :+ Vector(x.get, y.get)
                x = None
                y = None
              }
            }
            case DXFValue(370, _) => width = Some(value.toDouble.get / 100)
            case _ =>
          })
          if (points.length > 1) {
            Some(PolylineShape(points).setAttributes("StrokeWidth" -> width.getOrElse(0.5)))
          } else None
        }
        //(Multiline?) text
        case DXFValue(0, "MTEXT") => {
          var x, y, h, w: Option[Double] = None
          var text: String = ""

          values.foreach((value: DXFValue) => value match {
            case DXFValue(10, _) => x = value.toDouble
            case DXFValue(20, _) => y = value.toDouble
            case DXFValue(40, _) => h = value.toDouble
            case DXFValue(41, _) => w = value.toDouble
            case DXFValue(1, string: String) => text = string
            case _ =>
          })
          if (!text.isEmpty) {
            Some(TextShape(text, (Vector2D(x.get, y.get)), h.get))
          } else None
        }
        // Add support for additional import types here:

        case _ => None
      }
    } catch {
      case e => {
        Debug.warning("DXFSection: Failed to parse section to ShapeLike. Returned error: " + e)
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

  def apply(value: DXFValue): DXFSection = new DXFSection(List(value))

  def apply(value1: DXFValue, value2: DXFValue, values: DXFValue*): DXFSection = DXFSection(value1) + value2 ++ values

  /**
   * Create a DXFSection from a Vector2D.
   */
  def fromVector(vector: Vector2D) = {
    DXFSection(DXFValue(100, "AcDbPoint"), DXFValue(10, vector.x), DXFValue(20, vector.y))
  }

  //****EXPORT FUNCTIONS ****
  //write selected shape to DXF:
  def toDXF(shape: Shape): DXFSection = {

    def vectorToDXF(v: Vector2D) = {
      Seq(DXFValue(10, v.x),
        DXFValue(20, v.y))
    }

    try {
      shape match {
        //export lineShapes
        case l: LineShape => {
          DXFSection(DXFValue(0, "LWPOLYLINE"),
            //random identifier number (HEX)
            DXFValue(5, (scala.util.Random.nextInt.toHexString)),
            DXFValue(100, "AcDbEntity"),
            //layer
            DXFValue(8, 0),
            DXFValue(100, "AcDbPolyline"),
            //width
            DXFValue(370, if (!shape.attributes.get("StrokeWidth").isEmpty) shape.attributes.get("StrokeWidth").get.toString.toDouble * 100 else 0),
            //number of points
            DXFValue(90, 2),
            DXFValue(70, 0),
            //LineWeight
            DXFValue(43, 0.0),
            //Points
            DXFValue(10, l.p1.x), DXFValue(20, l.p1.y), DXFValue(10, l.p2.x), DXFValue(20, l.p2.y))
        }
        case p: PolylineShape => {
          val vertices = p.geometry.vertices
          val numberOfVertices = vertices.size

          DXFSection(

            DXFValue(0, "LWPOLYLINE"),
            //random identifier number (HEX)
            DXFValue(5, (scala.util.Random.nextInt.toHexString)),
            DXFValue(100, "AcDbEntity"),
            //layer
            DXFValue(8, 0),
            DXFValue(100, "AcDbPolyline"),
            //width
            DXFValue(370, if (!shape.attributes.get("StrokeWidth").isEmpty) shape.attributes.get("StrokeWidth").get.toString.toDouble * 100 else 0),
            //number of points
            DXFValue(90, numberOfVertices),
            DXFValue(70, 0),
            //LineWeight
            DXFValue(43, 0.0)
            //Points
          ) ++ vertices.map(vectorToDXF).flatten
        }
        case c: CircleShape => {
          DXFSection(DXFValue(0, "CIRCLE"),
            //random identifier number (HEX)
            DXFValue(5, (scala.util.Random.nextInt.toHexString)),
            DXFValue(100, "AcDbEntity"),
            //layer
            DXFValue(8, 0),
            DXFValue(100, "AcDbCircle"),
            //width
            DXFValue(370, if (!shape.attributes.get("StrokeWidth").isEmpty) shape.attributes.get("StrokeWidth").get.toString.toDouble * 100 else 0),
            //number of points
            DXFValue(90, 2),
            DXFValue(70, 0),
            //LineWeight
            DXFValue(43, 0.0),
            //Center point
            DXFValue(20, c.center.x), DXFValue(30, c.center.y),
            //radius
            DXFValue(40, c.radius)
          )
        }
        case t: TextShape => {
          println(t.position.x)
          println(t.position.y)
          DXFSection(DXFValue(0, "MTEXT"),
            //random identifier number (HEX)
            DXFValue(5, (scala.util.Random.nextInt.toHexString)),
            DXFValue(100, "AcDbEntity"),
            //layer
            DXFValue(8, 0),
            DXFValue(100, "AcDbMText"),
            //placement
            DXFValue(10, t.position.x),
            DXFValue(20, t.position.y),
            DXFValue(30, 0.0),
            //text height
            DXFValue(40, t.scale / 1.2),
            //text rectangle width
            DXFValue(41, t.geometry.width),
            //attachment point
            DXFValue(71, 1),
            //drawing direction
            DXFValue(72, 1),
            //text string
            DXFValue(1, t.text)
          )
        }
      }
    }
  }
}