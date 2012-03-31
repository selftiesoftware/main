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


import java.sql._
import com.siigna.app.model.shape._
import com.siigna.util.geom.Vector2D

class pgsqlGetShapes {

  //Modtager      x- og y-koordinat (Int),
  //              samt afstand fra centerpunkt i x- og y-retning (Int), der skal søges
  //Returnerer    en sequence af instantierede shapes:   (Shape1, Shape2,..., ShapeN)

  def getShapes (xCoordinate: Int, yCoordinate: Int, zCoordinate: Int, xDistance: Int, yDistance: Int, zDistance: Int) = {

    // Field variable definition
    var query: String               ="0"
    var i: Int = 0
    var j: Int = 0
    var shapeType : Option[Int] = None
    var shapeId : Option[Int] = None

    var pointIdSequence: Seq[Int] = Seq()
    var pointCoordinatesLocationSequence: Seq[Int] = Seq()

    var resultSequenceShapeIdPointIdShapeId: Seq[Int] = Seq()
    var resultSequenceShapeIdPointIdPointId: Seq[Int] = Seq()

    var resultSequencePointIdXYZCoordinatesPointId: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesX: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesY: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesZ: Seq[Int] = Seq()

    var resultSequenceShapes: Seq[ImmutableShape] = Seq()

    //Virker ikke: Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()

    //Opretter forbindelse til databasen og laver createStatement variabel.
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Finder shape id og tilhørende point id for shapes, der har et eller flere punkter i det angivne område (søgning A):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_point_relation.shape_id, shape_point_relation.point_id  " +
      "FROM shape_point_relation " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape_point_relation.shape_id = alias2.shape_id"
    val queryResultShapeIdPointId: ResultSet = createStatement.executeQuery(query)
    while (queryResultShapeIdPointId.next()) {
      resultSequenceShapeIdPointIdShapeId = resultSequenceShapeIdPointIdShapeId :+ queryResultShapeIdPointId.getInt("shape_id")
      resultSequenceShapeIdPointIdPointId = resultSequenceShapeIdPointIdPointId :+ queryResultShapeIdPointId.getInt("point_id")
    }

    //Finder punkt id og tilhørende koordinater for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.

    query =   "SELECT DISTINCT point.point_id, x_coordinate, y_coordinate, z_coordinate " +
              "FROM point " +
              "JOIN " +
              "(shape_point_relation " +
                  "JOIN " +
                  "(shape_point_relation " +
                      "JOIN " +
                      "(SELECT point_id " +
                      "FROM point " +
                      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
                          "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
                          "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
                      "AS alias " +
                      "ON  shape_point_relation.point_id = alias.point_id) " +
                  "AS alias2 " +
                  "ON shape_point_relation.shape_id = alias2.shape_id) " +
              "ON point.point_id = shape_point_relation.point_id"

    val queryResultPointIdCoordinates: ResultSet = createStatement.executeQuery(query)
    while (queryResultPointIdCoordinates.next()) {
      resultSequencePointIdXYZCoordinatesPointId = resultSequencePointIdXYZCoordinatesPointId :+ queryResultPointIdCoordinates.getInt("point_id")
      resultSequencePointIdXYZCoordinatesX = resultSequencePointIdXYZCoordinatesX :+ queryResultPointIdCoordinates.getInt("x_coordinate")
      resultSequencePointIdXYZCoordinatesY = resultSequencePointIdXYZCoordinatesY :+ queryResultPointIdCoordinates.getInt("y_coordinate")
      resultSequencePointIdXYZCoordinatesZ = resultSequencePointIdXYZCoordinatesZ :+ queryResultPointIdCoordinates.getInt("z_coordinate")
    }

    //Finder shape type og tilhørende shape id for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_type, shape.shape_id  " +
              "FROM shape " +
              "JOIN " +
              "(shape_point_relation " +
                  "JOIN " +
                  "(SELECT point_id " +
                  "FROM point " +
                  "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
                      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
                      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
                  "AS alias " +
                  "ON  shape_point_relation.point_id = alias.point_id) " +
              "AS alias2 " +
              "ON shape.shape_id = alias2.shape_id"
    val queryResultShapeTypeShapeId: ResultSet = createStatement.executeQuery(query)

    //Finder de shapes, der er returneret
    while (queryResultShapeTypeShapeId.next()) {
      //Henter shape-id
      shapeId = Some(queryResultShapeTypeShapeId.getInt("shape_id"))
      //Henter shape-type
      shapeType = Some(queryResultShapeTypeShapeId.getInt("shape_type"))
      //laver liste over de steder i resultSequencePointIdXYZCoordinatesX/Y/Z hvor koordinaterne til punkterne i shapen findes
        //Først skal punkt-id findes ud fra shape id:
          i=0
          while (resultSequenceShapeIdPointIdShapeId.isDefinedAt(i)) {
            if (resultSequenceShapeIdPointIdShapeId(i) == shapeId.get  ) {
              pointIdSequence = pointIdSequence :+ resultSequenceShapeIdPointIdPointId(i)
            }
          i=i+1
          }
        //Så kan placeringen af punkterne i resultSequencePointIdXYZCoordinates findes ud fra point id:
          i=0
          while (pointIdSequence.isDefinedAt(i)) {
            j=0
            while (resultSequencePointIdXYZCoordinatesPointId.isDefinedAt(j)) {
              if (pointIdSequence(i) == resultSequencePointIdXYZCoordinatesPointId(j)) {
                pointCoordinatesLocationSequence = pointCoordinatesLocationSequence :+ j
              }
              j=j+1
            }
          i=i+1
          }

      //Punkt-shape:
      if (shapeType.get == 1)
        {}
      //Line-shape:
      if (shapeType.get == 2)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Polyline-shape:
      if (shapeType.get == 3)
        {}
      //Cirkel-shape:
      if (shapeType.get == 4)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Arc-shape:
      if (shapeType.get == 5)
      {}

    //Nulstiller sequences til brug for næste gennemløb af løkken, hvor næste shape findes frem.
      pointIdSequence = Seq()
      pointCoordinatesLocationSequence = Seq()
    }

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres
    (resultSequenceShapes)
  }

