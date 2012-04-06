package com.siigna.app.model.drawing

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 06-04-12
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */

object activeDrawing {

  var drawingId: Option[Int] = None
  var drawingName: Option[String] = None
  var offsetX: Option[Int] = None
  var offsetY: Option[Int] = None
  var offsetZ: Option[Int] = None

  def setActiveDrawingId(id:Int) {
    drawingId = Some(id)
  }
  
  def setActiveDrawingName(id:Int,name:String) {
    com.siigna.app.controller.pgsql_handler.pgsqlUpdate.renameDrawing(id,name)
    drawingName = Some(name)  }

  /*def getAllDrawingInfoFromDatabase(id:Int) {
    val returned = new com.siigna.app.controller.pgsql_handler.pgsqlGet
    (drawingName,offsetX,offsetY,offsetZ) = returned.drawingDataFromId(id)
  }*/
}
