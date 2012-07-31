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
package com.siigna.util.geom

/**
 * A rectangle given by two points.
 *
 * @param xMin  The least x-value
 * @param yMin  The least y-value
 * @param xMax  The largest x-value
 * @param yMax  The largest y-value
 */
@SerialVersionUID(-1453115647)
case class Rectangle2D(xMin : Double, yMin : Double, xMax : Double, yMax : Double) extends Rectangle with GeometryEnclosed2D {

  type T = Rectangle2D

  def area = width * height

  /**
   * The lowest left corner of the rectangle.
   */
  def bottomLeft  = Vector(xMin, yMin)

  /**
   * The lowest right corner of the rectangle.
   */
  def bottomRight = Vector(xMax, yMin)

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
  override val center = (topLeft + bottomRight) / 2

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

  /**
   * The boundary of the rectangle. In this case returns itself.
   */
  override def boundary = this

  def circumference = height * 2 + width * 2

  def closestPoint(point : Vector2D) = point

  def contains(geom : Geometry2D) : Boolean = geom match {
    /**
     * Examines whether a circle is within the four boundaries
     * of a rectangle.
     */
    case circle : Circle2D => {
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
      (bottomLeft.x <= point.x && point.x <= topRight.x &&
         bottomLeft.y <= point.y && point.y <= topRight.y)

    /**
     * Examines whether a given rectangle is within (or on top of) the four boundaries
     * of this rectangle.
     */
    case rectangle : Rectangle2D =>
      (bottomLeft.x <= rectangle.bottomLeft.x && rectangle.topRight.x <= topRight.x &&
         bottomLeft.y <= rectangle.bottomLeft.y && rectangle.topRight.y <= topRight.y)

    case g => throw new UnsupportedOperationException("Rectangle: Contains not yet implemented for " + g)
  }

  def distanceTo(geom : Geometry2D) = geom match {
    /**
     * Calculates the distance to a point.
     */
    case point : Vector2D =>
      Segment2D.segmentsOnClosedPathOfPoints(vertices.toSeq).view.map(
        _ distanceTo(point)
      ).reduceLeft( (a, b) => if (a < b) a else b)

    case _ => throw new UnsupportedOperationException("Rectangle: DistanceTo not yet implemented for " + geom)
  }

  def expand(geom : Geometry2D) : Rectangle2D = geom match {
    /**
     * Expands this rectangle to include an arc.
     * TODO: Not the right way to include an arc!
     */
    case arc : Arc2D => expand(Circle(arc.center, arc.radius))

    /**
     * Expands this rectangle to include a circle.
     */
    case circle : Circle2D =>
      expand(Rectangle2D(Vector(circle.center.x - circle.radius, circle.center.y + circle.radius),
                       Vector(circle.center.x + circle.radius, circle.center.y - circle.radius)))

    /**
     * Expands this rectangle to include a point.
     */
    case point : Vector2D => {
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
    case rect : Rectangle2D => {
      val newTopLeft     = Vector(scala.math.min(topLeft.x, rect.topLeft.x),
                                  scala.math.max(topLeft.y, rect.topLeft.y))
      val newBottomRight = Vector(scala.math.max(bottomRight.x, rect.bottomRight.x),
                                  scala.math.min(bottomRight.y, rect.bottomRight.y))
      Rectangle2D(newTopLeft, newBottomRight)
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
    case that : Rectangle2D =>
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
  def overlap(that : Rectangle2D) : Double = {
    if (intersects(that)) {
      val xMin = math.max(bottomLeft.x, that.bottomLeft.x)
      val yMin = math.max(bottomLeft.y, that.bottomLeft.y)
      val xMax = math.min(topRight.x, that.topRight.x)
      val yMax = math.max(topRight.y, topRight.y)
      (xMax - xMin) * (yMax - yMin)
    } else 0 // No overlap
  }

  lazy val vertices = Seq(topLeft, topRight, bottomRight, bottomLeft)

  def transform(t : TransformationMatrix) = {
    val p1 = topLeft.transform(t)
    val p2 = bottomRight.transform(t)

    Rectangle2D(p1, p2)
  }

  def width = (xMax - xMin).abs

}

/**
 * A companion object to Rectangle2D.
 */
object Rectangle2D {

  val NaN = java.lang.Double.NaN

  def empty = EmptyRectangle2D

  /**
   * An empty rectangle.
   */
  object EmptyRectangle2D extends Rectangle2D(NaN, NaN, NaN, NaN)

  def apply(v1 : Vector2D, v2 : Vector2D) =
    new Rectangle2D(math.min(v1.x, v2.x), math.min(v1.y, v2.y), math.max(v1.x, v2.x), math.max(v1.y, v2.y))
}