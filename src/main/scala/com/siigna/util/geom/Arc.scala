/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */
package com.siigna.util.geom

import com.siigna.app.model.action.CreateShape

/**
 * A mathematical representation of a circle-piece, that is a not-full circle.
 * 
 * <b>Important:</b> Assumes that (start != middle != end).
 */
trait Arc {

  /**
   * The circle with the same center and radius as this Arc.
   * @return  A Circle with the same center and radius.
   */
  def circle : Circle

  /**
   * A value used to signal arcs defined counter-clockwise.
   */
  val cw, CW, clockwise = 0
  
  /**
   * A value used to signal arcs defined clockwise.
   */
  val ccw, CCW, counterclockwise = 1
  
  /**
   * Calculates the end angle. The end angle is always calculated so the
   * arc is drawn counter-clockwise (ccw). Firstly this is done because there's
   * a common mathematical convention that every circle is drawn ccw and
   * secondly because java's graphical engine follows this convention.
   */
  def endAngle : Double

  /**
   * Calculates the entire angle of the circle section.
   */
  def length : Double

  /**
   * The radius of the center to the circumference of the circle representing the circle-piece.
   */
  def radius : Double

  /**
   * Calculates the start angle. The start angle is always calculated so the
   * arc is drawn counter-clockwise (ccw). Firstly this is done because there's
   * a common mathematical convention that every circle is drawn ccw and
   * secondly because java's graphical engine follows this convention.
   */
  def startAngle : Double

  /**
   * Determines whether a given angle is included in the arc's periphery.
   */
  def insideArcDegrees(angle : Double) : Boolean

}

/**
 * A companion object to the Arc trait.
 */
object Arc {

