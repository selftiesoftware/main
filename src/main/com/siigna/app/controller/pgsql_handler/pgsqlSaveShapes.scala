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

  //Save point: Modtager x, y og z-koordinater (Int), og returnerer pointId (Int)
  def saveShapes (/*coordinates: Seq[Int]*/) = {

    val databaseConnection: Connection = DriverManager.getConnection("jdbc:postgresql://siigna.com/siigna_world","siigna_world_user","s11gn@TUR")
    val createStatement: Statement = databaseConnection.createStatement()

    var shapeIds: Seq[Int] = Seq()
    var pointIds: Seq[Int] = Seq()
    var propertyIds: Seq[Int] = Seq()
    var polylineSizes: Seq[Int] = Seq()
    var shapeTypes: Seq[Int] = Seq()



    //Går gennem alle punkter i modellen

    println("jada")
    val modelContains = Model.seq
    var queryStringShapeType: String = "INSERT INTO shape (shape_type) VALUES "
    var queryStringCoordinates: String = "INSERT INTO point (x_coordinate, y_coordinate, z_coordinate) VALUES "

      modelContains.foreach(shape => shape match {
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
        //WrappedArray(LineShape(Vector2D(27.0,31.0),Vector2D(27.0,24.0),Attributes()))
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
    })
        


    //Querystreng til at gemme shapetyper og punkter færdiggøres:

    queryStringShapeType = queryStringShapeType.take(queryStringShapeType.length-1)
    queryStringCoordinates = queryStringCoordinates.take(queryStringCoordinates.length-1)
    queryStringShapeType += " RETURNING shape_id"
    queryStringCoordinates += " RETURNING point_id"

    println(queryStringShapeType)
    println(queryStringCoordinates)


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



    //Vi har shape- og point-ids. Nu skal property og relationer laves.
    var queryStringPropertyInt: String = "INSERT INTO property_int (property_int_number,property_int_value) VALUES "
    var queryStringShapePointRelation: String = "INSERT INTO shape_point_relation (shape_id,point_id) VALUES "
    val shapeIdListIterator = shapeIds.iterator
    val pointIdListIterator = pointIds.iterator
    val polylineSizesListIterator = polylineSizes.iterator

    shapeTypes.foreach(shape => shape match {
      case 1 => println("point")
      case 2 => {
        //Hvis det er en linje tages det næste shape-id og de næste 2 punkt-id'er og føjes til query-strengen
        val shapeId = shapeIdListIterator.next()
        queryStringShapePointRelation += "(" + shapeId + "," + pointIdListIterator.next() + ")," +
                                         "(" + shapeId + "," + pointIdListIterator.next() + "),"
      }
      case 3 => {
        //Hvis det er en polyline findes id og længde. Punkt-id'er sættes i property,
        //query-streng til property og shape-point-relation laves
        var pointIds = pointIdListIterator.next()
        val shapeIdPolyline = shapeIdListIterator.next()
        val polylineSize = polylineSizesListIterator.next()

        queryStringPropertyInt += "(1000," + polylineSize + "),"
        //Første punkt kommer med i property - om det er startpunktet i en tom polylinje eller startpunktet i én med linjer i
        queryStringPropertyInt += "(1001," + pointIds + "),"
        queryStringShapePointRelation += "(" + shapeIdPolyline + "," + pointIds + "),"
        var shapeIdPolylineSegment = 0
        if (polylineSize > 0) {
          shapeIdPolylineSegment = shapeIdListIterator.next()
          queryStringShapePointRelation += "(" + shapeIdPolylineSegment + "," + pointIds + "),"
        }
        var i = 0
        while (i<(polylineSize)) {
          //Hvis der er mere end et linjestykke er startpunktet allerede med, og det overspringes
          // i property, men medtages i shape_point_relation.
          if (i>0) {
            val pointIds2 = pointIdListIterator.next()
            if (polylineSize > 0) {
              shapeIdPolylineSegment = shapeIdListIterator.next()
              queryStringShapePointRelation += "(" + shapeIdPolylineSegment + "," + pointIds2 + "),"
            }
          }
          pointIds = pointIdListIterator.next()
          queryStringPropertyInt += "(" + (1002+i) + "," + pointIds + "),"
          queryStringShapePointRelation += "(" + shapeIdPolyline + "," + pointIds + "),"
          if (polylineSize > 0) {
            queryStringShapePointRelation += "(" + shapeIdPolylineSegment + "," + pointIds + "),"
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
        shapeIds = shapeIds :+ queryResultShapeIds.getInt("property_int_id")
      }

      val queryResultPointIds: ResultSet = createStatement.executeQuery(queryStringShapePointRelation)
      while (queryResultPointIds.next()) {
        pointIds = pointIds :+ queryResultPointIds.getInt("shape_id")
      }
    }



    var queryStringShapePropertyRelation: String = "INSERT INTO shape_property_relationt (shape_id,property_id) VALUES "


    //queryStringShapePropertyRelation += "(" + shapeId + "," +




    /*
    var queryStringShapePointRelation: String =
        "INSERT INTO shape_point_relationt (shape_id,point_id) VALUES "
    var i:Int = 0
    while (shapeIds.isDefinedAt(i)) {
      queryStringShapePointRelation += "(" + shapeIds(i) + "," + pointIds(i*2) + "),"
      queryStringShapePointRelation += "(" + shapeIds(i) + "," + pointIds((i*2)+1) + "),"
      i=i+1
    }

    queryStringShapePointRelation = queryStringShapePointRelation.take(queryStringShapePointRelation.length-1)

    //Hvis denne linje ikke er med kommer der en fejl. Bør nok fjernes ved optimering. Nu er den der så det virker
    queryStringShapePointRelation += " RETURNING shape_id"

    println(queryStringShapePointRelation)

    createStatement.executeQuery(queryStringShapePointRelation)
      createStatement.close()

    }
    */

    /*INSERT INTO shapet (shape_type) VALUES (2),(2),...,(2) RETURNING shape_id

    INSERT INTO pointt (x_coordinate, y_coordinate, z_coordinate) VALUES (x,y,z), (x,y,z) RETURNING point_id

    INSERT INTO shape_point_relationt (shape_id, point_id) VALUES (x,x),(x,x)

    query =       "INSERT INTO shape " +
      "(shape_type) " +
      "VALUES" +
      "(2)" +
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

*/
    /*var i=0
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
     */

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


