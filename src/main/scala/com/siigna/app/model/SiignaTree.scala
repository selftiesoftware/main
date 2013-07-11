package com.siigna.app.model

import org.khelekore.prtree.{PRTree, MBRConverter}
import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D, Rectangle2D}
import scala.collection.JavaConversions._

object SiignaTree {

  type leftType = Int
  type rightType = Shape
  type treeType = (leftType,rightType)

  /**
   * Return a map from a bounding box and a tree
   */
  def find(query: Rectangle2D,rtree:PRTree[(leftType,rightType)]):Map[leftType,rightType]= {

    iterableAsScalaIterable[treeType](
      rtree.find(
        query.bottomLeft.x,
        query.bottomLeft.y,
        query.topRight.x,
        query.topRight.y
      )
    ).foldLeft(Map[leftType,rightType]())((m,t) => m.updated(t._1,t._2))

  }

  /**
   * Return a map from a bounding box and a tree
   */
  def find(point: Vector2D,radius:Double,rtree:PRTree[(leftType,rightType)]):Map[leftType,rightType]= {

    iterableAsScalaIterable[treeType](
      rtree.find(
        point.x-radius,
        point.y-radius,
        point.x+radius,
        point.y+radius
      )
    ).foldLeft(Map[leftType,rightType]())((m,t) => m.updated(t._1,t._2))

  }

  def mbr(rtree:PRTree[(leftType,rightType)]):SimpleRectangle2D = {

    val box = rtree.getMBR()
    if (box == null)
      SimpleRectangle2D(0,0,0,0)
    else
      SimpleRectangle2D(box.getMin(0),box.getMin(1),box.getMax(0),box.getMax(1))

  }

  def apply(m:Map[SiignaTree.leftType, SiignaTree.rightType]) = {
    val st = new SiignaTree(m)

    st.apply
  }
}

class SiignaTree(shapes:Map[SiignaTree.leftType, SiignaTree.rightType]) {

  val converter = new MBRConverter[SiignaTree.treeType] {

    override def getDimensions = 2

    override def getMin(a:Int, x: SiignaTree.treeType) = {
      if (a==0)
        x._2.geometry.boundary.xMin
      else
        x._2.geometry.boundary.yMin
    }

    override def getMax(a:Int,x: SiignaTree.treeType) = {

      if (a==0)
        x._2.geometry.boundary.xMax
      else
        x._2.geometry.boundary.yMax

    }

  }

  def apply = {
    val t = new PRTree[SiignaTree.treeType](converter,2)
    t.load(asJavaCollection(shapes))
    t
  }

}