  /**
   * Creates a 2D Arc.
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : Arc2D = Arc2D(start, middle, end)
}

/**
 * A mathematical representation of a 2-dimensional arc i. e. a circle piece. An arc is drawn counter-clockwise, so
 * positive values draws an arc counter-clockwise, and negative values draws it clockwise.
 *
 * @param center  The center of the circle representing the arc.
 * @param radius  The radius of the circle representing the arc.
 * @param startAngle  The starting angle of the arc. In degrees between 0 and 360.
 * @param angle The number of degrees the arc is spanning.
 */
@SerialVersionUID(-1174359692)
case class Arc2D(override val center : Vector2D, radius : Double, startAngle : Double, angle : Double) extends Arc with GeometryBasic2D {

  type T = Arc2D

  val circle = Circle2D(center, radius)

  /**
   * The end angle of the arc.
   */
  lazy val endAngle = (startAngle + angle) % 360

  /**
   * The point where the arc ends.
   */
  //TODO: check precision
  lazy val endPoint =  {
    val x = Math.round((math.cos(endAngle.toRadians)*radius + center.x)*10000.0)/10000.0
    val y = Math.round((math.sin(endAngle.toRadians)*radius + center.y)*10000.0)/10000.0
    Vector2D(x,y)
  }

  /**
   * Calculates the length of the arc, that is the distance the circumference spans.
   */
  lazy val length = radius * radius * math.Pi * (angle / 360.0)

  /**
   * Calculates the middle point of the arc (the point between the start and end point on the circumference).
   */
  lazy val midPoint = {
    val middleAngle = endAngle - startAngle
    val a = if (insideArcDegrees(middleAngle)) middleAngle else (middleAngle + 180) % 360
    Vector2D(math.cos(middleAngle) * radius, math.sin(middleAngle) * radius) + center
  }

  /**
   * The point where the arc starts. Remember we calculate angles from 3 o'clock counter-clockwise.
   */
  lazy val startPoint = Vector2D(math.cos(startAngle) * radius, math.sin(startAngle) * radius) + center

  /**
   * Calculates the boundary of the arc.
   * Thanks to: http://groups.google.com/group/comp.graphics.algorithms/browse_thread/thread/1adbcc734e44d024/79201c57a09149fe?lnk=gst&q=%22arc+%27bounding+box%27%22#79201c57a09149fe
   */
  def boundary = {
    def crop(a : Double) = if (a > 360) a - 360 else if (a < 0) a + 360 else a

    // First we find the coordinates for the ends of the arc
    val startX = math.cos(math.toRadians(startAngle)) * radius
    val startY = math.sin(math.toRadians(startAngle)) * radius
    val endX   = math.cos(math.toRadians(endAngle)) * radius
    val endY   = math.sin(math.toRadians(endAngle)) * radius

    // Locate the extremes (remember 0 degrees is 3 o'clock)
    val minX = if (crop(startAngle - 180) > crop(endAngle - 180)) -radius else math.min(startY, endX)
    val maxX = if (startAngle             > endAngle)              radius else math.max(startX, endX)
    val minY = if (crop(startAngle - 270) > crop(endAngle - 270)) -radius else math.min(startY, endY)
    val maxY = if (crop(startAngle - 90)  > crop(endAngle - 90))   radius else math.max(startY, endY)

    // Return the corresponding rectangle
    SimpleRectangle2D(center.x + minX, center.y + minY, center.x + maxX, center.y + maxY)
  }

  def closestPoint(vector : Vector2D) = (vector - center).unit * radius

  def distanceTo(geometry : Geometry2D) = geometry match {
    case p : Vector2D => {
      // The angle from 0 degrees (3 o'clock) to the point
      val angleToPoint = (p - center).angle

      // If the angle is lesser than the start and greater than the end angle
      // it must be 'inside' the arc, and the shortest distance must then be
      // the distance to the circle-section.
      if (angleToPoint >= startAngle && angleToPoint <= endAngle)
        scala.math.abs(radius - (center - p).length)

      // If the arc goes through 0 degrees (i. e. 360 degrees so the angle of
      // the start-point is lesser then the start-point), then we must test for
      // a case where the point can be 'inside' the circle-section when the
      // angle from the point to the X-axis is greater than the endAngle
      // or lesser than the startAngle.
      else if (endAngle < startAngle && (angleToPoint > startAngle || angleToPoint < endAngle))
        scala.math.abs(radius - (center - p).length)

      // If the anglePoint is closest to endAngle
      else if (angleToPoint - endAngle <= angleToPoint - startAngle)
        (p - endPoint).length

      // Else the point must be closest to startAngle
      else
        (p - startPoint).length
    }
    case _ => throw new UnsupportedOperationException("Not implemented")
  }

  /**
   * Determines whether a given angle is included in the arc's periphery.
   *
   * @param angle  The angle to examine in degrees.
   */
  def insideArcDegrees(angle : Double) : Boolean = {
    def bindTo360(a : Double) = if (a < 0) a + 360 else if (a > 360) a - 360 else a
    bindTo360(angle - startAngle) <= this.angle
  }

  def intersects(geom : Geometry2D) = geom match {

    case collection : CollectionGeometry2D => collection.intersects(this)

    case segment : Segment2D => {
      val parallelVectorD = segment.p2 - segment.p1  //normalized vector (p2 moved)
      val delta = segment.p1 - this.center  //delta = p2 - the circle center. (CHECK IF THIS IS RIGHT)
      val a = this.endPoint
      val b = this.startPoint
      //define a circle which contains an arc implicitly      |X-C|^2 = R^2  C = center
      //get intersections (aka Delta) = (D dot /\)^2 - |D|^2(|/\|^2 -R^2)     where |D| = length of the parallelVectorD
      //the result is rounded to five decimals.
      val intersectValue = math.round((math.pow((parallelVectorD * delta),2) - (math.pow(parallelVectorD.length,2) * (math.pow(delta.length,2) - math.pow(this.radius,2)))) * 100000)/100000.toDouble
      //intersectValue < 0    no intersection
      //intersectValue = 0    line tangent (one intersection)
      //intersectValue > 0    two intersections

      val tP = (-parallelVectorD * delta + math.sqrt(math.pow(parallelVectorD * delta,2) - math.pow(parallelVectorD.length,2)*(math.pow(delta.length,2) -math.pow(circle.radius,2)))) / math.pow(parallelVectorD.length,2)
      val tN = (-parallelVectorD * delta - math.sqrt(math.pow(parallelVectorD * delta,2) - math.pow(parallelVectorD.length,2)*(math.pow(delta.length,2) -math.pow(circle.radius,2)))) / math.pow(parallelVectorD.length,2)
      val int1 = segment.p1 + parallelVectorD * tP
      val int2 = segment.p1 + parallelVectorD * tN

      val ortVector = (b - a).normal

      //evaluate if the intersections are on the arc (onArc should be >= 0)
      val int1OnArc = (int1 - a) * ortVector
      val int2OnArc = (int2 - a) * ortVector
      val tPonArc = if(int1OnArc >= 0) true else false
      val tNonArc = if(int2OnArc >= 0) true else false

      //if both tP and tN are outside the range 0-1, there are no intersections:
      if( tP < 0 && tP > 1 && tN < 0 && tN > 1 ) false

      //if one of tP of tN are in range 0-1, there is an intersection - if the respective intersection is on the arc segment:
      else if((tP >= 0 && tP <= 1 && tPonArc == true) || (tN >= 0 && tN <= 1 && tNonArc == true)) true

      //if intersectValue is zero, the segment is tangent to the arc (one intersection)
      else if(intersectValue == 0) true

      //if none of these conditions are true, there are no intersections.
      else false
    }

    //TODO: matches on the boundary of the arc, which is not correct.
    case rectangle : SimpleRectangle2D => {
      !(boundary.xMin > rectangle.xMax || boundary.xMin < rectangle.xMin || boundary.yMin > rectangle.yMax || boundary.yMax < rectangle.yMin)
    }
    case g => throw new UnsupportedOperationException("Rectangle: Intersects not yet implemented with " + g)

    /**
   * Returns a list of points, defined as the intersection(s) between the
   * arc and another arc.
   */
  }
  def intersections(geometry : Geometry2D) : Set[Vector2D] = geometry match {
    case arc : Arc2D => {
      val circleIntersections = Circle(center, radius).intersections(Circle(arc.center, arc.radius))
      if (circleIntersections.isEmpty) {
        Set() // No intersections
      } else { // Check whether both points are inside the respective arcs
        circleIntersections.filter(p => {
          val angle1 = (p - center).angle
          val angle2 = (p - arc.center).angle
          (insideArcDegrees(angle1) && arc.insideArcDegrees(angle2))
        })
      }
    }
    case segment : Segment2D => {
      val parallelVectorD = segment.p2 - segment.p1  //normalized vector (p2 moved)
      val delta = segment.p1 - this.center  //delta = p2 - the circle center. (CHECK IF THIS IS RIGHT)
      val a = this.endPoint
      val b = this.startPoint
      val intersectValue = math.round((math.pow((parallelVectorD * delta),2) - (math.pow(parallelVectorD.length,2) * (math.pow(delta.length,2) - math.pow(this.radius,2)))) * 100000)/100000.toDouble
      //find the roots; that is the value t to be inserted into the
      val tP = (-parallelVectorD * delta + math.sqrt(math.pow(parallelVectorD * delta,2) - math.pow(parallelVectorD.length,2)*(math.pow(delta.length,2) -math.pow(circle.radius,2)))) / math.pow(parallelVectorD.length,2)
      val tN = (-parallelVectorD * delta - math.sqrt(math.pow(parallelVectorD * delta,2) - math.pow(parallelVectorD.length,2)*(math.pow(delta.length,2) -math.pow(circle.radius,2)))) / math.pow(parallelVectorD.length,2)
      val int1 = segment.p1 + parallelVectorD * tP
      val int2 = segment.p1 + parallelVectorD * tN
      val ortVector = (b - a).normal

      //evaluate if the intersections are on the arc (onArc should be >= 0)
      val int1OnArc = (int1 - a) * ortVector
      val int2OnArc = (int2 - a) * ortVector
      val tPonArc = if(int1OnArc >= 0) true else false
      val tNonArc = if(int2OnArc >= 0) true else false

      //two intersections: two roots, both in range 0-1 and on the arc.
      if(intersectValue > 0 && (tP >= 0 && tPonArc == true && tP <= 1) && (tN >= 0 && tNonArc == true && tN <=1)) Set(int1,int2)

      //one intersection: if tP is in range and tP is on the arc while tN is not  - or the other way around.
      else if(tP >= 0 && tP <= 1 && tPonArc == true) Set(int1)
      else if(tN >= 0 && tN <= 1 && tNonArc == true) Set(int2)

      //TODO: tangent situations does not generate the correct vector, as both roots yield NaN?? The intersection needs to be calculated.
      else if (intersectValue == 0) Set(Vector2D(0,0))

      //zero intersections: if intersectValue < 0
      else Set()




      //if only ONE intersection is found (the line does not cross the full circle, check if this intersection lies on the arc. If so, return it:
      //else if((tP >= 0 && tP <= 1 && tPonArc == true) && tN < 0 || tN > 1) Set(int1)
      //else if((tN >= 0 && tN <= 1 && tNonArc == true) && tP < 0 || tP > 1) Set(int2)



    }
    case _ => throw new UnsupportedOperationException("Not implemented")
  }

  def transform(t : TransformationMatrix) =
    new Arc2D(t.transform(center), radius * t.scaleFactor, startAngle, angle)

  // TODO: Add middlepoint
  lazy val vertices = {
    Seq(startPoint, endPoint)
  }

}

/**
 * The companion object for Arc2D.
 */
object Arc2D {

