package com.siigna.app.model

import org.khelekore.prtree.{PRTree, MBRConverter}
import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D, Rectangle2D}
import scala.collection.JavaConversions._

/**
 * A wrapper for the java <a href="http://en.wikipedia.org/wiki/Priority_R-tree" title="PRTrees on Wikipedia">priority
 * r-tree</a> found at <a href="http://www.khelekore.org/prtree/">http://www.khelekore.org/prtree/</a>.
 */
object SiignaTree {

  type LeftType = Int
  type RightType = Shape
  type TreeType = (LeftType,RightType)

  /**
   * A [[org.khelekore.prtree.MBRConverter]] used to convert siigna types to Minimum Bounding Rectangles (MBR).
   */
  val converter = new MBRConverter[SiignaTree.TreeType] {

    override def getDimensions = 2

    override def getMin(a:Int, x: SiignaTree.TreeType) = {
      if (a==0)
        x._2.geometry.boundary.xMin
      else
        x._2.geometry.boundary.yMin
    }

    override def getMax(a:Int,x: SiignaTree.TreeType) = {
      if (a==0)
        x._2.geometry.boundary.xMax
      else
        x._2.geometry.boundary.yMax

    }
  }

  /**
   * Creates a new <a href="http://en.wikipedia.org/wiki/Priority_R-tree" title="PRTrees on Wikipedia">priority
   * r-tree</a>.
   * @param m  The map to insert into the RTree.
   * @return  A [[org.khelekore.prtree.PRTree]]
   */
  def apply(m: Map[LeftType, RightType]) : PRTree[SiignaTree.TreeType] = {
    val t = new PRTree[SiignaTree.TreeType](converter,2)
    t.load(asJavaCollection(m))
    t
  }

  /**
   * Searches the given tree for shapes inside the given query rectangle. This method mainly works as a wrapper
   * between the java and scala collection-types.
   * @param query  The rectangle query used as a delimiter for the search area.
   * @param rtree  The r-tree to search.
   * @return A Map of the found shapes and their id's.
   */
  def find(query: SimpleRectangle2D, rtree:PRTree[(LeftType,RightType)]):Map[LeftType,RightType]= {
    iterableAsScalaIterable[TreeType](
      rtree.find(
        query.bottomLeft.x,
        query.bottomLeft.y,
        query.topRight.x,
        query.topRight.y
      )
    ).toMap
  }

  /**
   * Searches the given tree for shapes within the radius of the given point. This method mainly works as a wrapper
   * between the java and scala collection-types.
   * @param point  The point to use as center for the search.
   * @param radius  The rectangle query to search for shapes inside.
   * @param rtree  The r-tree to search.
   * @return A Map of the found shapes and their id's.
   */
  def find(point: Vector2D,radius:Double,rtree:PRTree[(LeftType,RightType)]):Map[LeftType,RightType]= {
    iterableAsScalaIterable[TreeType](
      rtree.find(
        point.x-radius,
        point.y-radius,
        point.x+radius,
        point.y+radius
      )
    ).toMap
  }

  /**
   * Finds the minimum bounding rectangle (MBR) of the given tree and converts it to a
   * [[com.siigna.util.geom.SimpleRectangle2D]].
   * @param rtree  The tree whose MBR to use.                   .
   * @return  A [[com.siigna.util.geom.SimpleRectangle2D]] if the MBR was not null, otherwise an empty rectangle
   *          (0, 0, 0, 0).
   */
  def mbr(rtree:PRTree[(LeftType,RightType)]):SimpleRectangle2D = {
    val box = rtree.getMBR
    if (box == null)
      SimpleRectangle2D(0,0,0,0)
    else
      SimpleRectangle2D(box.getMin(0),box.getMin(1),box.getMax(0),box.getMax(1))

  }
}
