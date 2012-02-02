package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 22-01-12
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */

import java.sql._

class pgsqlSaveShapePolyline {


  //SaveShapePolyline: Modtager sekvens af ((x1, y1, z1), (x2, y2, z2)- koordinater osv. (Int) 
  //                  og returnerer shapeId (Int) og liste af pointId'er (Int)
  def postgresSaveShapePolyline (coordinates: Seq[Int]) = {
    
    // Field variable definition
    var pointIdListe : Seq[Int] = Seq()
    var xCoordinate : Option[Int] = None
    var yCoordinate : Option[Int] = None
    var zCoordinate : Option[Int] = None
    var pointId : Option[Int] = None
    var shapeId : Option[Int] = None
    var resultSequence: Seq[Int] = Seq()
    val savePoint = new PgsqlSavePoint()
    var query: String               ="0"


    //Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Checker om punkterne i shapen findes, ellers oprettes de. Der laves en liste over points.
    var i: Int =0
    while (coordinates.isDefinedAt(i)) {
      xCoordinate = Some(coordinates(i))
      i=i+1
      yCoordinate = Some(coordinates(i))
      i=i+1
      zCoordinate = Some(coordinates(i))
      i=i+1
      pointId = Some(savePoint.postgresSavePoint(xCoordinate.get,yCoordinate.get,zCoordinate.get))

      pointIdListe = pointIdListe :+ pointId.get
    }


    //Check om shapen (polylinjen) findes i forvejen:
    //Finder shape-ids på evt. shapes, der indeholder alle punkter:
    i=0
      query =   "SELECT shape_id " +
        "FROM shape_point_relation " +
        "WHERE point_id = "
        while (pointIdListe.isDefinedAt(i)) {
          if (i==0) {
            query = query +
              pointIdListe(i).toString
          } else {
            query = query +
              " INTERSECT ALL " +
              "SELECT shape_id " +
              "FROM shape_point_relation " +
              "WHERE point_id = " + pointIdListe(i).toString
          }
          i=i+1
        }
      val queryResult: ResultSet = createStatement.executeQuery(query)
      while (queryResult.next()) {
        resultSequence = resultSequence :+ queryResult.getInt("shape_id")
      }

      //Finder den shape, der indeholder punkterne, og er af typen polylinje (type-id 3), hvis den findes
      i=0
      while (resultSequence.isDefinedAt(i)) {
        query =   "SELECT shape_id " +
          "FROM shape " +
          "WHERE shape_type = 3 " +
          "AND shape_id = " + resultSequence(i).toString
        val queryResult: ResultSet = createStatement.executeQuery(query)
        if (queryResult.next()) shapeId = Some(queryResult.getInt("shape_id"))
        i=i+1
      }

    //Hvis shapen ikke findes, gemmes shapen, og shape-id returneres
    if (!shapeId.isDefined) {
      query =       "INSERT INTO shape " +
        "(shape_type) " +
        "VALUES" +
        "(3)" +
        "RETURNING shape_id"
      val queryResult: ResultSet = createStatement.executeQuery(query)
      if (queryResult.next()) shapeId = Some(queryResult.getInt("shape_id"))

      i=0
      while (pointIdListe.isDefinedAt(i)) {
        if (i==0) {
                      query = "INSERT INTO shape_point_relation " +
                              "(shape_id, point_id) " +
                              "VALUES " +
                              "(" + shapeId.get.toString + "," + pointIdListe(i).toString + ")"
        } else {
                      query = query +
                               ",(" + shapeId.get.toString + "," + pointIdListe(i).toString + ")"
        }
        i=i+1
      }

      createStatement.execute(query)
    }
    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (shapeId.get,pointIdListe)
  }
}