  //import java.lang.Double.NaN

  /**
   * Locates a center point from three points on a periphery.
   */
  def findCenterPoint(start : Vector2D, middle : Vector2D, end : Vector2D) : Vector2D = {
    // If two of the points are the same, we cannot properly locate the center
    if (start == middle && start == end && middle == end) {
      throw new IllegalArgumentException("Unable to create arc if two points coincide.")
    }
    // Locate the center where the normal-vectors of the lines from start to middle and end to middle intersects
    // First find the two middle points
    val m1 = (middle + start)/2
    val m2 = (end + middle)/2
    // Then find the normal vectors
    val p1 = m1 + (middle - start).normal
    val p2 = m2 + (end - middle).normal

    // Locate the intersections
    val intersections = Line2D(m1, p1).intersections(Line2D(m2, p2))
    if (intersections.size != 1) {
      throw new UnsupportedOperationException("Unable to calculate the center of the arc.")
    } else {
      intersections.head
    }
  }

  /**
   * Calculates the entire angle-span of an arc from a start and end angle (CCW).
   */
  def findArcSpan(startAngle : Double, endAngle : Double) = {
    if (startAngle > endAngle)
      endAngle - startAngle + 360
    else if (startAngle < endAngle)
      endAngle - startAngle
    else throw new IllegalArgumentException("Cannot create arc with 0 degrees.")
  }

