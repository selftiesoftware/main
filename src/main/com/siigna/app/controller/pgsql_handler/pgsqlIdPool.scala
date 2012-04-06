package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 05-04-12
 * Time: 06:56
 * To change this template use File | Settings | File Templates.
 */

import java.sql._

object pgsqlIdPool {

  var shapeIds: Seq[Int] = Seq()
  var drawingIds: Seq[Int] = Seq()

  def getNewShapeId() = {
    if (shapeIds.length < 5) {
      val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
      val createStatement: Statement = databaseConnection.createStatement()
      for (i <- 0 until 10) {
        val query: String = "INSERT INTO shape (shape_type) VALUES (0) RETURNING shape_id"
        val queryResult: ResultSet = createStatement.executeQuery(query)
        while (queryResult.next()) {
          shapeIds = shapeIds :+ queryResult.getInt("shape_id")
        }
      }
      databaseConnection.close()
    }
    val shapeId = shapeIds.head
    shapeIds = shapeIds.tail

    //Data, der returneres
    (shapeId)
  }

  def getNewDrawingId() = {
    if (drawingIds.length < 2) {
      val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
      val createStatement: Statement = databaseConnection.createStatement()
      for (i <- 0 until 3) {
        val query: String = "INSERT INTO drawing (drawing_name) VALUES ("Unnamed") RETURNING drawing_id"
        val queryResult: ResultSet = createStatement.executeQuery(query)
        while (queryResult.next()) {
          drawingIds = drawingIds :+ queryResult.getInt("shape_id")
        }
      }
      databaseConnection.close()
    }
    val drawingId = drawingIds.head
    drawingIds = drawingIds.tail

    (drawingId)
  }
}
