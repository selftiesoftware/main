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

  def allShapesInDrawingFromDrawingIdWithoutDatabaseId (drawingId: Int) = {

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
    var resultSequenceShapes: Seq[ImmutableShape] = Seq()
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
        shapeIdIterator.next()
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),Vector2D(coordinatesIterator.next(),coordinatesIterator.next()))
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 3 => {
        //For polylines skal findes startpunkt, seq af subshapes samt attributter:
        shapeIdIterator.next()
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
        shapeIdIterator.next()
        resultSequenceShapes = resultSequenceShapes :+ new com.siigna.app.model.shape.CircleShape(Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),propertyIntValueIterator.next(),attributes)
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 6 => {
        shapeIdIterator.next()
        resultSequenceShapes = resultSequenceShapes :+ new ArcShape(Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),propertyIntValueIterator.next(),propertyIntValueIterator.next(),propertyIntValueIterator.next(),attributes)
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case 7 => {
        shapeIdIterator.next()
        resultSequenceShapes = resultSequenceShapes :+ new TextShape(propertyTextValueIterator.next(),Vector2D(coordinatesIterator.next(),coordinatesIterator.next()),propertyIntValueIterator.next(),attributes)
        numberOfPropertyInts = numberOfPropertyIntsIterator.next()
      }
      case x => println ("Ukendt shape")
    })

    val polylineInfoIterator = polylineInfo.iterator

    while (polylineInfoIterator.hasNext) {
      val polylineNumberOfSubshapes = polylineInfoIterator.next()
      polylineSubshapeSeq = Seq()
      for (i <- 0 until polylineNumberOfSubshapes) {
        polylineSubshapeSeq = polylineSubshapeSeq :+ polylineSubshapes(polylineInfoIterator.next())
      }
      resultSequenceShapes = resultSequenceShapes :+ PolylineShape(Vector2D(polylineInfoIterator.next(),polylineInfoIterator.next()),polylineSubshapeSeq,attributes)
    }

      //Luk forbindelsen
    createStatement.close()

    //Data, der returneres
    (resultSequenceShapes)
  }

  //Modtager      x- og y-koordinat (Int),
  //              samt afstand fra centerpunkt i x- og y-retning (Int), der skal søges
  //Returnerer    en sequence af instantierede shapes:   (Shape1, Shape2,..., ShapeN)
  def getShapes (xCoordinate: Int, yCoordinate: Int, zCoordinate: Int, xDistance: Int, yDistance: Int, zDistance: Int) = {

    // Field variable definition
    var query: String               ="0"
    var i: Int = 0
    var j: Int = 0
    var shapeType : Option[Int] = None
    var shapeId : Option[Int] = None

    var pointIdSequence: Seq[Int] = Seq()
    var pointCoordinatesLocationSequence: Seq[Int] = Seq()

    var resultSequenceShapeIdPointIdShapeId: Seq[Int] = Seq()
    var resultSequenceShapeIdPointIdPointId: Seq[Int] = Seq()

    var resultSequencePointIdXYZCoordinatesPointId: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesX: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesY: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesZ: Seq[Int] = Seq()

    var resultSequenceShapes: Seq[ImmutableShape] = Seq()

    //Virker ikke: Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()

    //Opretter forbindelse til databasen og laver createStatement variabel.
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Finder shape id og tilhørende point id for shapes, der har et eller flere punkter i det angivne område (søgning A):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_point_relation.shape_id, shape_point_relation.point_id  " +
      "FROM shape_point_relation " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape_point_relation.shape_id = alias2.shape_id"
    val queryResultShapeIdPointId: ResultSet = createStatement.executeQuery(query)
    while (queryResultShapeIdPointId.next()) {
      resultSequenceShapeIdPointIdShapeId = resultSequenceShapeIdPointIdShapeId :+ queryResultShapeIdPointId.getInt("shape_id")
      resultSequenceShapeIdPointIdPointId = resultSequenceShapeIdPointIdPointId :+ queryResultShapeIdPointId.getInt("point_id")
    }

    //Finder punkt id og tilhørende koordinater for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.

    query =   "SELECT DISTINCT point.point_id, x_coordinate, y_coordinate, z_coordinate " +
              "FROM point " +
              "JOIN " +
              "(shape_point_relation " +
                  "JOIN " +
                  "(shape_point_relation " +
                      "JOIN " +
                      "(SELECT point_id " +
                      "FROM point " +
                      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
                          "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
                          "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
                      "AS alias " +
                      "ON  shape_point_relation.point_id = alias.point_id) " +
                  "AS alias2 " +
                  "ON shape_point_relation.shape_id = alias2.shape_id) " +
              "ON point.point_id = shape_point_relation.point_id"

    val queryResultPointIdCoordinates: ResultSet = createStatement.executeQuery(query)
    while (queryResultPointIdCoordinates.next()) {
      resultSequencePointIdXYZCoordinatesPointId = resultSequencePointIdXYZCoordinatesPointId :+ queryResultPointIdCoordinates.getInt("point_id")
      resultSequencePointIdXYZCoordinatesX = resultSequencePointIdXYZCoordinatesX :+ queryResultPointIdCoordinates.getInt("x_coordinate")
      resultSequencePointIdXYZCoordinatesY = resultSequencePointIdXYZCoordinatesY :+ queryResultPointIdCoordinates.getInt("y_coordinate")
      resultSequencePointIdXYZCoordinatesZ = resultSequencePointIdXYZCoordinatesZ :+ queryResultPointIdCoordinates.getInt("z_coordinate")
    }

    //Finder shape type og tilhørende shape id for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_type, shape.shape_id  " +
              "FROM shape " +
              "JOIN " +
              "(shape_point_relation " +
                  "JOIN " +
                  "(SELECT point_id " +
                  "FROM point " +
                  "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
                      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
                      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
                  "AS alias " +
                  "ON  shape_point_relation.point_id = alias.point_id) " +
              "AS alias2 " +
              "ON shape.shape_id = alias2.shape_id"
    val queryResultShapeTypeShapeId: ResultSet = createStatement.executeQuery(query)

    //Finder de shapes, der er returneret
    while (queryResultShapeTypeShapeId.next()) {
      //Henter shape-id
      shapeId = Some(queryResultShapeTypeShapeId.getInt("shape_id"))
      //Henter shape-type
      shapeType = Some(queryResultShapeTypeShapeId.getInt("shape_type"))
      //laver liste over de steder i resultSequencePointIdXYZCoordinatesX/Y/Z hvor koordinaterne til punkterne i shapen findes
        //Først skal punkt-id findes ud fra shape id:
          i=0
          while (resultSequenceShapeIdPointIdShapeId.isDefinedAt(i)) {
            if (resultSequenceShapeIdPointIdShapeId(i) == shapeId.get  ) {
              pointIdSequence = pointIdSequence :+ resultSequenceShapeIdPointIdPointId(i)
            }
          i=i+1
          }
        //Så kan placeringen af punkterne i resultSequencePointIdXYZCoordinates findes ud fra point id:
          i=0
          while (pointIdSequence.isDefinedAt(i)) {
            j=0
            while (resultSequencePointIdXYZCoordinatesPointId.isDefinedAt(j)) {
              if (pointIdSequence(i) == resultSequencePointIdXYZCoordinatesPointId(j)) {
                pointCoordinatesLocationSequence = pointCoordinatesLocationSequence :+ j
              }
              j=j+1
            }
          i=i+1
          }

      //Punkt-shape:
      if (shapeType.get == 1)
        {}
      //Line-shape:
      if (shapeType.get == 2)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Polyline-shape:
      if (shapeType.get == 3)
      {

      }
      //Cirkel-shape:
      if (shapeType.get == 4)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Arc-shape:
      if (shapeType.get == 5)
      {}

    //Nulstiller sequences til brug for næste gennemløb af løkken, hvor næste shape findes frem.
      pointIdSequence = Seq()
      pointCoordinatesLocationSequence = Seq()
    }

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres
    resultSequenceShapes.foreach( shape => println (shape))

    (resultSequenceShapes)
  }

  def getShapesInArea (xCoordinate: Int, yCoordinate: Int, zCoordinate: Int, xDistance: Int, yDistance: Int, zDistance: Int) = {

    // Field variable definition
    var query: String               ="0"
    var i: Int = 0
    var j: Int = 0
    var shapeType : Option[Int] = None
    var shapeId : Option[Int] = None

    var pointIdSequence: Seq[Int] = Seq()
    var pointCoordinatesLocationSequence: Seq[Int] = Seq()

    var resultSequenceShapeIdPointIdShapeId: Seq[Int] = Seq()
    var resultSequenceShapeIdPointIdPointId: Seq[Int] = Seq()

    var resultSequencePointIdXYZCoordinatesPointId: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesX: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesY: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesZ: Seq[Int] = Seq()

    var resultSequenceShapes: Seq[ImmutableShape] = Seq()

    //Virker ikke: Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()

    //Opretter forbindelse til databasen og laver createStatement variabel.
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Finder shape id og tilhørende point id for shapes, der har et eller flere punkter i det angivne område (søgning A):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_point_relation.shape_id, shape_point_relation.point_id  " +
      "FROM shape_point_relation " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape_point_relation.shape_id = alias2.shape_id"
    val queryResultShapeIdPointId: ResultSet = createStatement.executeQuery(query)
    while (queryResultShapeIdPointId.next()) {
      resultSequenceShapeIdPointIdShapeId = resultSequenceShapeIdPointIdShapeId :+ queryResultShapeIdPointId.getInt("shape_id")
      resultSequenceShapeIdPointIdPointId = resultSequenceShapeIdPointIdPointId :+ queryResultShapeIdPointId.getInt("point_id")
    }

    //Finder punkt id og tilhørende koordinater for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.

    query =   "SELECT DISTINCT point.point_id, x_coordinate, y_coordinate, z_coordinate " +
      "FROM point " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape_point_relation.shape_id = alias2.shape_id) " +
      "ON point.point_id = shape_point_relation.point_id"

    val queryResultPointIdCoordinates: ResultSet = createStatement.executeQuery(query)
    while (queryResultPointIdCoordinates.next()) {
      resultSequencePointIdXYZCoordinatesPointId = resultSequencePointIdXYZCoordinatesPointId :+ queryResultPointIdCoordinates.getInt("point_id")
      resultSequencePointIdXYZCoordinatesX = resultSequencePointIdXYZCoordinatesX :+ queryResultPointIdCoordinates.getInt("x_coordinate")
      resultSequencePointIdXYZCoordinatesY = resultSequencePointIdXYZCoordinatesY :+ queryResultPointIdCoordinates.getInt("y_coordinate")
      resultSequencePointIdXYZCoordinatesZ = resultSequencePointIdXYZCoordinatesZ :+ queryResultPointIdCoordinates.getInt("z_coordinate")
    }

    //Finder shape type og tilhørende shape id for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_type, shape.shape_id  " +
      "FROM shape " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape.shape_id = alias2.shape_id"
    val queryResultShapeTypeShapeId: ResultSet = createStatement.executeQuery(query)

    //Finder de shapes, der er returneret
    while (queryResultShapeTypeShapeId.next()) {
      //Henter shape-id
      shapeId = Some(queryResultShapeTypeShapeId.getInt("shape_id"))
      //Henter shape-type
      shapeType = Some(queryResultShapeTypeShapeId.getInt("shape_type"))
      //laver liste over de steder i resultSequencePointIdXYZCoordinatesX/Y/Z hvor koordinaterne til punkterne i shapen findes
      //Først skal punkt-id findes ud fra shape id:
      i=0
      while (resultSequenceShapeIdPointIdShapeId.isDefinedAt(i)) {
        if (resultSequenceShapeIdPointIdShapeId(i) == shapeId.get  ) {
          pointIdSequence = pointIdSequence :+ resultSequenceShapeIdPointIdPointId(i)
        }
        i=i+1
      }
      //Så kan placeringen af punkterne i resultSequencePointIdXYZCoordinates findes ud fra point id:
      i=0
      while (pointIdSequence.isDefinedAt(i)) {
        j=0
        while (resultSequencePointIdXYZCoordinatesPointId.isDefinedAt(j)) {
          if (pointIdSequence(i) == resultSequencePointIdXYZCoordinatesPointId(j)) {
            pointCoordinatesLocationSequence = pointCoordinatesLocationSequence :+ j
          }
          j=j+1
        }
        i=i+1
      }

      //Punkt-shape:
      if (shapeType.get == 1)
      {}
      //Line-shape:
      if (shapeType.get == 2)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Polyline-shape:
      if (shapeType.get == 3)
      {}
      //Cirkel-shape:
      if (shapeType.get == 4)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Arc-shape:
      if (shapeType.get == 5)
      {}

      //Nulstiller sequences til brug for næste gennemløb af løkken, hvor næste shape findes frem.
      pointIdSequence = Seq()
      pointCoordinatesLocationSequence = Seq()
    }

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (resultSequenceShapes)
  }
}
