package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 20-02-12
 * Time: 19:42
 * To change this template use File | Settings | File Templates.
 */

import java.sql._
import com.siigna.app.model.Model
import com.siigna.app.model.shape.LineShape


//import java.lang.String

class pgsqlSaveLine {

  Class.forName("org.postgresql.Driver")

  //(data) field declarations:
  var pointId: Int                =0
  var query: String               ="0"
  //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()

  // methods:

  //Save point: Modtager x, y og z-koordinater (Int), og returnerer pointId (Int)
  def saveLine (/*coordinates: Seq[Int]*/) = {

    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    var XCoordinates: Seq[Int] = Seq()
    var YCoordinates: Seq[Int] = Seq()
    var ZCoordinates: Seq[Int] = Seq()
    var punkterDerFindesAllerede: Seq[Int] = Seq()

    // Her skal funktionen, der laver sekvensen baseret på metoder til shapes fra modellen, være.

    //Laver søgestreng, der finder id på de punkter, der findes i forvejen

    println("jada")
    val aaa = Model.seq
    var i=0
    while (aaa.isDefinedAt(i)) {
      if (aaa(i).getClass.toString == "class com.siigna.app.model.shape.LineShape") {
        var j = aaa(i)
        println ("Line")
        println (j)
        println (j.getClass)
        var k = j.attributes
        println (k.getClass)
        //var l = j.
        //println (l)
        //LineShape(Vector2D(1894.0,2375.0),Vector2D(1594.0,2375.0),Attributes())
      }
      if (aaa(i).getClass.toString == "class com.siigna.app.model.shape.PolylineShape") {
        println ("Polyline")
      }
      i=i+1
    }

    println("jada2")

    /*
    query = "SELECT point_id " +
      "FROM point "
    coordinates.foreach {

    }

      "WHERE x_coordinate = " + xCoordinateString + " AND y_coordinate = " + yCoordinateString + " AND z_coordinate = " + zCoordinateString



    var queryResult: ResultSet = createStatement.executeQuery(query)
    if (queryResult.next()) {pointId = queryResult.getInt("point_id")
    } else {
      //Hvis det ikke findes, indsættes det i databasen, og pointId returneres
      query = "INSERT INTO point " ++
        "(x_coordinate, y_coordinate, z_coordinate) " ++
        "VALUES " ++
        "(" ++ xCoordinateString ++ "," ++ yCoordinateString ++ "," ++ zCoordinateString ++ ")" +
        "RETURNING point_id"
      queryResult = createStatement.executeQuery(query)
      if (queryResult.next()) pointId = queryResult.getInt("point_id")
    }
    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (pointId)

  */
  }

}

