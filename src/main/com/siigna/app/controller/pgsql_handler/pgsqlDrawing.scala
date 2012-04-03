package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 22-01-12
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */

import java.sql._

class pgsqlDrawing {

  //getLine: Modtager      shapeId og returnerer (PointId1, x1-, y1- og z1-koordinat (Int), PointId2, x2-, y2- og z2-koordinat (Int)).
  def createNewDrawingWithName (drawingName: String, offsetX: Int, offsetY: Int, offsetZ: Int) = {

    // Field variable definition

    //Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Finder point-ids på de points, der er i shapen:
    query =   "INSERT INTO drawing (drawing_name,drawing_offset_x,drawing_offset_y,drawing_offset_z) " +
      "VALUES (" +  drawingName + "," + offsetX + "," + offsetY + "," + offsetZ + "RETURNING drawing_id"

    val queryResult: ResultSet = createStatement.executeQuery(query)
    queryResult.next()
    val drawingId = queryResult.getInt("point_id")

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (drawingId)
  }
}