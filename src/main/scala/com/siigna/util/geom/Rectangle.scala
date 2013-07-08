/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */
package com.siigna.util.geom

/**
 * A rectangle, defined as four line-segments in a multidimensional space. Can be compared to other rectangles
 * by their areas.
 */
trait Rectangle extends Ordered[Rectangle] {

  type T <: Rectangle
  type V <: Vector

  /**
   * Moves the rectangle with the coordinates given in the vector by subtracting the coordinate-values of the
   * rectangle with the coordinate values from the vector.
   * @param point  The point that illustrates the distance to move the rectangle.
   * @return  A new rectangle moved by a distance corresponding to -(point).
   */
  def -(point : V) : T

  /**
   * Moves the rectangle with the coordinates given in the vector by adding the coordinate-values of the
   * rectangle with the coordinate values from the vector.
   * @param point  The point that illustrates the distance to move the rectangle.
   * @return  A new rectangle moved by a distance corresponding to +(point).
   */
  def +(point : V) : T

  /**
   * Scales the coordinates of the rectangle with the coordinates given in the vector by multiplying the
   * coordinate-values of the rectangle with the coordinate values from the vector.
   * @param point  The point that illustrates the distance to move the rectangle.
   * @return  A new rectangle moved by a distance corresponding to (rectangle.x * point.x, rectangle-y * point.y ...).
   */
  def *(point : V) : T

  /**
   * Scales the rectangle with the values given in the vector by dividing the coordinate-values of the
   * rectangle with the coordinate values from the vector.
   * @param point  The point that illustrates the distance to move the rectangle.
   * @return  A new rectangle moved by a distance corresponding to (rectangle.x / point.x, rectangle-y / point.y ...).
   */
  def /(point : V) : T

  /**
   * Computes the area of the rectangle.
   */
  def area : Double

  /**
   * Calculate the circumference of the rectangle.
   */
  def circumference : Double

  /**
   * Compares two rectangles on the size of their area. If the difference is smaller than 0, but still larger than
   * 0 (absolute), we return the nearest integer that is not null.
   * @param that  The rectangle to compare.
   * @return < 0 if this rectangle is smaller in area, 0 if they are equal (within floating point precision) or > 1
   *         if this rectangle is larger than the given rectangle.
   */
  def compare(that: Rectangle) = {
    val x = this.area - that.area
    val r = x.toInt
    // Make sure that small differences in the interval ]-1,1[ are included (floating point precision included)
    if (r == 0 && x > 0 && x > Double.MinPositiveValue) 1
    else if (r == 0 && x < 0 && x < -Double.MinPositiveValue) -1
    else r
  }

  /**
   * The height of the rectangle
   */
  def height : Double

  /**
   * Checks whether the given point is on the outer edge of the rectangle.
   */
  def onPeriphery(point : V) : Boolean

  /**
   * Calculate the overlap between this and another rectangle. If two rectangles do not overlap the area is 0.
   */ 
  def overlap(that : T) : Double

  /**
   * Rounds the coordinates of the rectangle to the nearest whole numbers.
   * @return  A rectangle with its coordinates rounded.
   */
  def round : T

  /**
   * The width of the rectangle.
   * @return  A positive [[scala.Double]]
   */
  def width : Double

}

/**
 * A Rectangle in 2 dimensions.
 */
trait Rectangle2D extends Rectangle with GeometryClosed2D {


  type T <: Rectangle2D

  def area = width * height

  /**
   * The lowest left corner of the rectangle.
   */
  def bottomLeft  : Vector2D

  /**
   * The lowest right corner of the rectangle.
   */
  def bottomRight : Vector2D

  /**
   * Returns the line spanning from the bottom left corner to the bottom right.
   */
  def borderBottom = Segment2D(bottomLeft, bottomRight)

  /**
   * Returns the line spanning from the top left corner to the bottom left.
   */
  def borderLeft = Segment2D(topLeft, bottomLeft)

  /**
   * Returns the line spanning from the top right corner to the bottom right.
   */
  def borderRight = Segment2D(topRight, bottomRight)

