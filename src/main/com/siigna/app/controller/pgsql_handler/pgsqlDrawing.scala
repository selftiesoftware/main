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

    var query = "INSERT INTO drawing (drawing_name,drawing_offset_x,drawing_offset_y,drawing_offset_z) " +
      "VALUES (" +  drawingName + "," + offsetX + "," + offsetY + "," + offsetZ + "RETURNING drawing_id"

    val queryResult: ResultSet = createStatement.executeQuery(query)
    queryResult.next()
    val drawingId = queryResult.getInt("point_id")

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (drawingId)

  }

  //Returnerer en sequence af drawingName1,drawingId1,drawingName2,drawingName2, osv.
  def getAllDrawingNamesAndIds() = {

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
}