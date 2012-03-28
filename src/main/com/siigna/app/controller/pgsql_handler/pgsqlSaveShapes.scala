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
    shapes.foreach(shape => shape match {
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
      case _ => println("Ukendt")
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

    //Listen "shapeTypes" gennemgås, og parallelt hermed gennemgås "shapeId" og "pointId":
    shapeTypes.foreach(shape => shape match {
      //ShapeType 1: Println "point"
      case 1 => println("point")
      //ShapeType 2: Til søgestrengen "shapePointRelation" lægges shape-id og de tilhørerende punkt-id'er:
      case 2 => {
        val shapeId = shapeIdListIterator.next()
        queryStringShapePointRelation += "(" + shapeId + "," + pointIdListIterator.next() + ")," +
                                         "(" + shapeId + "," + pointIdListIterator.next() + "),"
      }
      //ShapeType 3: Til søgestrengen "propertyInt" lægges (1000,"polylineSize"),
      //             Til søgestrengen "propertyInt" lægges (1001,"pointId"),(1002,"pointId") osv. for alle punkter.
      //             Til søgestrengen "shapePointRelation" lægges
      case 3 => {
        //Hvis det er en polyline findes id og længde. Punkt-id'er sættes i property,
        //query-streng til property og shape-point-relation laves
        var pointIdsFromList = pointIdListIterator.next()
        val shapeIdPolyline = shapeIdListIterator.next()
        val polylineSize = polylineSizesListIterator.next()
        queryStringPropertyInt += "(1000," + polylineSize + "),"
        //ShapeId og PointId for første punkt kommer med i shapePointRelation
        queryStringShapePointRelation += "(" + shapeIdPolyline + "," + pointIdsFromList + "),"
        //Første punkt kommer med i property - om det er startpunktet i en tom polylinje eller startpunktet i én med linjer i
        queryStringPropertyInt += "(1001," + pointIdsFromList + "),"
        //Hvis polylineSize > 0 er der også en shapetype 4, og det første punkt af denne kommer i shapePointRelation
        var shapeIdPolylineSegment = 0
        if (polylineSize > 0) {
          shapeIdPolylineSegment = shapeIdListIterator.next()
          queryStringShapePointRelation += "(" + shapeIdPolylineSegment + "," + pointIdsFromList + "),"
        }
        var i = 0
        while (i<(polylineSize)) {
          //For første punkt (i=0): Hvis der er mindst et linjestykke er startpunktet allerede med,
          //og det overspringes i property, men medtages i shape_point_relation.
          if (i>0) {
            val pointIdsFromList2 = pointIdListIterator.next()
            if (polylineSize > 0) {
              shapeIdPolylineSegment = shapeIdListIterator.next()
              queryStringShapePointRelation += "(" + shapeIdPolylineSegment + "," + pointIdsFromList2 + "),"
            }
          }
          pointIdsFromList = pointIdListIterator.next()
          queryStringPropertyInt += "(" + (1002+i) + "," + pointIdsFromList + "),"
          queryStringShapePointRelation += "(" + shapeIdPolyline + "," + pointIdsFromList + "),"
          if (polylineSize > 0) {
            queryStringShapePointRelation += "(" + shapeIdPolylineSegment + "," + pointIdsFromList + "),"
          }
          i=i+1
        }
      }
      case 4 => println("polyline segment")
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


