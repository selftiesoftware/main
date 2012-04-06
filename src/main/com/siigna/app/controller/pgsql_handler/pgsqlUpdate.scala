package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 06-04-12
 * Time: 08:03
 * To change this template use File | Settings | File Templates.
 */

import java.sql._
import com.siigna.app.model.shape.LineShape

object pgsqlUpdate {

  //Modtager drawingId, det eksisterene shapeId samt en polylineshape.
  //Returnerer nyt shape Id (den oprindelige shape kunne bruges i en anden tegning - og den skal ikke ændres)
  def singlePolylineInDrawing (drawingId:Int,ShapeId:Int,shape:com.siigna.app.model.shape.PolylineShape) = {
    
    //Opretter forbindelse til serveren
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    //Variable deklareres:
    var shapeIds: Seq[Int] = Seq()
    var pointIds: Seq[Int] = Seq()
    var propertyIntIds: Seq[Int] = Seq()

    //Først slettes den gamle drawing-shape-relation
    val query: String = "DELETE FROM drawing_shape_relation WHERE shape_id = " + ShapeId
    createStatement.execute(query)

    //Shape og Point ekspederes:
    var queryShape = "INSERT INTO shape (shape_type,number_of_property_ints) VALUES (3,"+shape.shapes.length+"),"
    var queryPoint = "INSERT INTO point (x_coordinate, y_coordinate, z_coordinate) VALUES (" + shape.startPoint.x + "," + shape.startPoint.y + ",0),"
    //Subshapes i polylinjen gennemgås:
    shape.shapes.foreach(subShape => subShape match {
      case subShape : com.siigna.app.model.shape.LineShape => {
        queryShape += "(4,0),"
        queryPoint += "("+subShape.p2.x.toInt+","+subShape.p2.y.toInt+",0),"          
      }
    })
    queryShape = queryShape.take(queryShape.length-1)
    queryShape += " RETURNING shape_id"
    queryPoint = queryPoint.take(queryPoint.length-1)
    queryPoint += " RETURNING point_id"

    val queryResultShape: ResultSet = createStatement.executeQuery(queryShape)
    while (queryResultShape.next()) {
      shapeIds = shapeIds :+ queryResultShape.getInt("shape_id")
    }
    val queryResultPoint: ResultSet = createStatement.executeQuery(queryPoint)
    while (queryResultPoint.next()) {
      pointIds = pointIds :+ queryResultPoint.getInt("point_id")
    }

    //drawing_shape_relation, shape_point_relation samt property_int:
    queryShape = "INSERT INTO drawing_shape_relation (drawing_id,shape_id) VALUES "
    queryPoint = "INSERT INTO shape_point_relation (shape_id,point_id) VALUES "
    var queryPropertyInt = "INSERT INTO property_int (property_int_number,property_int_value) VALUES "
    for (i <- 0 until shapeIds.length) {
      queryShape += "("+drawingId+","+shapeIds(i)+"),"
      queryPoint += "("+shapeIds(i)+","+pointIds(i)+"),"
      queryPropertyInt += "("+(1000+i)+","+shapeIds(i)+"),"
    }
    queryShape = queryShape.take(queryShape.length-1)
    createStatement.execute(queryShape)
    queryPoint = queryPoint.take(queryPoint.length-1)
    createStatement.execute(queryPoint)
    queryPropertyInt = queryPropertyInt.take(queryPropertyInt.length-1)
    queryPropertyInt += " RETURNING property_int_id"
    val queryResultPropertyInt: ResultSet = createStatement.executeQuery(queryPropertyInt)
    while (queryResultPropertyInt.next()) {
      propertyIntIds = propertyIntIds :+ queryResultPropertyInt.getInt("property_int")
    }
    //ShapePropertyIntRelation:
    var queryShapePropertyIntRelation = "INSERT INTO shape_property_int_relation (shape_id,property_int_id) VALUES "
    propertyIntIds.foreach (propertyInt => queryShapePropertyIntRelation += "("+shapeIds(0)+","+propertyInt+"),")
    queryShapePropertyIntRelation = queryShapePropertyIntRelation.take(queryShapePropertyIntRelation.length-1)
    createStatement.execute(queryShapePropertyIntRelation)
    println ("Polyline shape updated in database")

    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (shapeIds(0))

  }


  def renameDrawing(drawingId:Int,DrawingName:String) {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    val query:String = "UPDATE drawing SET drawing_name = "+DrawingName+" WHERE drawing_id = "+drawingId
    createStatement.execute(query)
    println ("Drawing renamed in database")
    databaseConnection.close()
  }

  def singleLineshapeInDrawing (drawingId:Int,ShapeId:Int,newShape: com.siigna.app.model.shape.LineShape) = {

    //Opretter forbindelse til serveren
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()
    //Først slettes den gamle drawing-shape-relation
    val query: String = "DELETE FROM drawing_shape_relation WHERE shape_id = " + ShapeId
    createStatement.execute(query)
    //Shape og Point ekspederes:
    var queryShape = "INSERT INTO shape (shape_type,number_of_property_ints) VALUES (2,0) RETURNING shape_id"
    var queryPoint = "INSERT INTO point (x_coordinate, y_coordinate, z_coordinate) VALUES (" + newShape.p1.x + "," + newShape.p1.y + ",0),(" + newShape.p2.x + "," + newShape.p2.y + ",0) RETURNING point_id"

    val queryResultShape: ResultSet = createStatement.executeQuery(queryShape)
    queryResultShape.next()
    val newShapeId:Int = queryResultShape.getInt("shape_id")

    val queryResultPoint: ResultSet = createStatement.executeQuery(queryPoint)
    queryResultPoint.next()
    val pointId1 = queryResultPoint.getInt("point_id")
    val pointId2 = queryResultPoint.getInt("point_id")

    //drawing_shape_relation, shape_point_relation:
    queryShape = "INSERT INTO drawing_shape_relation (drawing_id,shape_id) VALUES ("+drawingId+","+newShapeId+")"
    queryPoint = "INSERT INTO shape_point_relation (shape_id,point_id) VALUES ("+newShapeId+","+pointId1+"),("+newShapeId+","+pointId2+")"
    createStatement.execute(queryShape)
    createStatement.execute(queryPoint)
    println ("Line shape updated in database")

    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (newShapeId)

  }

