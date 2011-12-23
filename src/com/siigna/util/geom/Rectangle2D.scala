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
package com.siigna.util.geom

/**
 * A rectangle given by two points.
 * TODO:
 */
case class Rectangle2D(xMin : Double, yMin : Double, xMax : Double, yMax : Double) extends Rectangle[Dimension2]
{

  /**
   * The lowest left corner of the rectangle.
   */
  def bottomLeft  = Vector(xMin, yMin)

  /**
   * The lowest right corner of the rectangle.
   */
  def bottomRight = Vector(xMax, yMax)

  /**
   * The upper left corner of the rectangle.
   */
  def topLeft     = Vector(xMin, yMax)

  /**
   * The upper right corner of the rectangle.
   */
  def topRight    = Vector(xMax, yMax)

  /**
   * The center of the rectangle.
   */
  lazy val center = (topLeft + bottomRight) / 2

  /**
   * Computes the area of the rectangle.
   */
  def area = height * width

  /**
   * The boundary of the rectangle. In this case returns itself.
   */
  def boundary = this

  /**
   * Examines whether a given arc is within the four boundaries
   * of a rectangle.
   */
  def contains(arc : Arc2D) : Boolean =
  {
    if (arc == null) {
      false
    } else {
      // If it contains the entire circle of the arc, that's great.
      if (contains(Circle(arc.center, arc.start)))
        true
      // If not we have to look at how much of the circle it contains.
      // If the rectangle contains all points, and doesn't intercept with the
      // rectangle, we have a hit!
      else {
        (contains(arc.start) && contains(arc.middle) && contains(arc.end) && !intersect(arc))
      }
    }
  }

  /**
   * Examines whether a circle is within the four boundaries
   * of a rectangle.
   */
  def contains(circle : Circle2D) : Boolean =
  {
    if (circle == null) {
      false
    } else {
      val UL = Vector(circle.center.x - circle.radius, circle.center.y - circle.radius) //Upper left
      val LR = Vector(circle.center.x + circle.radius, circle.center.y + circle.radius) //Lower right
      (contains(UL) && contains(LR))
    }
  }

  /**
   * Examines whether an ellipse is within the four boundaries
   * of a rectangle.
   */
  def contains(e : Ellipse) : Boolean =
  {
    if (e == null) {
      false
    } else {
      // Creates a transformation matrix that translates (pans) and rotates the
      // ellipse, so the center is positioned at <code>Vector(0, 0)</code> and the
      // two focus-points is on at the X-axis
      val rotated  = e.f1.angle
      val toCenter = TransformationMatrix(-e.center, 1) rotate(-rotated, e.center)

      // Transform the points
      val newCenter = e.center.transform(toCenter)

      val topLeft     = Vector(newCenter.x - e.a, newCenter.y + e.b).transform(toCenter.inverse)
      val topRight    = Vector(newCenter.x + e.a, newCenter.y + e.b).transform(toCenter.inverse)
      val bottomLeft  = Vector(newCenter.x - e.a, newCenter.y - e.b).transform(toCenter.inverse)
      val bottomRight = Vector(newCenter.x + e.a, newCenter.y - e.b).transform(toCenter.inverse)

      (contains(topLeft) && contains(topRight) && contains(bottomLeft) && contains(bottomRight))
    }
  }

  /**
   * Examines whether a line is within (or on top of) the four boundaries
   * of a rectangle.
   */
  def contains(line : Segment) : Boolean =
  {
    if (line.p1 == line.p2) {
      false
    } else {
      (contains(line.p1) && contains(line.p2))
    }
  }

  /**
   * Examines whether a point is within (or on top of) the four boundaries
   * of a rectangle.
   */
  def contains(point : Vector) : Boolean =
    (bottomLeft.x <= point.x && point.x <= topRight.x &&
       bottomLeft.y <= point.y && point.y <= topRight.y)

  /**
   * Examines whether a given rectangle is within (or on top of) the four boundaries
   * of this rectangle.
   */
  def contains(rectangle : Rectangle) : Boolean =
    (bottomLeft.x <= rectangle.bottomLeft.x && rectangle.topRight.x <= topRight.x &&
       bottomLeft.y <= rectangle.bottomLeft.y && rectangle.topRight.y <= topRight.y)

  /**
   * Calculate the circumference of the rectangle.
   */
  def circumference = width + width + height + height

  /**
   * Calculates the distance to a point.
   */
  def distanceTo(point : Vector) =
    Segment.segmentsOnClosedPathOfPoints(points).map(
      _ distanceTo(point)
    ).reduceLeft( (a, b) => if (a < b) a else b)

  /**
   * Expands this rectangle to include an arc.
   * TODO: Not the right way to include an arc!
   */
  def expand(arc : Arc) : Rectangle =
    expand(Circle(arc.center, arc.start))

  /**
   * Expands this rectangle to include a circle.
   */
  def expand(circle : Circle) : Rectangle =
    expand(Rectangle(Vector(circle.center.x - circle.radius, circle.center.y + circle.radius),
                     Vector(circle.center.x + circle.radius, circle.center.y - circle.radius)))

  /**
   * Expands this rectangle to include an ellipse.
   */
  def expand(e : Ellipse) : Rectangle = {
    // Creates a transformation matrix that translates (pans) and rotates the
    // ellipse, so the center is positioned at <code>Vector(0, 0)</code> and the
    // two focus-points is on at the X-axis
    val rotated  = e.f1 angle
    val toCenter = TransformationMatrix(-e.center, 1) rotate(-rotated, e.center)

    // Transform the points
    val newCenter = e.center.transform(toCenter)

    val newTopLeft     = Vector(newCenter.x - e.a, newCenter.y + e.b).transform(toCenter.inverse)
    val newTopRight    = Vector(newCenter.x + e.a, newCenter.y + e.b).transform(toCenter.inverse)
    val newBottomLeft  = Vector(newCenter.x - e.a, newCenter.y - e.b).transform(toCenter.inverse)
    val newBottomRight = Vector(newCenter.x + e.a, newCenter.y - e.b).transform(toCenter.inverse)

    val rect1 = expand(newTopLeft)
    val rect2 = rect1.expand(newTopRight)
    val rect3 = rect2.expand(newBottomLeft)
    val rect4 = rect3.expand(newBottomRight)

    // Returns the new rectangle
    rect4
  }

  /**
   * Expands this rectangle to include a point.
   */
  def expand(point : Vector) : Rectangle = {
    if (contains(point))
      this
    else {
      val newTopLeft     = Vector(scala.math.min(topLeft.x, point.x), scala.math.max(topRight.y, point.y))
      val newBottomRight = Vector(scala.math.max(bottomRight.x, point.x), scala.math.min(bottomRight.y, point.y))
      Rectangle(newTopLeft, newBottomRight)
    }
  }

  /**
   * Expands this rectangle to include another rectangle.
   */
  def expand(rect : Rectangle) : Rectangle = {
    val newTopLeft     = Vector(scala.math.min(topLeft.x, rect.topLeft.x),
                                scala.math.max(topLeft.y, rect.topLeft.y))
    val newBottomRight = Vector(scala.math.max(bottomRight.x, rect.bottomRight.x),
                                scala.math.min(bottomRight.y, rect.bottomRight.y))
    Rectangle(newTopLeft, newBottomRight)
  }

  def handles = List((bottomRight + topRight)/2, topRight,
                     (topRight + topLeft)/2, topLeft,
                     (topLeft + bottomLeft)/2, bottomLeft,
                     (bottomLeft + bottomRight)/2, bottomRight)

  /**
   * Returns the height of the rectangle.
   */
  def height = (topLeft.y - bottomLeft.y).abs

  /**
   * Examines whether a given arc intersect with this rectangle.
   */
  def intersects(arc : Arc) = arc.intersects(this)
  /**
   * Examines whether a given circle intersects with this rectangle.
   */
  def intersects(circle : Circle) = circle.intersects(this)

  /**
   * Examines whether a given ellipse intersects with this rectangle.
   * TODO: You can do better than this!
   */
  def intersect(ellipse : Ellipse) =
  {
    false
  }

  /**
   * Examines whether a given rectangle intersects with this rectangle.
   *
   * A = this, B = that.
   * Cond1.  If A's left edge is to the right of the B's right edge, then A is Totally to right Of B
   * Cond2.  If A's right edge is to the left of the B's left edge,  then A is Totally to left Of B
   * Cond3.  If A's top edge is below B's bottom  edge,              then A is Totally below B
   * Cond4.  If A's bottom edge is above B's top edge,               then A is Totally above B
   *
   * Reference: http://stackoverflow.com/questions/306316/determine-if-two-rectangles-overlap-each-other/306332#306332
   */
  def intersects(that : Rectangle2D) =
    !(xMin > that.xMax || xMax < that.xMin || yMin > that.yMax || yMax < that.yMin)

  /**
   * Examines whether a given circle intersects with this rectangle.
   */
  def intersects(segment : Segment2D) = segment.intersects(this)

  /**
   * Examines where a given rectangle intersects with this rectangle.
   */
  def intersects(rectangle : Rectangle2D) = if (intersects(rectangle))
      Segment.segmentsOnClosedPathOfPoints(this points).map(
        segment => Segment.segmentsOnClosedPathOfPoints(rectangle.points).map(segment.intersects(_)).flatten
      ).flatten.distinct
    else false

  def intersects(segment : Segment2D) = segment.intersects(this)

  /**
   * Checks whether the given point is on the outer edge of the rectangle.
   */
  def onPeriphery(point : Vector2D) =
    (point.x == xMin || point.x == xMax) && (point.y == yMax || point.y == yMin)

  /**
   * Calculate the overlap between this and another rectangle. If two rectangles do not overlap the area is 0.
   */ 
  def overlap(that : Rectangle2D) : Double = {
    if (intersects(that)) {
      val xMin = math.max(bottomLeft.x, that.bottomLeft.x)
      val yMin = math.max(bottomLeft.y, that.bottomLeft.y)
      val xMax = math.min(topRight.x, that.topRight.x)
      val yMax = math.max(topRight.y, topRight.y)
      (xMax - xMin) * (yMax - yMin)
    } else 0 // No overlap
  }

  /**
   * The points of the rectangle.
   */
  def points = List(topLeft, topRight, bottomRight, bottomLeft)

  def transform(t : TransformationMatrix) = Rectangle(p1.transform(t), p2.transform(t))

  /**
   * Returns a rectangle that encapsulates both this rectangle and the given rectangle.
   */
  def union(that : Rectangle) =
    Rectangle2D(Vector(math.min(this.topLeft.x, that.topLeft.x), math.min(this.topLeft.y, that.topLeft.y)),
              Vector(math.max(this.bottomRight.x, that.bottomRight.x), math.max(this.bottomRight.y, that.bottomRight.y)))

  /**
   * Returns the width of the rectangle.
   */
  def width = (topRight.x - topLeft.x).abs

}

/**
 * A companion object to Rectangle2D.
 */
object Rectangle2D {

  import java.lang.Double.NaN

  def empty = new Rectangle2D(NaN, NaN, NaN, NaN)

}