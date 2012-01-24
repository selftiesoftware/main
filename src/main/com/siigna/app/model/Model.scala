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

package com.siigna.app.model

import collection.generic.SeqForwarder
import collection.immutable.HashMap
import collection.mutable.ArrayBuffer

import com.siigna.app.Siigna
import com.siigna.util.logging.Log
import shape.{GroupShape, ImmutableShape, Shape}
import com.siigna.util.rtree.PRTree
import com.siigna.util.geom.{Rectangle2D, Rectangle, Vector2D}

/**
 * The model of Siigna which contains a sequence of <i>immutable</i> shapes associated with ID's
 * (<code>var static : HashMap[String, Shape]</code>)
 * and a <i>Prioritized R Tree</i> (<code>val tree : PRTree[String]</code>) for graphic searches.
 * The <i>immutable</i> Seq represents the drawing as it looks like in the Siigna universe - the version everybody sees.
 * The Prioritized R-tree is used for fast spatial queries through the shapes.
 * The Model extends ImmutableModel to easy the implementation, if you wanted to include the trait, but change any functions.
 *
 * TODO: Make parallel!
 * TODO: Create a thread for actions.
 * TODO: Remove touple-addition and use String and Shape separately!
 * TODO: Remove "+" operations.
 * TODO: Optimize group-operations. Possibly let the group shapes operate as regular shapes.
 */
class Model extends GroupableModel with SeqForwarder[ImmutableShape] {

  /**
   * Groups defined in the model.
   */
  val groups = ArrayBuffer[GroupShape]()

  /**
   * This hashmap contains every shape in the model.
   */
  var static  = HashMap[String, ImmutableShape]()

  /**
   * Creates a new prioritized R-tree associated with strings used to store the id of the shapes.
   */
  val tree = new PRTree()

  /**
   * Add a shape to the model. <b>Assuming that the shape does not exist in the Model!</b>
   */
  def + (elem : (String, ImmutableShape)) : Model = {
    try {
      static = static + elem
      tree add (elem._1, elem._2.boundary)
    } catch {
      case e => Log.error("Model: Insertion failed. Unknown error: "+e)
    }
    this
  }

  /**
   * Adds several shapes to the model.
   */
  def ++ (elems : Traversable[(String, ImmutableShape)]) : Model = {
    try {
      static = static.++(elems.toMap).asInstanceOf[HashMap[String, ImmutableShape]]
      tree.add(elems.map(e => (e._1, e._2.boundary)).toMap)
    } catch {
      case e => Log.error("Model: Insertion failed. Unkown error: "+e)
    }
    this
  }

  /**
   * Remove a shape by it's index in the model, assuming the shape exists.
   */
  def - (key : String) : Model = {
    try {
      val shape = static(key)
      static = static - key
      tree remove (key, shape.boundary)
    } catch {
      case e : NoSuchElementException => Log.warning("Model: Removal failed. Could not find the given shape in the model: "+e)
      case e => Log.warning("Model: Removal failed. Unknown error encountered: "+e)
    }
    this
  }

  /**
   * Removes several shapes from the model.
   */
  def -- (keys : Traversable[String]) : Model = {
    try {
      val elems : Map[String, Rectangle2D] = keys.collect{case key => (key -> static(key).boundary)}.toMap
      static = static -- keys
      tree remove elems
    } catch {
      case e : NoSuchElementException => Log.warning("Model: Removals failed. Could not find a given shape in the model: "+e)
      case e => Log.warning("Model: Removals failed. Unknown error encountered: "+e)
    }
    this
  }

  /**
   * Prints the model as a (possibly very long) string. You're warned...
   */
  override def toString() = "Model[ Shapes: ("+static.toString+"),\n"+
                          "Prioritized R-Tree: "+tree.toString+" ]"

  /**
   * The underlying Seq as required by the SeqForwarder trait.
   */
  def underlying = static.values.toSeq

  /**
   * Updates the shape with the associated key id.
   */
  def update(id : String, shape : ImmutableShape) : Model = {
    try {
      val oldShape = static(id)
      static = static.updated(id, shape)
      tree remove (id, oldShape.boundary)
      tree add (id, shape.boundary)
    } catch {
      case e : NoSuchElementException => Log.error("Update failed. Could not find the given shape in the model: "+e)
      case e => Log.error("Update failed. Unknown error encountered: "+e)
    }
    this
  }

