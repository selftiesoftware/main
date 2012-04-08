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

object pgsqlSave {

  Class.forName("org.postgresql.Driver")

  //(data) field declarations:
  var pointId: Int                =0
  var query: String               ="0"
  //val createStatement: Statement = PgsqlConnectionInfo.GetConnectionAndStatement()

  // methods:

  //Save shapes: Modtager sekvens af ImmutableShapes, og gemmer disse i databasen.
  //For at gemme alt i modellen: Indsæt "Model.seq" hvor metoden kaldes -
  //Model.seq (husk stort M) kører en metode på modellen, der returnerer en sekvens af alle shapes i modellen.


  //Den, der skal bruges
  def mapOfShapesIntoDrawing (shapes: Map[Int,com.siigna.app.model.shape.ImmutableShape],drawingId: Int) = {

  //Den midlertidige:
  //def saveMapOfShapesIntoDrawing (drawingId: Int) = {
  //val shapes = Model.seq

    //Opretter forbindelse til serveren
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    //Variable deklareres:
    var shapeIds: Seq[Int] = Seq()
    var pointIds: Seq[Int] = Seq()
    var propertyIntIds: Seq[Int] = Seq()
    var propertyTextIds: Seq[Int] = Seq()
    var polylineSizes: Seq[Int] = Seq()
    var shapeTypes: Seq[Int] = Seq()
    var numbersOfPropertyInts: Seq[Int] = Seq()
    var numbersOfPropertyTexts: Seq[Int] = Seq()
    var propertyIntValues: Seq[Int] = Seq()
    var propertyTextValues: Seq[String] = Seq()

    //Søgestrengene "shapeType" og "coordinates" påbegyndes:
    var queryStringShapeType: String = "INSERT INTO shape (shape_type,number_of_property_ints) VALUES "
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
    //For alle lægges antallet af propertyInts til sekvensen numbersOfPropertyInts

    shapes.foreach(tuple => tuple._2 match {
      case shape : LineShape => {
        val i:Int = shape.p1.x.toInt
        val j:Int = shape.p1.y.toInt
        val k:Int = shape.p2.x.toInt
        val l:Int = shape.p2.y.toInt
        val m = shape.attributes
        shapeTypes = shapeTypes :+ 2
        queryStringShapeType += "(2,0),"
        queryStringCoordinates += "(" + i + ","
        queryStringCoordinates += j + ",0),"
        queryStringCoordinates += "(" + k + ","
        queryStringCoordinates += l + ",0),"
        numbersOfPropertyInts = numbersOfPropertyInts :+ 0
        numbersOfPropertyTexts = numbersOfPropertyTexts :+ 0
      }
        
      case shape : PolylineShape => {
        shapeTypes = shapeTypes :+ 3
        queryStringShapeType += "(3," + shape.shapes.length + "),"
        polylineSizes = polylineSizes :+ shape.shapes.length
        numbersOfPropertyInts = numbersOfPropertyInts :+ (shape.shapes.length)
        numbersOfPropertyTexts = numbersOfPropertyTexts :+ 0
        queryStringCoordinates += "(" + shape.startPoint.x + "," + shape.startPoint.y + ",0),"

        shape.shapes.foreach(subShape => subShape match {
          case subShape : LineShape => {
            val i:Int = subShape.p2.x.toInt
            val j:Int = subShape.p2.y.toInt
            val m = subShape.attributes
            shapeTypes = shapeTypes :+ 4
            numbersOfPropertyInts = numbersOfPropertyInts :+ 0
            numbersOfPropertyTexts = numbersOfPropertyTexts :+ 0
            queryStringShapeType += "(4,0),"
            queryStringCoordinates += "(" + i + ","
            queryStringCoordinates += j + ",0),"
          }
        })
      }
      case shape : com.siigna.app.model.shape.CircleShape => {
        val m = shape.attributes
        shapeTypes = shapeTypes :+ 5
        queryStringShapeType += "(5,0),"
        queryStringCoordinates += "(" + shape.center.x + ","
        queryStringCoordinates += shape.center.y + ",0),"
        numbersOfPropertyInts = numbersOfPropertyInts :+ 1
        propertyIntValues = propertyIntValues :+ shape.radius.toInt
        numbersOfPropertyTexts = numbersOfPropertyTexts :+ 0
      }
      case shape : com.siigna.app.model.shape.ArcShape => {
        val m = shape.attributes
        shapeTypes = shapeTypes :+ 6
        queryStringShapeType += "(6,0),"
        queryStringCoordinates += "(" + shape.center.x + ","
        queryStringCoordinates += shape.center.y + ",0),"
        numbersOfPropertyInts = numbersOfPropertyInts :+ 3
        propertyIntValues = propertyIntValues :+ shape.radius.toInt
        propertyIntValues = propertyIntValues :+ shape.startAngle.toInt
        propertyIntValues = propertyIntValues :+ shape.angle.toInt
        numbersOfPropertyTexts = numbersOfPropertyTexts :+ 0
      }
      case shape: com.siigna.app.model.shape.TextShape => {
        val m = shape.attributes
        shapeTypes = shapeTypes :+ 7
        queryStringShapeType += "(7,0),"
        queryStringCoordinates += "(" + shape.position.x + ","
        queryStringCoordinates += shape.position.y + ",0),"
        numbersOfPropertyInts = numbersOfPropertyInts :+ 1
        propertyIntValues = propertyIntValues :+ shape.scale.toInt
        propertyTextValues = propertyTextValues :+ shape.text
        numbersOfPropertyTexts = numbersOfPropertyTexts :+ 1
      }
      case x => println("Ukendt - returnerede: " + x)
    })

    //Querystreng til at gemme shapetyper og punkter færdiggøres:

    //Det sidste komma i søgestrengene shapeType og Coordinate fjernes:
    queryStringShapeType = queryStringShapeType.take(queryStringShapeType.length-1)
    queryStringCoordinates = queryStringCoordinates.take(queryStringCoordinates.length-1)
    //Til shapeType søgestrengen tilføjes "returning shape_id"
    queryStringShapeType += " RETURNING shape_id"
    queryStringCoordinates += " RETURNING point_id"
    
    //Hvis søgestrengene er over den længde, de ville have, hvis intet var tilføet, udføres søgningerne.
    //Resultaterne gemmes i de to sekvenser: "shapeIds" og "pointIds"
    if (queryStringCoordinates.length > 87) {
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

    //Søgestrengene "propertyInt", "propertyText" og "shapePointRelation" påbegyndes:
    var queryStringPropertyInt: String = "INSERT INTO property_int (property_int_number,property_int_value) VALUES "
    var queryStringPropertyText: String = "INSERT INTO property_text (property_text_number,property_text_value) VALUES "
    var queryStringShapePointRelation: String = "INSERT INTO shape_point_relation (shape_id,point_id) VALUES "
    //Der laves iteratorer til at gå gennem sekvenserne "shapeId", "pointId" og "polylineSize":
    val shapeIdListIterator = shapeIds.iterator
    val pointIdListIterator = pointIds.iterator
    val polylineSizesListIterator = polylineSizes.iterator
    val shapeTypesIterator = shapeTypes.iterator
    val propertyIntValuesIterator = propertyIntValues.iterator
    val propertyTextValuesIterator = propertyTextValues.iterator

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
      case 3 => {
        //Hvis det er en polyline findes id og længde. Punkt-id'er sættes i property,
        //query-streng til property og shape-point-relation laves
        var shapeIdCurrent = shapeIdListIterator.next()
        val shapeIdPolyline = shapeIdCurrent
        val polylineSize = polylineSizesListIterator.next()
        //Hvis polylineSize > 0 er der subshapes. Der laves løkke, der gennemløbes så mange gange som der er subshapes:
        for (i <- 0 until polylineSize) {
          //Næste shape i shapeType-listen er en subshape.
          shapeTypesIterator.next() match {
            //Hvis det er en linje:
            case 4 => {
              //Næste shapeId er linjesegmentet:
              shapeIdCurrent = shapeIdListIterator.next()
              //Subshapens id indsættes i propertyInt:
              queryStringPropertyInt += "(" + (1000+i) + "," + shapeIdCurrent + "),"
              //Hvis det er første subshape i linjen, kommer første punkt med i shapePointRelation for polylinjen:
              if (i == 0) {queryStringShapePointRelation += "(" + shapeIdPolyline + "," + pointIdListIterator.next() + "),"}
              //ShapeId for linjesegmentet og punktId for "punkt to" i linjen indsættes i shapePointRelation for linjesegmentet:
              queryStringShapePointRelation += "(" + shapeIdCurrent + "," + pointIdListIterator.next() + "),"
            }
            case x => println("Ukendt polyline subshape")
          }
        }
      }
      case 4 => println("polyline segment - burde ikke komme ud her, men allerede være ekspederet...")
      case 5 => {
        queryStringShapePointRelation += "(" + shapeIdListIterator.next() + "," + pointIdListIterator.next() + "),"
        queryStringPropertyInt += "(" + 1000 + "," + propertyIntValuesIterator.next() + "),"
      }
      case 6 => {
        queryStringShapePointRelation += "(" + shapeIdListIterator.next() + "," + pointIdListIterator.next() + "),"
        queryStringPropertyInt += "(" + 1000 + "," + propertyIntValuesIterator.next() + "),"
        queryStringPropertyInt += "(" + 1001 + "," + propertyIntValuesIterator.next() + "),"
        queryStringPropertyInt += "(" + 1002 + "," + propertyIntValuesIterator.next() + "),"
      }
      case 7 => {
        queryStringShapePointRelation += "(" + shapeIdListIterator.next() + "," + pointIdListIterator.next() + "),"
        queryStringPropertyInt += "(" + 1000 + "," + propertyIntValuesIterator.next() + "),"
        queryStringPropertyText += "(" + 1 +"," + propertyTextValuesIterator.next() + "),"
      }
      case _ => println("ukendt")

    })
    // Og tada- der gemmes
    queryStringPropertyInt = queryStringPropertyInt.take(queryStringPropertyInt.length-1)
    queryStringPropertyText = queryStringPropertyText.take(queryStringPropertyText.length-1)
    queryStringShapePointRelation = queryStringShapePointRelation.take(queryStringShapePointRelation.length-1)
    queryStringPropertyInt += " RETURNING property_int_id"
    queryStringPropertyText += " RETURNING property_text_id"

