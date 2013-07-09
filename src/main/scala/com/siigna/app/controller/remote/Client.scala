package com.siigna.app.controller.remote

import com.siigna.app.model.Model
import com.siigna.util.Log
import com.siigna.app.model.action.RemoteAction
import com.siigna.app.model.server.User
import com.siigna.util.io.{Marshal, Unmarshal}

/**
 * Http Client for siigna communication
 * @param address: Fully qualified domain name for the base address of siigna server to use (including port)
 */
class Client(val address:String) {

  /**
   * @return whether the current server is alive
   */
  def alive = {

    try{
      val response = new Connection(address).get

      new String(response).equals("Siigna")
    } catch {
      case e : Throwable => {
        Log("Couldn't connect",e)
        false
      }
    }
  }

  /**
   * @param session: Optionally a session to authenticate with
   * @return a new drawing id from the server
   */
  def getDrawingId(session:Session) = {
    val conn = new Connection(address+"/drawingId"+Client.sessionToUrl(session))

    java.lang.Long.parseLong(new String(conn.get))
  }

  /**
   * Get the "next" drawing on the server. Meaning just a new one
   * @param session: Authentication is required
   */
  def getDrawing(session:Session) = {

    val conn = new Connection(address+"/drawing/new"+Client.sessionToUrl(session))
    val res = conn.get

    Unmarshal[Model](res) match {

      case Some(m:Model) => Some(m)

      case None => {
        Log.error("Couldn't get a new drawing !")
        None
      }
    }
  }

  /**
   * Get an existing drawing which you have access to
   * @param drawingId: Drawing to get
   * @param session: Session with access to the drawng
   */
  def getDrawing(drawingId: Long, session:Session) = {
    val conn = new Connection(address+"/drawing/"+drawingId+Client.sessionToUrl(session))
    val res = conn.get

    Unmarshal[Model](res)
  }

  /**
   * Get an [amount] long range of shape.
   * @param amount: How many shapes to get
   * @param session: Requires auth
   */
  def getShapeIds(amount:Int, session:Session) = {

    val conn = new Connection(address+"/shapeId/"+amount+Client.sessionToUrl(session))
    val res = conn.get

    Unmarshal[Range](res)
  }

  /**
   * Performs a [[com.siigna.app.model.action.RemoteAction]] on the drawing id in the session
   * @param action
   * @param session
   * @return An action id from the server
   */
  def setAction(action:RemoteAction,session:Session) ={

    val conn = new Connection(address+"/action"+Client.sessionToUrl(session))

    val bytes = Marshal(action)

    new String(conn.post(bytes)).toInt
  }

  /**
   * Get's the action with the given id on the drawing that the session is authenticated with
   * @param actionId
   * @param session
   */
  def getAction(actionId:Int,session:Session) = {

    val conn = new Connection(address+"/action/"+actionId+Client.sessionToUrl(session))

    Unmarshal[RemoteAction](conn.get)
  }

  /**
   * Get the next action id for the drawing set in the session
   * @param session: specifies the drawing
   */
  def getActionId(session:Session) = {
    val conn = new Connection(address+"/actionId"+Client.sessionToUrl(session))
    new String(conn.get).toInt
  }

}

object Client {


  /**
   * Simple function to extract a session into a string for url in get requests
   * format: ?session={drawingId},{userId},{username},{userkey}
   * @param ses
   */
  def sessionToUrl(ses:Session) = {
    ses match {
      case Session(drawing,User(id,name,token)) => {
        "?session="+drawing+","+id+","+name+","+token
      }
      case _ => "session=invalid"
    }
  }
  
}
