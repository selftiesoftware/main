package com.siigna.app.controller.pgsql_handler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 13-01-12
 * Time: 18:55
 * To change this template use File | Settings | File Templates.
 */

import java.sql._
import java.lang.String

  class pgsql_db_query () {

    Class.forName("org.postgresql.Driver")
    // Kræver at postgresql-9.1-901.jdbc4.jar eller lign. driverfil er tilføjet til CLASSPATH (menu: file -> project structure).
    // Filen skal lægges ind i et passende dir (fx. med de andre filer i CLASSPATH", og så tilføjes til projektet i intellij.

    //(data) field declarations:
    var pointId: Int                =0
    var query: String               ="0"
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    // methods:

    //Get point:
    def postgresSavePoint (xCoordinateInt: Int, yCoordinateInt: Int, zCoordinateInt: Int): (Int) = {
      //Lav punkterne om fra integers til strenge
      val xCoordinateString: String = xCoordinateInt.toString
      val yCoordinateString: String = yCoordinateInt.toString
      val zCoordinateString: String = zCoordinateInt.toString
      //Check om punktet findes i forvejen - hvis det gør returneres point_id
      query = "SELECT point_id " ++
              "FROM point " ++
              "WHERE x_coordinate = " ++ xCoordinateString ++ " AND y_coordinate = " ++ yCoordinateString ++ " AND z_coordinate = " ++ zCoordinateString
      var queryResult: ResultSet = createStatement.executeQuery(query)
      queryResult.next()
      pointId = queryResult.getInt("point_id")
      //Hvis det ikke findes, indsættes det i databasen, og pointId fra foregående søgning returneres
      if (!queryResult.next) {
        query = "INSERT INTO point " ++
                "(x_coordinate, y_coordinate, z_coordinate) " ++
                "VALUES " ++
                "(" ++ xCoordinateString ++ "," ++ yCoordinateString ++ "," ++ zCoordinateString ++ ")"
        createStatement.execute(query)
        //Og point_id findes og returneres
        query = "SELECT point_id " ++
                "FROM point " ++
                "WHERE x_coordinate = " ++ xCoordinateString ++ " AND y_coordinate = " ++ yCoordinateString ++ " AND z_coordinate = " ++ zCoordinateString
        queryResult = createStatement.executeQuery(query)
        queryResult.next()
        pointId = queryResult.getInt("point_id")
        println ("her")
      }
      //Luk forbindelsen
      databaseConnection.close()

      //Data, der returneres
      (pointId)
    }

   /*
    //Testfunktion til illustration:
    def postgresDirectQuery (querySelect: String, queryFrom: String, queryWhere: String) {
      query = "select " ++ querySelect ++ " from " ++ queryFrom ++ " where " ++ queryWhere
      var queryResult: ResultSet = createStatement.executeQuery(query)

      while (queryResult.next()) {
        println(queryResult.getString("table_name"))
      }
      databaseConnection.close()
    }
    */

  }


