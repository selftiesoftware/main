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

package com.siigna.app.controller.pgsql_handler


import java.sql._
import com.siigna.app.model.shape._
import com.siigna.util.geom.Vector2D

object pgsqlGet {

  //Returnerer map af id -> immutableshape
  def allShapesInDrawingFromDrawingIdWithDatabaseId (drawingId: Int) = {

    //Opretter forbindelse til databasen og laver createStatement variabel.
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    var drawing_name: String = ""
    var drawingOffsetX: Int = 0
    var drawingOffsetY: Int = 0
    var drawingOffsetZ: Int = 0
    var resultSequenceShapeType: Seq[Int] = Seq()
    var resultSequenceShapeId: Seq[Int] = Seq()
    var resultSequenceNumberOfPropertyInts: Seq[Int] = Seq()
    var resultSequencePropertyIntNumber: Seq[Int] = Seq()
    var resultSequencePropertyIntValue: Seq[Int] = Seq()
    var resultSequencePropertyTextNumber: Seq[Int] = Seq()
    var resultSequencePropertyTextValue: Seq[String] = Seq()
    var resultSequenceCoordinates: Seq[Int] = Seq()
    var polylineInfo: Seq[Int] = Seq()
    var resultSequenceShapes: Map[Int,ImmutableShape] = Map()
    var polylineSubshapes: Map[Int,com.siigna.app.model.shape.PolylineShape.InnerPolylineShape] = Map()
    var polylineSubshapeSeq: Seq[com.siigna.app.model.shape.PolylineShape.InnerPolylineShape] = Seq()
    val attributes: com.siigna.util.collection.Attributes = com.siigna.util.collection.Attributes()

    val query1: String = "" +
      "SELECT drawing_name,drawing_offset_x,drawing_offset_y,drawing_offset_z " +
      "FROM drawing " +
      "WHERE drawing_id = " + drawingId

    val query2: String = "" +
      "SELECT t2.shape_type,t2.shape_id,t2.number_of_property_ints " +
      "FROM shape as t2 " +
      "JOIN (" +
      "SELECT shape_id "+
      "FROM drawing_shape_relation " +
      "WHERE drawing_id = " + drawingId + ") as t1 "+
      "ON t1.shape_id = t2.shape_id"

    val query3: String = "" +
      "SELECT property_int_number,property_int_value " +
      "FROM property_int as t3 " +
      "JOIN (shape_property_int_relation as t2 " +
      "JOIN (" +
      "SELECT shape_id "+
      "FROM drawing_shape_relation " +
      "WHERE drawing_id = " + drawingId + ") as t1 " +
      "ON t1.shape_id = t2.shape_id) " +
      "ON t2.property_int_id = t3.property_int_id"
    val query4: String = "" +
      "SELECT x_coordinate,y_coordinate " +
      "FROM point as t3 " +
      "JOIN (shape_point_relation as t2 " +
      "JOIN (" +
      "SELECT shape_id "+
      "FROM drawing_shape_relation " +
      "WHERE drawing_id = " + drawingId + ") as t1 " +
      "ON t1.shape_id = t2.shape_id) " +
      "ON t2.point_id = t3.point_id"
    val query5: String = "" +
      "SELECT property_text_number,property_text_value " +
      "FROM property_text as t3 " +
      "JOIN (shape_property_text_relation as t2 " +
      "JOIN (" +
      "SELECT shape_id "+
      "FROM drawing_shape_relation " +
      "WHERE drawing_id = " + drawingId + ") as t1 " +
      "ON t1.shape_id = t2.shape_id) " +
      "ON t2.property_text_id = t3.property_text_id"

    createStatement.execute(query1)
    val queryResult1: ResultSet = createStatement.getResultSet
    while (queryResult1.next()) {
      drawing_name = queryResult1.getString("drawing_name")
      drawingOffsetX = queryResult1.getInt("drawing_offset_x")
      drawingOffsetY = queryResult1.getInt("drawing_offset_y")
      drawingOffsetZ = queryResult1.getInt("drawing_offset_z")
    }

    createStatement.execute(query2)
    val queryResult2: ResultSet = createStatement.getResultSet
    while (queryResult2.next()) {
      resultSequenceShapeType = resultSequenceShapeType :+ queryResult2.getInt("shape_type")
      resultSequenceShapeId = resultSequenceShapeId :+ queryResult2.getInt("shape_id")
      resultSequenceNumberOfPropertyInts = resultSequenceNumberOfPropertyInts :+ queryResult2.getInt("number_of_property_ints")
    }
    resultSequenceNumberOfPropertyInts = resultSequenceNumberOfPropertyInts :+ 0


    createStatement.execute(query3)
    val queryResult3: ResultSet = createStatement.getResultSet
    while (queryResult3.next()) {
      resultSequencePropertyIntNumber = resultSequencePropertyIntNumber :+ queryResult3.getInt("property_int_number")
      resultSequencePropertyIntValue = resultSequencePropertyIntValue :+ queryResult3.getInt("property_int_value")
    }

    createStatement.execute(query4)
    val queryResult4: ResultSet = createStatement.getResultSet
    while (queryResult4.next()) {
      resultSequenceCoordinates = resultSequenceCoordinates :+ queryResult4.getInt("x_coordinate")
      resultSequenceCoordinates = resultSequenceCoordinates :+ queryResult4.getInt("y_coordinate")
      //resultSequenceCoordinates = resultSequenceCoordinates :+ queryResult4.getInt("z_coordinate")
    }
    createStatement.execute(query5)
    val queryResult5: ResultSet = createStatement.getResultSet
    while (queryResult5.next()) {
      resultSequencePropertyTextNumber = resultSequencePropertyTextNumber :+ queryResult3.getInt("property_text_number")
      resultSequencePropertyTextValue = resultSequencePropertyTextValue :+ queryResult3.getString("property_text_value")
    }

    val shapeIdIterator = resultSequenceShapeId.iterator

    val numberOfPropertyIntsIterator = resultSequenceNumberOfPropertyInts.iterator
    var numberOfPropertyInts = numberOfPropertyIntsIterator.next()
    val propertyIntValueIterator = resultSequencePropertyIntValue.iterator
    val propertyTextValueIterator = resultSequencePropertyTextValue.iterator
    val coordinatesIterator = resultSequenceCoordinates.iterator

    resultSequenceShapeType.foreach(shapeType => shapeType match {
      case 1 => println("Point")
      case 2 => {
        resultSequenceShapes += (shapeIdIterator.next() -> LineShape(Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),Vector2D(coordinatesIterator.next(),coordinatesIterator.next())))
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 3 => {
        //For polylines skal findes startpunkt, seq af subshapes samt attributter:
        polylineInfo = polylineInfo :+ shapeIdIterator.next()
        polylineInfo = polylineInfo :+ numberOfPropertyInts
        for (i <- 0 until numberOfPropertyInts) {
          polylineInfo = polylineInfo :+ propertyIntValueIterator.next()
        }
        polylineInfo = polylineInfo :+ coordinatesIterator.next()
        polylineInfo = polylineInfo :+ coordinatesIterator.next()
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 4 => {
        polylineSubshapes += (shapeIdIterator.next() -> new PolylineShape.PolylineLineShape(Vector2D(coordinatesIterator.next(),coordinatesIterator.next())))
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 5 => {
        resultSequenceShapes += (shapeIdIterator.next() -> new com.siigna.app.model.shape.CircleShape(Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),propertyIntValueIterator.next(),attributes))
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 6 => {
        resultSequenceShapes += (shapeIdIterator.next() -> new ArcShape(Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),propertyIntValueIterator.next(),propertyIntValueIterator.next(),propertyIntValueIterator.next(),attributes))
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 7 => {
        resultSequenceShapes += (shapeIdIterator.next() -> new TextShape(propertyTextValueIterator.next(),Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),propertyIntValueIterator.next(),attributes))
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case x => println ("Ukendt shape")
    })

    val polylineInfoIterator = polylineInfo.iterator

    while (polylineInfoIterator.hasNext) {
      val polylineId = polylineInfoIterator.next()
      val polylineNumberOfSubshapes = polylineInfoIterator.next()
      polylineSubshapeSeq = Seq()
      for (i <- 0 until polylineNumberOfSubshapes) {
        polylineSubshapeSeq = polylineSubshapeSeq :+ polylineSubshapes(polylineInfoIterator.next())
      }
      resultSequenceShapes += (polylineId -> PolylineShape(Vector2D(polylineInfoIterator.next(),polylineInfoIterator.next()),polylineSubshapeSeq,attributes))
    }

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres
    (resultSequenceShapes)
  }

  //Returnerer en sequence af drawingName1,drawingId1,drawingName2,drawingName2, osv.
  def allDrawingNamesAndIds() = {

    var resultSequence: Seq[Int] = Seq()

    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    var query = "SELECT drawing_name,drawing_id FROM drawing"

    val queryResult: ResultSet = createStatement.executeQuery(query)
    queryResult.next()
    val drawingId = queryResult.getInt("point_id")

    while (queryResult.next()) {
      resultSequence = resultSequence :+ queryResult.getInt("drawing_name")
      resultSequence = resultSequence :+ queryResult.getInt("drawing_id")
    }

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (resultSequence)
  }

  def contributorNameFromId(id:Int) = {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    val query:String = "SELECT contributor_name FROM contributor WHERE contributor_id = "+id
    val resultSet: ResultSet = createStatement.executeQuery(query)
    resultSet.next()

    databaseConnection.close()
    println ("Contributor name for contributor with id "+id+" retrieved from database")

    (resultSet.getString("contributor_name"))
  }

  def contributorIdFromNameAndPassword(name:String,password:String) = {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    val query:String = "SELECT contributor_id FROM contributor WHERE contributor_name = "+name+" AND contributor_password = "+password
    val resultSet: ResultSet = createStatement.executeQuery(query)
    resultSet.next()

    databaseConnection.close()
    println ("Contributor id for contributor with supplied contributorname and password retrieved from database")

    (resultSet.getString("contributor_id"))
  }

  def contributorsLastActiveDrawing(userId:Int) = {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()
    var drawingId: Option[Int] = None

    val query:String = "SELECT property_int_value FROM property_int as t3 JOIN (contributor as t2 JOIN contributor_basic_property_int_relation as t1 ON t1.contributor_id = t2.contributor_id) ON t1.property_int_id = t3.property_int_id WHERE property_int_number=1 AND t2.contributor_id = "+userId
    val queryResult: ResultSet = createStatement.executeQuery(query)
    while (queryResult.next()) {
        drawingId = Some(queryResult.getInt("property_int_value"))
    }
    databaseConnection.close()
    (drawingId)
  }

  def drawingNameFromId(drawingId:Int) = {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    val query:String = "SELECT drawing_name FROM drawing WHERE drawing_id = "+drawingId
    val resultSet: ResultSet = createStatement.executeQuery(query)
    resultSet.next()

    databaseConnection.close()
    println ("Drawing name for drawing with id "+drawingId+" retrieved from database")
    
    (resultSet.getString("drawing_name"))
  }

  def drawingDataFromId(drawingId:Int) = {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    val query:String = "SELECT drawing_name,drawing_offset_x,drawing_offset_y,zdrawing_offset_z FROM drawing WHERE drawing_id = "+drawingId
    val resultSet: ResultSet = createStatement.executeQuery(query)
    resultSet.next()

    databaseConnection.close()

    println ("Drawing name for drawing with id "+drawingId+" retrieved from database")

    (resultSet.getString("drawing_name"),resultSet.getString("drawing_offset_x"),resultSet.getString("drawing_offset_y"),resultSet.getString("drawing_offset_z"))
  }

  def drawingIdFromName(drawingName:String) = {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    val query:String = "SELECT drawing_id FROM drawing WHERE drawing_name = '"+drawingName+"'"
    val resultSet: ResultSet = createStatement.executeQuery(query)
    var resultSequence: Seq[Int] = Seq()
    while (resultSet.next()) {
      resultSequence = resultSequence :+ resultSet.getInt("drawing_id")
      println ("Drawing id for drawing with name "+drawingName+" retrieved from database")
    }

    println ("Retrieved drawing name from id")
    databaseConnection.close()

    (resultSequence)
  }
}