  /**
   * Returns the line spanning from the top left corner to the rop right.
   */
  def borderTop = Segment2D(topLeft, topRight)

  def boundary = {
    val vertices = Seq(this.bottomLeft,bottomRight,topRight,topLeft)
    def max(i1: Double, i2: Double): Double = if (i1 > i2) i1 else i2
    def min(i1: Double, i2: Double): Double = if (i1 < i2) i1 else i2

    val xMax = vertices.map(_.x).reduceLeft(max)
    val xMin = vertices.map(_.x).reduceLeft(min)
    val yMax = vertices.map(_.y).reduceLeft(max)
    val yMin = vertices.map(_.y).reduceLeft(min)

    Rectangle2D(Vector2D(xMax,yMax), Vector2D(xMin,yMin))
  }

  def circumference = height * 2 + width * 2

  def distanceTo(geom : Geometry2D) = geom match {
    /**
     * Calculates the distance from a simple rectangle to a point.
     */
    case point : Vector2D =>
      Segment2D.segmentsOnClosedPathOfPoints(vertices.toSeq).view.map(
        _ distanceTo(point)
      ).reduceLeft( (a, b) => if (a < b) a else b)

    case _ => throw new UnsupportedOperationException("Rectangle: DistanceTo not yet implemented for " + geom)
  }

  /**
   * Expands the rectangle to contain the given geometry.
   * @param geom  The geometry to include.
   * @return  A new and enlarged [[com.siigna.util.geom.Rectangle2D]].
   */
  def expand(geom : Geometry2D) : T

  /**
   * Returns the height of the rectangle.
   */
  def height : Double

  def onPeriphery(point : Vector2D) : Boolean

  /**
   * The upper left corner of the rectangle.
   */
  def topLeft     : Vector2D

  /**
   * The upper right corner of the rectangle.
   */
  def topRight    : Vector2D

  lazy val vertices = Seq(topLeft, topRight, bottomRight, bottomLeft)

  def width : Double

}

/**
 * Factory object for [[com.siigna.util.geom.Rectangle2D]].
 */
object Rectangle2D {

  /**
   * Creates a [[com.siigna.util.geom.SimpleRectangle2D]] from the given points where the difference on the x axis
   * equals the width and the difference on the y axis equals the height.
   * @param v1  A [[com.siigna.util.geom.Vector2D]] indicating one of the four corners of the Rectangle
   * @param v2  Another vector for one of the four corners
   * @return  An instance of a [[com.siigna.util.geom.SimpleRectangle2D]]
   */
  def apply(v1 : Vector2D, v2 : Vector2D) =
    new SimpleRectangle2D(math.min(v1.x, v2.x), math.min(v1.y, v2.y), math.max(v1.x, v2.x), math.max(v1.y, v2.y))

  /**
   * Creates a [[com.siigna.util.geom.SimpleRectangle2D]] with a center and a width and a height. The rectangle is
   * constructed by defining the top left point as (center.x - width / 2, center.y - height / 2) and the bottom
   * right point as (center.x + width / 2, center.y + height / 2).
   * @param center  The center-point of the rectangle
   * @param width  The width of the rectangle
   * @param height  The height of the rectangle.
   * @return  An instance of a [[com.siigna.util.geom.SimpleRectangle2D]]
   */
  def apply(center : Vector2D, width : Double, height : Double) = {
    val w = width * 0.5
    val h = height * 0.5
    new SimpleRectangle2D(center.x - w, center.y - h, center.x + w, center.y + h)
  }

  /**
   * Creates a [[com.siigna.util.geom.SimpleRectangle2D]] from the given points where the difference on the x axis
   * equals the width and the difference on the y axis equals the height.
   * @param xMin  The smallest x-coordinate
   * @param yMin  The smallest y-coordinate
   * @param xMax  The largest x-coordinate
   * @param yMax  The largest y-coordinate
   * @return  An instance of a [[com.siigna.util.geom.SimpleRectangle2D]]
   */
  def apply(xMin : Double, yMin : Double, xMax : Double, yMax : Double) =
    new SimpleRectangle2D(xMin, yMin, xMax, yMax)

}