    if (queryStringShapePointRelation.length()>59) createStatement.execute(queryStringShapePointRelation)

    if (queryStringPropertyInt.length > 99) {
      val queryResultPropertyIntIds: ResultSet = createStatement.executeQuery(queryStringPropertyInt)
      while (queryResultPropertyIntIds.next()) {
        propertyIntIds = propertyIntIds :+ queryResultPropertyIntIds.getInt("property_int_id")
      }
    }

    if (queryStringPropertyText.length > 110) {
      val queryResultPropertyTextIds: ResultSet = createStatement.executeQuery(queryStringPropertyText)
      while (queryResultPropertyTextIds.next()) {
        propertyTextIds = propertyTextIds :+ queryResultPropertyTextIds.getInt("property_text_id")
      }
    }

    //Alle shapeIds gennemgås, og shapePropertyInt-relation samt drawing-shape-relationer laves:
    var queryStringShapePropertyIntRelation: String = "INSERT INTO shape_property_int_relation (shape_id,property_int_id) VALUES "
    var queryStringShapePropertyTextRelation: String = "INSERT INTO shape_property_int_relation (shape_id,property_text_id) VALUES "
    var queryStringDrawingShapeRelation: String = "INSERT INTO drawing_shape_relation (drawing_id,shape_id) VALUES "
    val numbersOfPropertyIntsIterator = numbersOfPropertyInts.iterator
    val numbersOfPropertyTextsIterator = numbersOfPropertyTexts.iterator
    val propertyIntIdsIterator = propertyIntIds.iterator
    val propertyTextIdsIterator = propertyTextIds.iterator