  /**
   * Updates the shape with the associated key id with a given function.
   */
  def update(id : String, f : (ImmutableShape) => ImmutableShape) : Model = {
    try {
      val oldShape = static(id)
      val newShape = f(oldShape)
      static = static.updated(id, newShape)
      tree remove (id, oldShape.boundary)
      tree add (id, newShape.boundary)
    } catch {
      case e : NoSuchElementException => Log.error("Model: Update failed. Could not find the given shape in the model: "+e)
      case e => Log.error("Model: Update failed. Unknown error encountered: "+e)
    }
    this
  }

  /**
   * Updates a number of shapes associated with a key with a function.
   * TODO: Optimize
   */
  def update(ids : Traversable[String], f : (ImmutableShape) => ImmutableShape) : Model = {
    ids.foreach(update(_, f))
    this
  }

}

/**
 * The globally accessible Model.
 */
object Model extends DynamicModel with SeqForwarder[ImmutableShape] {

  /**
   * Returns the immutable shape associated with the given ids.
   */
  def apply(id : String) : Shape = {
    if (mutableShapes.contains(id))
      mutableShapes(id).shape
    else
      model.static.apply(id)
  }

  /**
   * Searches the model for every shape included in or touched by the Rectangle2D.
   */
  def apply(Rectangle2D : Rectangle2D) : Traversable[Shape] = apply(Rectangle2D, false)

  /**
   * Searches the model for every shape included in or touched by the Rectangle2D, with a flag that determines whether groups
   * should be included in the search.
   */
  def apply(Rectangle2D : Rectangle2D, includeGroups : Boolean = false) : Iterable[Shape] = try {
    if (!model.static.isEmpty) {
      val ids = if (includeGroups) {
        var ids = model.tree(Rectangle2D)
        model.groups.foreach(g => if (Rectangle2D.intersects(g.boundary)) { // TODO: Search for ids not boundaries!
          ids = ids ++ g.ids
        })
        ids.toSeq.distinct
      } else {
        model.tree(Rectangle2D)
      }
      // Replace shapes with dynamic shapes if they exist.
      ids.map(id => if (mutableShapes.contains(id)) mutableShapes(id) else model.static(id))
    } else Seq[Shape]()
  } catch {
    case e => Log.warning("Model: Query failed. Returning empty sequence. Unknown error: "+e)
    Seq[Shape]()
  }

  /**
   * Searches the model for the closest shape to the given point.
   */
  def apply(point : Vector2D) : Option[Shape] = try {
    val res = apply(point, 20)
    if (res.isEmpty)
      None
    else Some(res.reduceLeft((a, b) => if (a.distanceTo(point) <= b.distanceTo(point)) a else b))
  } catch {
    case e => Log.error("Model: Error occurred while retrieving shapes from a given point.", e)
    None
  }

  /**
   * Searches the model for shapes included in or touched by the point +/- a given margin.
   */
  def apply(point : Vector2D, margin : Double) : Traversable[Shape] = {
    val mbr = Rectangle2D(point.x - margin, point.y - margin, point.x + margin, point.y + margin)
    apply(mbr)
  }

  /**
   * The boundary from the current content of the Model.
   * The rectangle returned fits an A-paper format, but <b>without margin</b>.
   * This is done in order to make sure that the print viewed on page is the
   * actual print you get.
   *
   * @return A rectangle in an A-paper format (margin exclusive). The scale is given in <code>boundaryScale</code>.
   */
  def boundary = {
    val newBoundary  = model.tree.mbr
    val size         = (newBoundary.bottomRight - newBoundary.topLeft).abs
    val center       = (newBoundary.bottomRight + newBoundary.topLeft) / 2
    //val proportion   = 1.41421356

    // Saves the format, as the format with the margin subtracted
    var aFormatMin = Siigna.printFormatMin
    var aFormatMax = Siigna.printFormatMax

    // If the format is too small for the least proportion, then up the size
    // one format.
    // TODO: Optimize!
    val list = List[Double](2, 2.5, 2)
    var take = 0
    while (aFormatMin < scala.math.min(size.x, size.y) || aFormatMax < scala.math.max(size.x, size.y)) {
      val factor = list.apply(take)
      aFormatMin *= factor
      aFormatMax *= factor
      take = if (take < 2) take + 1 else 0
    }

    // Set the boundary-rectangle.
    if (size.x >= size.y) {
      Rectangle2D(Vector2D(center.x - aFormatMax * 0.5, center.y - aFormatMin * 0.5),
                Vector2D(center.x + aFormatMax * 0.5, center.y + aFormatMin * 0.5))
    } else {
      Rectangle2D(Vector2D(center.x - aFormatMin * 0.5, center.y - aFormatMax * 0.5),
                Vector2D(center.x + aFormatMin * 0.5, center.y + aFormatMax * 0.5))
    }
  }

