package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 18-01-12
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */

import java.sql._

class pgsqlGetShapesInArea {

    //SaveShapeLine: Modtager     x- og y-koordinat (Int), (default: 0,0)
    //                            samt afstand fra centerpunkt i x- og y-retning, der skal søges
    // og returnerer en sequence af: (Markør for ny shape (0), shapeType (Int), shapeId (Int), pointId'er (Int))
    def getShapesInArea (xCoordinate: Int, yCoordinate: Int, xDistance: Int, yDistance: Int) = {

      // Field variable definition
      var pointId : Option[Int] = None
      var shapeId : Option[Int] = None
      var resultSequence: Seq[Int] = Seq()

      var query: String               ="0"

      //Henter objektet med forbindelsesoplysninger
      //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()
      var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
      var createStatement: Statement = databaseConnection.createStatement()

      //Finder point-ids på de points, der er indenfor det søgte område:
        query =   "SELECT point_id " +
                  "FROM point " +
                  "WHERE (x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
                  " AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") "
        val queryResult: ResultSet = createStatement.executeQuery(query)
        while (queryResult.next()) {
          resultSequencePointId = resultSequence :+ queryResult.getInt("shape_id")
        }

        //Finder de linjer, der indeholder punkterne
        var i:Int =0
        while (resultSequencePointId.isDefinedAt(i)) {
          query =   "SELECT shape_id " +
            "FROM shape_point_relation " +
            "WHERE shape_type = 2 " +
            "AND point_id = " + resultSequence(i).toString
          val queryResult: ResultSet = createStatement.executeQuery(query)
          while (queryResult.next()) {
            resultSequenceShapeId = resultSequence :+ queryResult.getInt("shape_id")
          }
        }


      /*
      //Hvis shapen ikke findes, gemmes shapen, og shape-id returneres
      if (!shapeId.isDefined) {
        query =       "INSERT INTO shape " +
          "(shape_type) " +
          "VALUES" +
          "(2)" +
          "RETURNING shape_id"
        val queryResult: ResultSet = createStatement.executeQuery(query)
        if (queryResult.next()) shapeId = Some(queryResult.getInt("shape_id"))

        query =       "INSERT INTO shape_point_relation " +
          "(shape_id, point_id) " +
          "VALUES (" + shapeId.get.toString + "," + pointId1.get.toString + ") "
        createStatement.execute(query)
        query =       "INSERT INTO shape_point_relation " +
          "(shape_id, point_id) " +
          "VALUES (" + shapeId.get.toString + "," + pointId2.get.toString + ")"
        createStatement.execute(query)
      }
      //Luk forbindelsen
      createStatement.close()

      //Data, der returneres

      (shapeId.get,pointId1.get,pointId2.get)
      */
    }
  }