  def getShapesInArea (xCoordinate: Int, yCoordinate: Int, zCoordinate: Int, xDistance: Int, yDistance: Int, zDistance: Int) = {

    // Field variable definition
    var query: String               ="0"
    var i: Int = 0
    var j: Int = 0
    var shapeType : Option[Int] = None
    var shapeId : Option[Int] = None

    var pointIdSequence: Seq[Int] = Seq()
    var pointCoordinatesLocationSequence: Seq[Int] = Seq()

    var resultSequenceShapeIdPointIdShapeId: Seq[Int] = Seq()
    var resultSequenceShapeIdPointIdPointId: Seq[Int] = Seq()

    var resultSequencePointIdXYZCoordinatesPointId: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesX: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesY: Seq[Int] = Seq()
    var resultSequencePointIdXYZCoordinatesZ: Seq[Int] = Seq()

    var resultSequenceShapes: Seq[ImmutableShape] = Seq()

    //Virker ikke: Henter objektet med forbindelsesoplysninger
    //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()

    //Opretter forbindelse til databasen og laver createStatement variabel.
    var databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    var createStatement: Statement = databaseConnection.createStatement()

    //Finder shape id og tilhørende point id for shapes, der har et eller flere punkter i det angivne område (søgning A):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_point_relation.shape_id, shape_point_relation.point_id  " +
      "FROM shape_point_relation " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape_point_relation.shape_id = alias2.shape_id"
    val queryResultShapeIdPointId: ResultSet = createStatement.executeQuery(query)
    while (queryResultShapeIdPointId.next()) {
      resultSequenceShapeIdPointIdShapeId = resultSequenceShapeIdPointIdShapeId :+ queryResultShapeIdPointId.getInt("shape_id")
      resultSequenceShapeIdPointIdPointId = resultSequenceShapeIdPointIdPointId :+ queryResultShapeIdPointId.getInt("point_id")
    }

    //Finder punkt id og tilhørende koordinater for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.

    query =   "SELECT DISTINCT point.point_id, x_coordinate, y_coordinate, z_coordinate " +
      "FROM point " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape_point_relation.shape_id = alias2.shape_id) " +
      "ON point.point_id = shape_point_relation.point_id"

