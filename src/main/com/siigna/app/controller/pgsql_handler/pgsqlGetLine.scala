package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 22-01-12
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */

import java.sql._

class pgsqlGetLine {

  //getLine: Modtager      shapeId og returnerer (PointId1, x1-, y1- og z1-koordinat (Int), PointId2, x2-, y2- og z2-koordinat (Int)).
  def getLine (shapeId: Int) = {

    // Field variable definition
    var pointId1 : Option[Int] = None
    var pointId2 : Option[Int] = None
    var x1coordinate : Option[Int] = None
    var y1coordinate : Option[Int] = None
    var z1coordinate : Option[Int] = None
    var x2coordinate : Option[Int] = None
    var y2coordinate : Option[Int] = None
    var z2coordinate : Option[Int] = None
    var query: String              ="0"

    //Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Finder point-ids på de points, der er i shapen:
    query =   "SELECT point_id " +
      "FROM shape_point_relation " +
      "WHERE shape_id = " +
      shapeId.toString
    val queryResult: ResultSet = createStatement.executeQuery(query)
    queryResult.next()
    pointId1 = Some(queryResult.getInt("point_id"))
    queryResult.next()
    pointId2 = Some(queryResult.getInt("point_id"))

    //Finder koordinater til punkterne
    query =       "SELECT x_coordinate, y_coordinate, z_coordinate " ++
                  "FROM point " ++
                  "WHERE point_id = " ++ pointId1.get.toString ++
                  " OR point_id = " ++ pointId2.get.toString
    val queryResult2: ResultSet = createStatement.executeQuery(query)
    queryResult2.next()
      x1coordinate = Some(queryResult2.getInt("x_coordinate"))
      y1coordinate = Some(queryResult2.getInt("y_coordinate"))
      z1coordinate = Some(queryResult2.getInt("z_coordinate"))
    queryResult2.next()
      x2coordinate = Some(queryResult2.getInt("x_coordinate"))
      y2coordinate = Some(queryResult2.getInt("y_coordinate"))
      z2coordinate = Some(queryResult2.getInt("z_coordinate"))

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (pointId1.get,x1coordinate.get,y1coordinate.get,z1coordinate.get,pointId2.get,x2coordinate.get,y2coordinate.get,z2coordinate.get)
  }
}