package com.siigna.app.controller.pgsqlhandler

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 10-01-12
 * Time: 17:20
 * To change this template use File | Settings | File Templates.
 */

import java.sql._
import java.lang.String

class pgsql_db_connect (){
    Class.forName("org.postgresql.Driver")
  // Kræver at postgresql-9.1-901.jdbc4.jar eller lign. driverfil er tilføjet til CLASSPATH (menu: file -> project structure).
  // Filen skal lægges ind i et passende dir (fx. med de andre filer i CLASSPATH", og så tilføjes til projektet i intellij.

  val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
  val createStatement: Statement = databaseConnection.createStatement()
  }