  /**
   * Uses toInt since it always rounds down to an integer.
   */
  def boundaryScale = (scala.math.max(boundary.width, boundary.height) / Siigna.printFormatMax).toInt

  /**
   * The underlying dynamic shapes (selected shapes).
   */
  def dynamicShapes = mutableShapes

  /**
   * Returns the id for the given shape.
   * TODO: MapForwarder
   */
  def findId(f : (ImmutableShape) => Boolean) : Option[String] = {
    val res = model.static.find(s => f(s._2))
    if (res.isDefined)
      Some(res.get._1)
    else
      None
  }

  /**
   * Returns a list with ids for the given shapes.
   */
  def findIds(shapes : Traversable[ImmutableShape]) : Traversable[String] =
    shapes.map(s => findId(_ == s)).filter(_ == None).map(_.get)

  /**
   * The underlying immutable model.
   */
  def immutable = model.static

  /**
   * The underlying immutable shapes.
   */
  def shapes = underlying

  /**
   * Prints the model as a (possibly very long) string. You're warned...
   */
  override def toString = model.toString()

  /**
   * The underlying mutable model.
   */
  def underlying = model.static.values.toSeq

  /**
   * Searches the model for static shapes closes to a given point excluding <code>Groups</code> and <code>DynamicShapes</code>.
   */
  def queryForShapes(point : Vector2D) : Option[ImmutableShape] = {
    val res = queryForShapes(point, 10)
    if (res.isEmpty)
      None
    else Some(res.reduceLeft((a, b) => if (a.distanceTo(point) <= b.distanceTo(point)) a else b))
  }

  /**
   * Searches the model for static shapes from a point with a given margin excluding <code>Groups</code> and <code>DynamicShapes</code>.
   */
  def queryForShapes(point : Vector2D, margin : Double) : Iterable[ImmutableShape] =
    queryForShapes(Rectangle2D(point.x - margin, point.y - margin, point.x + margin, point.y + margin))

  /**
   * Searches the model for static shapes excluding <code>Groups</code> and <code>DynamicShapes</code>.
   */
  def queryForShapes(Rectangle2D : Rectangle2D) : Iterable[ImmutableShape] = try {
    if (!model.static.isEmpty) {
      model.tree(Rectangle2D).map(model.static)
    } else {
      Seq[ImmutableShape]()
    }
  } catch {
    case e => Log.warning("Model: Query failed. Returning empty sequence. Unknown error: "+e)
    Seq[ImmutableShape]()
  }

  /**
   * Searches the model for static shapes associated with an id, excluding <code>Groups</code> and <code>DynamicShapes</code>.
   */
  def queryForShapesWithId(Rectangle2D : Rectangle2D) : Map[String, ImmutableShape] = try {
    if (!model.static.isEmpty) {
      model.tree(Rectangle2D).map(i => i -> model.static(i)).toMap
    } else {
      Map[String, ImmutableShape]()
    }
  } catch {
    case e => Log.warning("Model: Query failed. Returning empty map. Unknown error: "+e)
    Map[String, ImmutableShape]()
  }

  /**
   * Searches the model for every shape included in or touched by the Rectangle2D. Includes a flag whether to include groups
   * in the search.
   */
  def queryWithId(mbr : Rectangle2D, includeGroups : Boolean = false) : Map[String, Shape] = try {
    if (!model.static.isEmpty) {
      val ids = if (includeGroups) {
        var ids = model.tree(mbr)
        model.groups.foreach(g => if (mbr.intersects(g.boundary)) {
          ids = ids ++ g.ids
        })
        ids.toSeq.distinct
      } else {
        model.tree(mbr)
      }
      // Replace shapes with dynamic shapes if they exist.
      ids.map(id => if (mutableShapes.contains(id)) (id -> mutableShapes(id)) else (id -> model.static(id))).toMap
    } else Map[String, Shape]()
  } catch {
    case e => Log.warning("Model: Query failed. Returning empty sequence. Unknown error: "+e)
    Map[String, Shape]()
  }

}