/**
 * A simple rectangle given by two points. Contrary to a [[com.siigna.util.geom.ComplexRectangle2D]] a simple rectangle
 * cannot be rotated.
 *
 * @param xMin  The least x-value
 * @param yMin  The least y-value
 * @param xMax  The largest x-value
 * @param yMax  The largest y-value
 */
case class SimpleRectangle2D(xMin : Double, yMin : Double, xMax : Double, yMax : Double) extends Rectangle2D {

  type T = SimpleRectangle2D

  def -(point : Vector2D) = new SimpleRectangle2D(xMin - point.x, yMin - point.y, xMax - point.x, yMax - point.y)
  def +(point : Vector2D) = new SimpleRectangle2D(xMin + point.x, yMin + point.y, xMax + point.x, yMax + point.y)
  def *(point : Vector2D) = new SimpleRectangle2D(xMin * point.x, yMin * point.y, xMax * point.x, yMax * point.y)
  def /(point : Vector2D) = new SimpleRectangle2D(xMin / point.x, yMin / point.y, xMax / point.x, yMax / point.y)

  /**
   * The lowest left corner of the rectangle.
   */
  def bottomLeft  = Vector2D(xMin, yMin)

  /**
   * The lowest right corner of the rectangle.
   */
  def bottomRight = Vector2D(xMax, yMin)

  /**
   * The upper left corner of the rectangle.
   */
  def topLeft     = Vector2D(xMin, yMax)

  /**
   * The upper right corner of the rectangle.
   */
  def topRight    = Vector2D(xMax, yMax)

  /**
   * The center of the rectangle.
   */
  override val center = (topLeft + bottomRight) / 2

  def closestPoint(point : Vector2D) = point

  def contains(geom : Geometry2D) : Boolean = geom match {
    // Todo: Refine this
    case arc : Arc2D => contains(arc.circle)

    /**
     * Examines whether a circle is within the four boundaries
     * of a rectangle.
     */
    case circle : Circle2D => {
      if (circle == null) {
        false
      } else {
        val UL = Vector2D(circle.center.x - circle.radius, circle.center.y - circle.radius) //Upper left
        val LR = Vector2D(circle.center.x + circle.radius, circle.center.y + circle.radius) //Lower right
        (contains(UL) && contains(LR))
      }
    }

    /**
     * Examines whether any elements exists inside the collection
     * that does not lie within this Rectangle
     */
    case collection : CollectionGeometry2D => collection.geometries.exists(g => !contains(g))

    /**
     * Examines whether an ellipse is within the four boundaries
     * of a rectangle.
     *
    case e : Ellipse2D => {
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

        val topLeft     = Vector2D(newCenter.x - e.a, newCenter.y + e.b).transform(toCenter.inverse)
        val topRight    = Vector2D(newCenter.x + e.a, newCenter.y + e.b).transform(toCenter.inverse)
        val bottomLeft  = Vector2D(newCenter.x - e.a, newCenter.y - e.b).transform(toCenter.inverse)
        val bottomRight = Vector2D(newCenter.x + e.a, newCenter.y - e.b).transform(toCenter.inverse)

        (contains(topLeft) && contains(topRight) && contains(bottomLeft) && contains(bottomRight))
      }
    }*/

    /**
     * Examines whether a line is within (or on top of) the four boundaries
     * of a rectangle.
     */
    case line : Segment2D => {
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
    case point : Vector2D =>
      bottomLeft.x <= point.x && point.x <= topRight.x &&
        bottomLeft.y <= point.y && point.y <= topRight.y

    /**
     * Examines whether a given rectangle is within (or on top of) the four boundaries
     * of this rectangle.
     */
    case rectangle : SimpleRectangle2D =>
      bottomLeft.x <= rectangle.bottomLeft.x && rectangle.topRight.x <= topRight.x &&
        bottomLeft.y <= rectangle.bottomLeft.y && rectangle.topRight.y <= topRight.y

    case g => throw new UnsupportedOperationException("Rectangle: Contains not yet implemented for " + g)
  }

  def expand(geom : Geometry2D) : SimpleRectangle2D = geom match {
    /**
     * Expands this rectangle to include an arc.
     * TODO: Not the right way to include an arc!
     */
    case arc : Arc2D => expand(Circle(arc.center, arc.radius))

    /**
     * Expands this rectangle to include a circle.
     */
    case circle : Circle2D =>
      expand(SimpleRectangle2D(circle.center.x - circle.radius, circle.center.y - circle.radius,
        circle.center.x + circle.radius, circle.center.y + circle.radius))

    /**
     * Expands this rectangle to include a point.
     */
    case point : Vector2D => {
      if (contains(point))
        this
      else {
        val newTopLeft     = Vector2D(scala.math.min(topLeft.x, point.x), scala.math.max(topRight.y, point.y))
        val newBottomRight = Vector2D(scala.math.max(bottomRight.x, point.x), scala.math.min(bottomRight.y, point.y))
        Rectangle2D(newTopLeft, newBottomRight)
      }
    }

    /**
     * Expands this rectangle to include another rectangle.
     */
    case rect : SimpleRectangle2D => {
      val xMin = scala.math.min(topLeft.x, rect.topLeft.x)
      val yMin = scala.math.min(bottomRight.y, rect.bottomRight.y)
      val xMax = scala.math.max(bottomRight.x, rect.bottomRight.x)
      val yMax = scala.math.max(topLeft.y, rect.topLeft.y)
      new SimpleRectangle2D(xMin, yMin, xMax, yMax)
    }

    case g => throw new UnsupportedOperationException("Rectangle: Expand not yet implemented for " + g)
  }


  /**
   * Returns the height of the rectangle.
   */
  def height = (yMax - yMin).abs

  def intersects(geom : Geometry2D) = geom match {
    case arc : Arc2D => arc.intersects(this)
    case circle : Circle2D => circle.intersects(this)
    case collection : CollectionGeometry2D => collection.intersects(this)
    case segment : Segment2D => segment.intersects(this)

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
    case that : SimpleRectangle2D =>
      !(xMin > that.xMax || xMax < that.xMin || yMin > that.yMax || yMax < that.yMin)

    case g => throw new UnsupportedOperationException("Rectangle: Intersects not yet implemented with " + g)
  }

  def intersections(geom : Geometry2D) : Set[Vector2D] = geom match {
    case line : Line2D => {
      val top = Line2D(topLeft, topRight)
      val right = Line2D(topRight, bottomRight)
      val bottom = Line2D(bottomRight, bottomLeft)
      val left = Line2D(bottomLeft, topLeft)

      Set(top, right, bottom, left).flatMap(_.intersections(line))
    }
    case segment : Segment2D => segment.intersections(this)
    case g => throw new UnsupportedOperationException("Rectangle: Intersections not yet implemented with " + g)
  }

  def onPeriphery(point : Vector2D) =
    (point.x == xMin || point.x == xMax) && (point.y == yMax || point.y == yMin)

  /**
   * Calculate the overlap between this and another rectangle. If two rectangles do not overlap the area is 0.
   */
  def overlap(that : SimpleRectangle2D) : Double = {
    if (intersects(that)) {
      val xMin = math.max(bottomLeft.x, that.bottomLeft.x)
      val yMin = math.max(bottomLeft.y, that.bottomLeft.y)
      val xMax = math.min(topRight.x, that.topRight.x)
      val yMax = math.max(topRight.y, topRight.y)
      (xMax - xMin) * (yMax - yMin)
    } else 0 // No overlap
  }

  def round = Rectangle2D(topLeft.round, bottomRight.round)

  def transform(t : TransformationMatrix) = {
    val p1 = topLeft.transform(t)
    val p2 = bottomRight.transform(t)

    SimpleRectangle2D(p1.x, p1.y, p2.x, p2.y)
  }

  def width = (xMax - xMin).abs

}