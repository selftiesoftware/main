package com.siigna.app.model.drawing
import com.siigna._

object activeDrawing {

  var drawingName: Option[String] = None
  var offsetX: Option[Int] = None
  var offsetY: Option[Int] = None
  var offsetZ: Option[Int] = None
  var contributorName: Option[String] = None
  var contributorId: Option[Int] = None

  def setContributorName(name:String) {
    contributorName = Some(name)
  }

  def setContributorId(id:Int) {
    contributorId = Some(id)
  }
  
  def setActiveDrawingName(name:String) {
    drawingName = Some(name)
  }

  /*def updateActiveDrawingNameInDatabase(id:Int,name:String) {
    com.siigna.app.controller.pgsql_handler.pgsqlUpdate.renameDrawing(id,name)
    drawingName = Some(name)
  }

  def getInfoOnUserAndDrawingAtStartup() {
  //set contributor
  setContributorId(com.siigna.app.controller.AppletParameters.getParametersInt(Some("contributorId")).get)
  setContributorName(pgsqlGet.contributorNameFromId(contributorId.get))
  //read drawing ID from database
  var lastActiveDrawing: Option[Int] = pgsqlGet.contributorsLastActiveDrawing(contributorId.get)
    if (lastActiveDrawing.isDefined) {
      //Henter tegningen, hvis der er en gammel:
      println ("Loading last active drawing...")
      val shapes: Map[Int,ImmutableShape] = pgsqlGet.allShapesInDrawingFromDrawingIdWithDatabaseId(lastActiveDrawing.get)
      if (shapes.size > 0 ) {Create(shapes)} else {println("Drawing is empty.")}
    }
    if (lastActiveDrawing.isEmpty) {
    println ("Brugeren har ikke nogle tidligere aktive tegninger i Siigna, eller den seneste aktive tegning er slettet. Der startes en ny tegning...")
    lastActiveDrawing = Some(pgsqlIdPool.getNewDrawingId())
    //Gemmer denne nye tegning som starttegning
    com.siigna.app.controller.pgsql_handler.pgsqlSave.lastActiveDrawingIdIntoContributorData(contributorId.get,lastActiveDrawing.get)
  }
  setActiveDrawingId(lastActiveDrawing.get)
  //read drawing title
  setActiveDrawingName(pgsqlGet.drawingNameFromId(drawingId.get).get)
  }

  /*def getDrawingInfoFromDatabase(id:Int) = {
    val returned = new com.siigna.app.controller.pgsql_handler.pgsqlGet
    (drawingName) = returned.drawingNameFromId(id)
    (drawingName)
  }*/ */
}
