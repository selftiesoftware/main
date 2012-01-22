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

    //GetShapesInArea: Modtager     x- og y-koordinat (Int), (default: 0,0)
    //                               samt afstand fra centerpunkt i x- og y-retning, der skal søges
    // og returnerer en sequence af: (shapeType1 (Int),shapeId1 (Int), shapeType2 (Int),shapeId2 (Int)...,...,...)
    def getShapesInArea (xCoordinate: Int, yCoordinate: Int, xDistance: Int, yDistance: Int) = {

      // Field variable definition
      var pointId : Option[Int] = None
      var shapeId : Option[Int] = None
      var resultSequence: Seq[Int] = Seq()
      var resultSequencePointId: Seq[Int] = Seq()
      var resultSequenceShapeId: Seq[Int] = Seq()
      var resultSequenceShapeType: Seq[Int] = Seq()

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
          resultSequencePointId = resultSequencePointId :+ queryResult.getInt("point_id")
        }

      //Finder de shapes, der indeholder punkterne
          query =       "SELECT DISTINCT shape_id " +
                        "FROM shape_point_relation " +
                        "WHERE "
            var i:Int =0
            while (resultSequencePointId.isDefinedAt(i)) {
              if (i==0) {
                query = query +
                        "point_id = " +
                        resultSequencePointId(i).toString
              } else {
                query = query +
                        "OR point_id = " +
                        resultSequencePointId(i).toString
              }
              i=i+1
            }
          val queryResult2: ResultSet = createStatement.executeQuery(query)
          while (queryResult2.next()) {
            resultSequenceShapeId = resultSequenceShapeId :+ queryResult2.getInt("shape_id")
          }

      //Finder shape_id for shapsene:
      i = 0
      query =       "SELECT shape_type " +
                    "FROM shape " +
                    "WHERE "
      while (resultSequenceShapeId.isDefinedAt(i)) {
        if (i==0) {
          query = query +
                    "shape_id = " +
            resultSequenceShapeId(i).toString
        } else {
          query = query +
                    " OR shape_id = " +
            resultSequenceShapeId(i).toString
        }
        i=i+1
      }
      val queryResult3: ResultSet = createStatement.executeQuery(query)
      while (queryResult3.next()) {
        resultSequenceShapeType = resultSequenceShapeType :+ queryResult3.getInt("shape_type")
      }

      i=0
      while (resultSequenceShapeType.isDefinedAt(i)) {
        resultSequence = resultSequence :+ resultSequenceShapeType(i)
        resultSequence = resultSequence :+ resultSequenceShapeId(i)
        i=i+1
      }

      //Luk forbindelsen
      createStatement.close()

      //Data, der returneres

      (resultSequence)
    }
}