  /**
   * Creates an arc from three points.
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : Arc2D = {
    // Calculate center and radius
    val center = findCenterPoint(start, middle, end)
    val radius = (start - center).length
    // The start angle of the points, NOT the arc
    val startAngle = (start - center).angle
    // The end angle of the points, NOT the arc
    val endAngle = (end - center).angle

    // Find the angle spanning start -> end CCW
    val angle = findArcSpan(startAngle, endAngle)

    // Find out which side of the line from start to end, the middle point is on.
    // If it's positive, the arc is counter-clockwise
    val det = (end.x - start.x) * (middle.y - start.y) - (end.y - start.y) * (middle.x - start.x)

    if (det > 0) {
      new Arc2D(center, radius, endAngle, findArcSpan(endAngle, startAngle))
    } else {
      new Arc2D(center, radius, startAngle, angle)
    }

    /*
    val middleAngle = (middle - center).angle
    // If the middle angle is inside the interval between start and end angle...
    // TODO: Not quite working.
    if (startAngle <= middleAngle && middleAngle <= angle + startAngle) {
      new Arc2D(center, radius, startAngle, angle)
    } else {
      // Otherwise swap the end and start angle and find the CCW angle from end -> start
      new Arc2D(center, radius, endAngle, findArcSpan(endAngle, startAngle))
    }
    */
  }

}