  def singleCircleShapeInDrawing (drawingId:Int,ShapeId:Int,newShape: com.siigna.app.model.shape.CircleShape) = {

    //Opretter forbindelse til serveren
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()
    //Først slettes den gamle drawing-shape-relation
    val query: String = "DELETE FROM drawing_shape_relation WHERE shape_id = " + ShapeId
    createStatement.execute(query)
    //Shape og Point ekspederes:
    var queryShape = "INSERT INTO shape (shape_type,number_of_property_ints) VALUES (5,1) RETURNING shape_id"
    var queryPoint = "INSERT INTO point (x_coordinate, y_coordinate, z_coordinate) VALUES (" + newShape.center.x + "," + newShape.center.y + ",0) RETURNING point_id"
    var queryPropertyInt = "INSERT INTO property_int (property_int_number,property_int_value) VALUES (1000,"+newShape.radius+") RETURNING property_int_id"

    val queryResultShape: ResultSet = createStatement.executeQuery(queryShape)
    queryResultShape.next()
    val newShapeId:Int = queryResultShape.getInt("shape_id")

    val queryResultPoint: ResultSet = createStatement.executeQuery(queryPoint)
    queryResultPoint.next()
    val pointId = queryResultPoint.getInt("point_id")

    val queryResultPropertyInt: ResultSet = createStatement.executeQuery(queryPropertyInt)
    queryResultPropertyInt.next()
    val propertyIntId:Int = queryResultPropertyInt.getInt("property_int_id")

    //drawing_shape_relation, shape_point_relation:
    queryShape = "INSERT INTO drawing_shape_relation (drawing_id,shape_id) VALUES ("+drawingId+","+newShapeId+")"
    queryPoint = "INSERT INTO shape_point_relation (shape_id,point_id) VALUES ("+newShapeId+","+pointId+")"
    queryPropertyInt = "INSERT INTO shape_property_int_relation (shape_id,property_int_id) VALUES ("+newShapeId+","+propertyIntId+")"
    createStatement.execute(queryShape)
    createStatement.execute(queryPoint)
    createStatement.execute(queryPropertyInt)

    println ("Circle shape updated in database")

    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (newShapeId)

  }

  def singleArcShapeInDrawing (drawingId:Int,ShapeId:Int,newShape: com.siigna.app.model.shape.ArcShape) = {

    //Opretter forbindelse til serveren
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()
    //Først slettes den gamle drawing-shape-relation
    val query: String = "DELETE FROM drawing_shape_relation WHERE shape_id = " + ShapeId
    createStatement.execute(query)
    //Shape og Point ekspederes:
    var queryShape = "INSERT INTO shape (shape_type,number_of_property_ints) VALUES (6,3) RETURNING shape_id"
    var queryPoint = "INSERT INTO point (x_coordinate, y_coordinate, z_coordinate) VALUES (" + newShape.center.x + "," + newShape.center.y + ",0) RETURNING point_id"
    var queryPropertyInt = "INSERT INTO property_int (property_int_number,property_int_value) VALUES (1000,"+newShape.radius+"),(1001,"+newShape.startAngle+"),(1002,"+newShape.angle+") RETURNING property_int_id"

    val queryResultShape: ResultSet = createStatement.executeQuery(queryShape)
    queryResultShape.next()
    val newShapeId:Int = queryResultShape.getInt("shape_id")

    val queryResultPoint: ResultSet = createStatement.executeQuery(queryPoint)
    queryResultPoint.next()
    val pointId = queryResultPoint.getInt("point_id")

    val queryResultPropertyInt: ResultSet = createStatement.executeQuery(queryPropertyInt)
    queryResultPropertyInt.next()
    val propertyIntId1:Int = queryResultPropertyInt.getInt("property_int_id")
    queryResultPropertyInt.next()
    val propertyIntId2:Int = queryResultPropertyInt.getInt("property_int_id")
    queryResultPropertyInt.next()
    val propertyIntId3:Int = queryResultPropertyInt.getInt("property_int_id")

    //drawing_shape_relation, shape_point_relation:
    queryShape = "INSERT INTO drawing_shape_relation (drawing_id,shape_id) VALUES ("+drawingId+","+newShapeId+")"
    queryPoint = "INSERT INTO shape_point_relation (shape_id,point_id) VALUES ("+newShapeId+","+pointId+")"
    queryPropertyInt = "INSERT INTO shape_property_int_relation (shape_id,property_int_id) VALUES ("+newShapeId+","+propertyIntId1+"),("+newShapeId+","+propertyIntId2+"),("+newShapeId+","+propertyIntId3+")"
    createStatement.execute(queryShape)
    createStatement.execute(queryPoint)
    createStatement.execute(queryPropertyInt)

    println ("Arc shape updated in database")

    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (newShapeId)

  }

}