    val queryResultPointIdCoordinates: ResultSet = createStatement.executeQuery(query)
    while (queryResultPointIdCoordinates.next()) {
      resultSequencePointIdXYZCoordinatesPointId = resultSequencePointIdXYZCoordinatesPointId :+ queryResultPointIdCoordinates.getInt("point_id")
      resultSequencePointIdXYZCoordinatesX = resultSequencePointIdXYZCoordinatesX :+ queryResultPointIdCoordinates.getInt("x_coordinate")
      resultSequencePointIdXYZCoordinatesY = resultSequencePointIdXYZCoordinatesY :+ queryResultPointIdCoordinates.getInt("y_coordinate")
      resultSequencePointIdXYZCoordinatesZ = resultSequencePointIdXYZCoordinatesZ :+ queryResultPointIdCoordinates.getInt("z_coordinate")
    }

    //Finder shape type og tilhørende shape id for shapes, der har et eller flere punkter i det angivne område (søgning B):
    //Hage: fx. cirkler, der ikke har punkter men kun streg i området kommer ikke med.
    query =   "SELECT DISTINCT shape_type, shape.shape_id  " +
      "FROM shape " +
      "JOIN " +
      "(shape_point_relation " +
      "JOIN " +
      "(SELECT point_id " +
      "FROM point " +
      "WHERE ((x_coordinate BETWEEN " + (xCoordinate - xDistance) + " AND " + (xCoordinate + xDistance) + ") " +
      "AND (y_coordinate BETWEEN " + (yCoordinate - yDistance) + " AND " + (yCoordinate + yDistance) + ") " +
      "AND (z_coordinate BETWEEN " + (zCoordinate - zDistance) + " AND " + (zCoordinate + zDistance) + "))) " +
      "AS alias " +
      "ON  shape_point_relation.point_id = alias.point_id) " +
      "AS alias2 " +
      "ON shape.shape_id = alias2.shape_id"
    val queryResultShapeTypeShapeId: ResultSet = createStatement.executeQuery(query)

    //Finder de shapes, der er returneret
    while (queryResultShapeTypeShapeId.next()) {
      //Henter shape-id
      shapeId = Some(queryResultShapeTypeShapeId.getInt("shape_id"))
      //Henter shape-type
      shapeType = Some(queryResultShapeTypeShapeId.getInt("shape_type"))
      //laver liste over de steder i resultSequencePointIdXYZCoordinatesX/Y/Z hvor koordinaterne til punkterne i shapen findes
      //Først skal punkt-id findes ud fra shape id:
      i=0
      while (resultSequenceShapeIdPointIdShapeId.isDefinedAt(i)) {
        if (resultSequenceShapeIdPointIdShapeId(i) == shapeId.get  ) {
          pointIdSequence = pointIdSequence :+ resultSequenceShapeIdPointIdPointId(i)
        }
        i=i+1
      }
      //Så kan placeringen af punkterne i resultSequencePointIdXYZCoordinates findes ud fra point id:
      i=0
      while (pointIdSequence.isDefinedAt(i)) {
        j=0
        while (resultSequencePointIdXYZCoordinatesPointId.isDefinedAt(j)) {
          if (pointIdSequence(i) == resultSequencePointIdXYZCoordinatesPointId(j)) {
            pointCoordinatesLocationSequence = pointCoordinatesLocationSequence :+ j
          }
          j=j+1
        }
        i=i+1
      }

      //Punkt-shape:
      if (shapeType.get == 1)
      {}
      //Line-shape:
      if (shapeType.get == 2)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Polyline-shape:
      if (shapeType.get == 3)
      {}
      //Cirkel-shape:
      if (shapeType.get == 4)
      {
        resultSequenceShapes = resultSequenceShapes :+ LineShape(Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(0)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(0))), Vector2D(resultSequencePointIdXYZCoordinatesX(pointCoordinatesLocationSequence(1)),resultSequencePointIdXYZCoordinatesY(pointCoordinatesLocationSequence(1))))
      }
      //Arc-shape:
      if (shapeType.get == 5)
      {}

      //Nulstiller sequences til brug for næste gennemløb af løkken, hvor næste shape findes frem.
      pointIdSequence = Seq()
      pointCoordinatesLocationSequence = Seq()
    }

    //Luk forbindelsen
    createStatement.close()

    //Data, der returneres

    (resultSequenceShapes)
  }
}
