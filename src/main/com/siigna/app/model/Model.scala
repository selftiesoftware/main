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

package com.siigna.app.model


import action.CreateShape
import com.siigna.app.Siigna
import com.siigna.util.geom.{Vector2D, Rectangle2D}
import shape.{Shape}
import collection.immutable.MapProxy
import com.siigna.app.controller.{Controller, AppletParameters}
import com.siigna.app.controller.remote.{RemoteAction, GetNewShapeIds}
import com.siigna.util.collection.Attributes

/**
 * An immutable model with two layers: an static and dynamic.
 * <br />
 * The static part is basically a long list of all the
 * [[com.siigna.app.model.shape.Shape]]s and their keys in the Model.
 * <br />
 * The dynamic part allows selecting parts of the global immutable layer. These shapes can be altered
 * without changes in the static layer which allows for significant performance benefits. When the
 * changes have been made (and the shapes are deselected), the shapes are removed from the dynamic
 * layer, and the actions which have been applied on the dynamic layer is applied on the static layer.
 *
 * @param shapes  The shapes and their identifiers (keys) stored in the model.
 *
 * TODO: Examine possibility to implement an actor. Thread-less please.
 */
sealed class Model(val shapes : Map[Int, Shape]) extends ImmutableModel[Int, Shape]
                                                                    with MutableModel
                                                                    with SpatialModel[Int, Shape]
                                                                    with ModelBuilder[Int, Shape] {

  def build(coll : Map[Int, Shape]) = new Model(coll)

}

/**
 * The model of Siigna.
 */
object Model extends ActionModel
                with SelectableModel
                with SpatialModel[Int, Shape]
                with MapProxy[Int, Shape] {

  var localShapeId: Int = 0
  var shapeIdBank: Seq[Int] = Seq()
  var shapesWithLocalShapeId: Seq[Int] = Seq()

  /**
   * The attributes of the model containing name, title, owner and other attributes
   * fetched from the server, necessary for Siigna.
   */
  var attributes = Attributes()

  /**
   * Hvis der er od'er i "banken: Returnerer en shapeId fra "banken".
   * Hvis der ikke er gives et "lokalt id", og dette føjes til listen over shapes med lokale id'er.
   * Kontrollerer, hvor mange id'er, der er tilbage i banken.
   * Hvis der er under et vist antal anmodes om "en ny portion".
   * @return
   */
  def getNewShapeId = {
    var shapeId: Int = -1

    if (shapeIdBank.length > 0) {
      shapeId = shapeIdBank.head
      shapeIdBank = shapeIdBank.tail
    } else {
      shapeId = localShapeId
      shapesWithLocalShapeId = shapesWithLocalShapeId :+ localShapeId
      localShapeId = localShapeId - 1
    }

    val client = AppletParameters.getClient
    if (shapeIdBank.length < 2 && client.isDefined) {
      GetNewShapeIds(2, client.get)
    }
    shapeId
  }

  /**
   * Saves a sequence of new shapeIds into the shapeIdBank variable
   * @param shapeIds
   */
  def receiveNewShapeIds(shapeIds: Seq[Int]) {
    shapeIdBank = shapeIdBank ++ shapeIds
    shapesWithLocalShapeId.foreach(localId => {
      if (shapeIdBank.length > 0){
        //Funktion, der opdaterer lokalt id med id fra serveren. Mangler metode...
        val bankId = shapeIdBank.head
        shapeIdBank = shapeIdBank.tail

        val shape = Model(localId)
        model remove localId
        model.add(bankId, shape)

        //Funktion, der gemmer og videresender de shapes, der nu har fået en global id
        val client = AppletParameters.getClient
        if (client.isDefined) {
          Controller ! RemoteAction(AppletParameters.drawingId.get, client.get, CreateShape(bankId, shape))
        }

        // Remove the id from the local bank
        shapesWithLocalShapeId = shapesWithLocalShapeId.tail
      }
    })
    println("shapeIdBank is now, after replacement of local Ids: "+shapeIdBank)
    println("Shapes with local Ids are now: " + shapesWithLocalShapeId)
    val client = com.siigna.app.controller.AppletParameters.getClient
    if (shapeIdBank.length < 5 && client.isDefined) {
      GetNewShapeIds(10, client.get)
    }
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
    val newBoundary  = model.mbr
    val size         = (newBoundary.bottomRight - newBoundary.topLeft).abs
    val center       = newBoundary.center
    //val proportion   = 1.41421356

    // Saves the format, as the format with the margin subtracted
    val printMargin = Siigna.double("printMargin").getOrElse(13.0)
    var aFormatMin = Siigna.double("printFormatMin").getOrElse(210.0) - printMargin
    var aFormatMax = Siigna.double("printFormatMax").getOrElse(297.0) - printMargin

    // If the format is too small for the least proportion, then up the size
    // one format.
    // TODO: Optimize!
    val list = List[Double](2, 2.5, 2)
    var take = 0 // which element to "take" from the above list
    while (aFormatMin < scala.math.min(size.x, size.y) || aFormatMax < scala.math.max(size.x, size.y)) {
      val factor = list(take)
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
  def boundaryScale = (scala.math.max(boundary.width, boundary.height) / Siigna.double("printFormatMax").getOrElse(297.0)).toInt
  
  /**
   * The [[com.siigna.util.rtree.PRTree]] used by the model.
   */
  //def rtree = model.rtree

  /**
   * The current selection represented by a an Option of [[com.siigna.app.model.Selection]].
   * @return  Some(Selection) if a selection is active or None if nothing has been selected
   */
  def selection : Option[Selection] = model.selection

  /**
   * The shapes currently in the model.
   * @return A ParMap containing shapes.
   */
  def shapes = model.shapes

  //------------- Required by the MapProxy trait -------------//
  def self = model.shapes

}