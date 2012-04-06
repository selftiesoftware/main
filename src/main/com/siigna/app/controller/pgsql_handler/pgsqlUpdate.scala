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

class pgsqlUpdate {

  //Modtager drawingId, det eksisterene shapeId samt en polylineshape.
  //Returnerer nyt shape Id (den oprindelige shape kunne bruges i en anden tegning - og den skal ikke ændres)
  def singlePolylineInCurrentDrawing (drawingId:Int,ShapeId:Int,shape:com.siigna.app.model.shape.PolylineShape) = {
    
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
    createStatement.execute(queryPoint)
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

    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (shapeIds(0))

  }

}
