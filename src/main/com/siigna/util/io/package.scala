package com.siigna.util

/**
 * The persistence package is capable of converting objects into byte arrays (marshalling), reading objects from
 * byte arrays (unmarshalling) and storing and reading content from disc.
 */
package object io extends Enumeration {

  /**
   * A Type object used to identify the type of a marshalled object.
   */
  object Type {
    // Remote package
    val Error   = 0.asInstanceOf[Byte]
    val Get     = 1.asInstanceOf[Byte]
    val Set     = 2.asInstanceOf[Byte]
    val Session = 3.asInstanceOf[Byte]
    val User    = 4.asInstanceOf[Byte]

    // Util
    val Attributes = 50.asInstanceOf[Byte]
    val TransformationMatrix = 51.asInstanceOf[Byte]
    val Vector2D   = 52.asInstanceOf[Byte]
    val Model      = 53.asInstanceOf[Byte]

    // Scala
    val Iterable = 80.asInstanceOf[Byte]
    val Map      = 81.asInstanceOf[Byte]

    // Actions
    val AddAttributes       = 100.asInstanceOf[Byte]
    val SetAttributes       = 101.asInstanceOf[Byte]
    val CreateShape         = 102.asInstanceOf[Byte]
    val CreateShapes        = 103.asInstanceOf[Byte]
    val DeleteShape         = 104.asInstanceOf[Byte]
    val DeleteShapes        = 105.asInstanceOf[Byte]
    val DeleteShapePart     = 106.asInstanceOf[Byte]
    val DeleteShapeParts    = 107.asInstanceOf[Byte]
    val RemoteAction        = 108.asInstanceOf[Byte]
    val SequenceAction      = 109.asInstanceOf[Byte]
    val TransformShape      = 110.asInstanceOf[Byte]
    val TransformShapeParts = 111.asInstanceOf[Byte]
    val TransformShapes     = 112.asInstanceOf[Byte]

    // Shapes
    val ArcShape      = 200.asInstanceOf[Byte]
    val CircleShape   = 201.asInstanceOf[Byte]
    val GroupShape    = 202.asInstanceOf[Byte]
    val ImageShape    = 203.asInstanceOf[Byte]
    val LineShape     = 204.asInstanceOf[Byte]
    val PolylineShapeClosed   = 205.asInstanceOf[Byte]
    val PolylineShapeOpen     = 206.asInstanceOf[Byte]
    val RectangleShapeSimple  = 207.asInstanceOf[Byte]
    val RectangleShapeComplex = 208.asInstanceOf[Byte]
    val TextShape     = 209.asInstanceOf[Byte]

    // Inner polyline shapes
    val PolylineLineShape = 220.asInstanceOf[Byte]
    val PolylineArcShape  = 221.asInstanceOf[Byte]

    // Shape parts
    val ArcShapePart       = 230.asInstanceOf[Byte]
    val CircleShapePart    = 231.asInstanceOf[Byte]
    val GroupShapePart     = 232.asInstanceOf[Byte]
    val ImageShapePart     = 233.asInstanceOf[Byte]
    val LineShapePart      = 234.asInstanceOf[Byte]
    val PolylineShapePart  = 235.asInstanceOf[Byte]
    val RectangleShapePart = 236.asInstanceOf[Byte]
    val TextShapePart      = 237.asInstanceOf[Byte]
  }

}