    shapeIds.foreach (shape => {
      queryStringDrawingShapeRelation += "( " + drawingId + "," + shape + "),"
      for (i <- 0 until numbersOfPropertyIntsIterator.next()) {
        queryStringShapePropertyIntRelation += "( " + shape + "," + propertyIntIdsIterator.next() + "),"
      }
      for (i <- 0 until numbersOfPropertyTextsIterator.next()) {
        queryStringShapePropertyTextRelation += "( " + shape + "," + propertyTextIdsIterator.next() + "),"
      }
    })


    //Den færdiggøres og der gemmes:
    queryStringShapePropertyIntRelation = queryStringShapePropertyIntRelation.take(queryStringShapePropertyIntRelation.length-1)
    queryStringShapePropertyTextRelation = queryStringShapePropertyTextRelation.take(queryStringShapePropertyTextRelation.length-1)
    queryStringDrawingShapeRelation = queryStringDrawingShapeRelation.take(queryStringDrawingShapeRelation.length-1)
    if (queryStringShapePropertyIntRelation.length > 77) {
      createStatement.executeUpdate(queryStringShapePropertyIntRelation)
      }
    if (queryStringShapePropertyTextRelation.length > 77) {
      createStatement.executeUpdate(queryStringShapePropertyTextRelation)
    }
    if (queryStringDrawingShapeRelation.length > 65) {
      createStatement.executeUpdate(queryStringDrawingShapeRelation)
    }


    //Luk forbindelsen
    databaseConnection.close()

    //Data, der returneres
    (pointId)


  }

  def lastActiveDrawingIdIntoContributorData(userId:Int,drawingId:Int) {
    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    val query:String = "INSERT INTO property_int (property_int_number,property_int_value) VALUES (1,"+drawingId+") RETURNING property_int_id"
    val queryResult: ResultSet = createStatement.executeQuery(query)
    queryResult.next()
    val propertyId:Int = queryResult.getInt("property_int_id")
    val query2 = "INSERT INTO contributor_basic_property_int_relation (contributor_id,property_int_id) VALUES ("+userId+","+propertyId+")"
    createStatement.execute(query2)

    println ("Set \"last active drawing\" for current user to current drawing in the database")
    databaseConnection.close()
  }



}


