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
import com.siigna.app.model.shape.{PolylineShape, LineShape}
import com.siigna.util.geom.Vector2D
import com.siigna.util.collection.Attributes


//import java.lang.String

class pgsqlSaveShapes {

  Class.forName("org.postgresql.Driver")

  //(data) field declarations:
  var pointId: Int                =0
  var query: String               ="0"
  //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()

  // methods:

  //Save shapes: Modtager sekvens af ImmutableShapes, og gemmer disse i databasen.
  //For at gemme alt i modellen: Indsæt "Model.seq" hvor metoden kaldes -
  //Model.seq (husk stort M) kører en metode på modellen, der returnerer en sekvens af alle shapes i modellen.


  //Den, der skal bruges - men så skal der tilpasses der, hvor den kaldes...:
  // def saveShapes (shapes: Seq[com.siigna.app.model.shape.ImmutableShape]) = {

  //Den midlertidige:
  def saveShapes () = {
  val shapes = Model.seq

    //Opretter forbindelse til serveren
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    //Variable deklareres:
    var shapeIds: Seq[Int] = Seq()
    var pointIds: Seq[Int] = Seq()
    var propertyIntIds: Seq[Int] = Seq()
    var polylineSizes: Seq[Int] = Seq()
    var shapeTypes: Seq[Int] = Seq()


    //Søgestrengene "shapeType" og "coordinates" påbegyndes:
    var queryStringShapeType: String = "INSERT INTO shape (shape_type) VALUES "
    var queryStringCoordinates: String = "INSERT INTO point (x_coordinate, y_coordinate, z_coordinate) VALUES "

    //Går igennem shapesne i den modtagne liste:

    //Lineshapes: Til sekvensen "shapeType" lægges "2" til,
    //            Til søgestrengen "shapeType" lægges "2" til,
    //            Til søgestrengen "coordinates" lægges "x1,y1,0" og "x2,y2,0" til
    //Polylines:  Til sekvensen "shapeType" lægges "3" til,
    //            Til søgestrengen "shapeType" lægges "3" til,
    //            Til sekvqnsen "polylineSizes" lægges længden af polylinjen til,
    //            Shapesene, der udgør polylinjen gennemgås:
    //            For linjer:       Til sekvensen "shapeType" lægges "4" til,
    //                              Til søgestrengen "shapeType" lægges "4" til,
    //                              Til søgestrengen "coordinates" lægges "x1,y1,0" og "x2,y2,0" til
    //Øvrige:     println "ukendt".

    shapes.foreach(tuple => tuple._2 match {
      case shape : LineShape => {
        val i:Int = shape.p1.x.toInt
        val j:Int = shape.p1.y.toInt
        val k:Int = shape.p2.x.toInt
        val l:Int = shape.p2.y.toInt
        val m = shape.attributes
        shapeTypes = shapeTypes :+ 2
        queryStringShapeType += "(2),"
        queryStringCoordinates += "(" + i + ","
        queryStringCoordinates += j + ",0),"
        queryStringCoordinates += "(" + k + ","
        queryStringCoordinates += l + ",0),"
      }
      case shape : PolylineShape => {
        println ("Polyline")
        println (shape)
        shapeTypes = shapeTypes :+ 3
        queryStringShapeType += "(3),"
        polylineSizes = polylineSizes :+ shape.shapes.length
        //Til at indsætte startpunkt for tomme polylines - findes, men metoden til at hente det mangler.
        /*
        if (shape.shapes.length == 0) {
          queryStringCoordinates += "(" + shape.startPoint.x + "," + shape.startPoint.y + ",0),"
        } */
        //Indtil dette laves indsættes et 0,0-punkt som startpunkt til tomme polylines. 
            if (shape.shapes.length == 0) queryStringCoordinates += "(0,0,0),"
        shape.shapes.foreach(subShape => subShape match {
          case subShape : LineShape => {
            val i:Int = subShape.p1.x.toInt
            val j:Int = subShape.p1.y.toInt
            val k:Int = subShape.p2.x.toInt
            val l:Int = subShape.p2.y.toInt
            val m = subShape.attributes
            shapeTypes = shapeTypes :+ 4
            queryStringShapeType += "(4),"
            queryStringCoordinates += "(" + i + ","
            queryStringCoordinates += j + ",0),"
            queryStringCoordinates += "(" + k + ","
            queryStringCoordinates += l + ",0),"
          }
        })
      }
      case x => println("Ukendt - returnerede: " + x)
    })

    //Querystreng til at gemme shapetyper og punkter færdiggøres:

    //Det sidste komma i søgestrengene shapeType og Coordinate fjernes:
    queryStringShapeType = queryStringShapeType.take(queryStringShapeType.length-1)
    queryStringCoordinates = queryStringCoordinates.take(queryStringCoordinates.length-1)
    //Til shapeType søgestrengen tilføjes "returning shape_id"
    //Til coordinates søgestrengen tilføjes "returning point_id"
    queryStringShapeType += " RETURNING shape_id"
    queryStringCoordinates += " RETURNING point_id"
    
    //Hvis søgestrengene er over den længde, de ville have, hvis intet var tilføet, udføres søgningerne.
    //Resultaterne gemmes i de to sekvenser: "shapeIds" og "pointIds"
    if (queryStringCoordinates.length > 100) {
      val queryResultShapeIds: ResultSet = createStatement.executeQuery(queryStringShapeType)
      while (queryResultShapeIds.next()) {
        shapeIds = shapeIds :+ queryResultShapeIds.getInt("shape_id")
      }

      val queryResultPointIds: ResultSet = createStatement.executeQuery(queryStringCoordinates)
      while (queryResultPointIds.next()) {
        pointIds = pointIds :+ queryResultPointIds.getInt("point_id")
      }
    }

    //Vi har shape- og point-ids. Nu skal property'er og shape-point-relationer laves.

    //Søgestrengene "propertyInt" og "shapePointRelation" påbegyndes:
    var queryStringPropertyInt: String = "INSERT INTO property_int (property_int_number,property_int_value) VALUES "
    var queryStringShapePointRelation: String = "INSERT INTO shape_point_relation (shape_id,point_id) VALUES "
    //Der laves iteratorer til at gå gennem sekvenserne "shapeId", "pointId" og "polylineSize":
    val shapeIdListIterator = shapeIds.iterator
    val pointIdListIterator = pointIds.iterator
    val polylineSizesListIterator = polylineSizes.iterator
    val shapeTypesIterator = shapeTypes.iterator

    //Listen "shapeTypes" gennemgås, og parallelt hermed gennemgås "shapeId" og "pointId":
    shapeTypesIterator.foreach(shape => shape match {
      //ShapeType 1: Println "point"
      case 1 => println("point")
      //ShapeType 2: Til søgestrengen "shapePointRelation" lægges shape-id og de tilhørerende punkt-id'er:
      case 2 => {
        val shapeIdCurrent = shapeIdListIterator.next()
        queryStringShapePointRelation += "(" + shapeIdCurrent + "," + pointIdListIterator.next() + ")," +
                                         "(" + shapeIdCurrent + "," + pointIdListIterator.next() + "),"
      }
      //ShapeType 3: Til søgestrengen "propertyInt" lægges (1000,"polylineSize"),
      //             Til søgestrengen "propertyInt" lægges (1001,"pointId"),(1002,"pointId") osv. for alle punkter.
      //             Til søgestrengen "shapePointRelation" lægges
      case 3 => {
        //Hvis det er en polyline findes id og længde. Punkt-id'er sættes i property,
        //query-streng til property og shape-point-relation laves
        var shapeIdCurrent = shapeIdListIterator.next()
        val shapeIdPolyline = shapeIdCurrent
        val polylineSize = polylineSizesListIterator.next()
        queryStringPropertyInt += "(1000," + polylineSize + "),"
        //Hvis polylineSize > 0 er der subshapes. Der laves løkke, der gennemløbes så mange gange som der er subshapes:
        for (i <- 0 until polylineSize) {
          //Næste shape i shapeType-listen er en subshape.
          shapeTypesIterator.next() match {
            //Hvis det er en linje:
            case 4 => {
              //Næste shapeId er linjesegmentet:
              shapeIdCurrent = shapeIdListIterator.next()

              //Subshapens id indsættes i propertyInt:
              queryStringPropertyInt += "(" + (1001+i) + "," + shapeIdCurrent + "),"
              var pointIdCurrent = pointIdListIterator.next()
              //Hvis det er første subshape i linjen, kommer første punkt med i shapePointRelation for polylinjen:
              if (i == 0) {queryStringShapePointRelation += "(" + shapeIdPolyline + "," + pointIdCurrent + "),"}
              //ShapeId for linjesegmentet og punktId for de to punkter indsættes i shapePointRelation for dette:
              queryStringShapePointRelation += "(" + shapeIdCurrent + "," + pointIdCurrent + "),"
              pointIdCurrent = pointIdListIterator.next()
              queryStringShapePointRelation += "(" + shapeIdCurrent + "," + pointIdCurrent + "),"
              //ShapeId for punkt nummer to sættes i shapePointRelation for polylinjen:
              queryStringShapePointRelation += "(" + shapeIdPolyline + "," + pointIdCurrent + "),"
            }
            case x => println("Ukendt polyline subshape")
          }
        }
      }
      case 4 => println("polyline segment - burde ikke komme ud her, men allerede være ekspederet...")
      case _ => println("ukendt")

    })
    // Og tada- der gemmes
    queryStringPropertyInt = queryStringPropertyInt.take(queryStringPropertyInt.length-1)
    queryStringShapePointRelation = queryStringShapePointRelation.take(queryStringShapePointRelation.length-1)
    queryStringPropertyInt += " RETURNING property_int_id"
    queryStringShapePointRelation += " RETURNING shape_id"

    if (queryStringPropertyInt.length > 110) {
      val queryResultShapeIds: ResultSet = createStatement.executeQuery(queryStringPropertyInt)
      while (queryResultShapeIds.next()) {
        propertyIntIds = propertyIntIds :+ queryResultShapeIds.getInt("property_int_id")
      }

      val queryResultPointIds: ResultSet = createStatement.executeQuery(queryStringShapePointRelation)
      while (queryResultPointIds.next()) {
        pointIds = pointIds :+ queryResultPointIds.getInt("shape_id")
      }
    }



    var queryStringShapePropertyRelation: String = "INSERT INTO shape_property_relation (shape_id,property_id) VALUES "


    //queryStringShapePropertyRelation += "(" + propertyIntIds + "," +



    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (pointId)


  }

}


