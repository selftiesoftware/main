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

  def setId(id:Int) {
    drawingId = Some(id)
  }
  
  def setName(id:Int,name:String) {
    com.siigna.app.controller.pgsql_handler.pgsqlUpdate.renameDrawing(id,name)
  }

}
