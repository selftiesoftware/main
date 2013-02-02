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
    val Error   = 0.toByte
    val Get     = 1.toByte
    val Set     = 2.toByte
    val Session = 3.toByte
    val User    = 4.toByte

    // Util
    val Attributes = 50.toByte
    val TransformationMatrix = 51.toByte
    val Vector2D   = 52.toByte
    val Model      = 53.toByte

    // Scala
    val Traversable = 80.toByte
    val Map         = 81.toByte

    // Java
    val Color    = 90.toByte

    // Actions
    val AddAttributes       = 100.toByte
    val SetAttributes       = 101.toByte
    val CreateShape         = 102.toByte
    val CreateShapes        = 103.toByte
    val DeleteShape         = 104.toByte
    val DeleteShapes        = 105.toByte
    val DeleteShapePart     = 106.toByte
    val DeleteShapeParts    = 107.toByte
    val RemoteAction        = 108.toByte
    val SequenceAction      = 109.toByte
    val TransformShape      = 110.toByte
    val TransformShapeParts = 111.toByte
    val TransformShapes     = 112.toByte

    // Shapes
    val ArcShape      = 200.toByte
    val CircleShape   = 201.toByte
    val GroupShape    = 202.toByte
    val ImageShape    = 203.toByte
    val LineShape     = 204.toByte
    val PolylineShapeClosed   = 205.toByte
    val PolylineShapeOpen     = 206.toByte
    val RectangleShapeSimple  = 207.toByte
    val RectangleShapeComplex = 208.toByte
    val TextShape     = 209.toByte

    // Inner polyline shapes
    val PolylineLineShape = 220.toByte
    val PolylineArcShape  = 221.toByte

    // Shape parts
    val ArcShapePart       = 230.toByte
    val CircleShapePart    = 231.toByte
    val GroupShapePart     = 232.toByte
    val ImageShapePart     = 233.toByte
    val LineShapePart      = 234.toByte
    val PolylineShapePart  = 235.toByte
    val RectangleShapePart = 236.toByte
    val TextShapePart      = 237.toByte
  }

}
