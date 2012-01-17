package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 14-01-12
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */

import java.sql._

class PgsqlSaveShapeLine {


  //SaveShapeLine: Modtager x1, y1, z1 og x2, y2, z2 koordinater (Int) og returnerer shapeId og pointId'er (Int)
  def postgresSaveShapeLine (x1Coordinate: Int, y1Coordinate: Int, z1Coordinate: Int,
                             x2Coordinate: Int, y2Coordinate: Int, z2Coordinate: Int): (Int,Int,Int) = {
    // Field variable definition
    var pointId1 : Option[Int] = None
    var pointId2 : Option[Int] = None
    var shapeId : Option[Int] = None
    var resultSequence: Seq[Int] = Seq()
    val savePoint = new PgsqlSavePoint()
    var query: String               ="0"

    //Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Checker om punkterne i shapen findes, ellers oprettes det:
    pointId1 = Some(savePoint.postgresSavePoint(x1Coordinate,y1Coordinate,z1Coordinate))
    pointId2 = Some(savePoint.postgresSavePoint(x2Coordinate,y2Coordinate,z2Coordinate))


     //Check om shapen (linien) findes i forvejen:
      //Finder shape-ids på de shapes, der indeholder begge punkter:
      if (pointId1.isDefined && pointId2.isDefined) {
        query =   "SELECT shape_id " +
                  "FROM shape_point_relation " +
                  "WHERE point_id = " + pointId1.get.toString +
                  "INTERSECT ALL " +
                  "SELECT shape_id " +
                  "FROM shape_point_relation " +
                  "WHERE point_id = " + pointId2.get.toString
        val queryResult: ResultSet = createStatement.executeQuery(query)
        while (queryResult.next()) {
          resultSequence = resultSequence :+ queryResult.getInt("shape_id")
        }

      //Finder den shape, der indeholder de to punkter, og er af typen linje (type-id 1), hvis den findes
        var i:Int =0
        while (resultSequence.isDefinedAt(i)) {
          query =   "SELECT shape_id " +
                    "FROM shape " +
                    "WHERE shape_type = 1 " +
                    "AND shape_id = " + resultSequence(i).toString
          val queryResult: ResultSet = createStatement.executeQuery(query)
          if (queryResult.next()) shapeId = Some(queryResult.getInt("shape_id"))
          i=i+1
        }
      }

    //Hvis shapen ikke findes, gemmes shapen, og shape-id returneres
    if (!shapeId.isDefined) {
      query =       "INSERT INTO shape " +
                    "(shape_type) " +
                    "VALUES" +
                    "(1)" +
                    "RETURNING shape_id"
      val queryResult: ResultSet = createStatement.executeQuery(query)
      if (queryResult.next()) shapeId = Some(queryResult.getInt("shape_id"))

      query =       "INSERT INTO shape_point_relation " +
                    "(shape_id, point_id)" +
                    "VALUES (" + shapeId.get.toString + "," + pointId1.get.toString + ")"
      createStatement.execute(query)
      query =       "INSERT INTO shape_point_relation " +
                    "(shape_id, point_id)" +
                    "VALUES (" + shapeId.get.toString + "," + pointId2.get.toString + ")"
      createStatement.execute(query)
    }
    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (shapeId.get,pointId1.get,pointId2.get)
  }
}