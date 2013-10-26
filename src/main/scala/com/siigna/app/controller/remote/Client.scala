package com.siigna.app.controller.remote

import com.siigna.app.model.Model
import com.siigna.util.Log
import com.siigna.app.model.action.RemoteAction
import com.siigna.app.model.server.User
import com.siigna.util.io.{Marshal, Unmarshal}
import java.net.URLEncoder

/**
 * Http Client for siigna communication
 * @param address: Fully qualified domain name for the base address of siigna server to use (including port)
 */
class Client(val address:String) {

  /**
   * @return whether the current server is alive
   */
  def alive = {
    val response = new Connection(address).get
    response.exists(new String(_).equals("Siigna"))
  }

  /**
   * @param session: Optionally a session to authenticate with
   * @return a new drawing id from the server
   */
  def getDrawingId(session:Session) : Option[Long] = {
    val conn = new Connection(address+"/drawingId"+Client.sessionToUrl(session))

    conn.get.map(s => {
      java.lang.Long.parseLong(new String(s))
    })
  }

  /**
   * Get the "next" drawing on the server. Meaning just a new one
   * @param session: Authentication is required
   */
  def getDrawing(session:Session) : Option[Model] = {

    val conn = new Connection(address+"/drawing/new"+Client.sessionToUrl(session))

    conn.get.flatMap(res => {
      Unmarshal[Model](res) match {
        case s : Some[Model] => s
        case None => {
          Log.error("Remote: Couldn't get a new drawing !")
          None
        }
      }
    })
  }

  /**
   * Get an existing drawing which you have access to
   * @param drawingId: Drawing to get
   * @param session: Session with access to the drawng
   */
  def getDrawing(drawingId: Long, session:Session) : Option[Model] = {
    val conn = new Connection(address+"/drawing/"+drawingId+Client.sessionToUrl(session))
    conn.get.flatMap(Unmarshal[Model])
  }

  /**
   * Get an [amount] long range of shape.
   * @param amount: How many shapes to get
   * @param session: Requires auth
   */
  def getShapeIds(amount:Int, session:Session) : Option[Range] = {
    val conn = new Connection(address+"/shapeId/"+amount+Client.sessionToUrl(session))
    conn.get.flatMap(Unmarshal[Range])
  }

  /**
   * Performs a [[com.siigna.app.model.action.RemoteAction]] on the drawing id in the session
   * @param action
   * @param session
   * @return An action id from the server
   */
  def setAction(action:RemoteAction,session:Session) : Option[Int] = {
    val conn = new Connection(address+"/action"+Client.sessionToUrl(session))
    val bytes = Marshal(action)
    conn.post(bytes).map(new String(_).toInt)
  }

  /**
   * Performs a number or [[com.siigna.app.model.action.RemoteAction]]s on the drawing id in the session
   * @param actions
   * @param session
   * @return An action id from the server
   */
  def setActions(actions:Seq[RemoteAction],session:Session) : Option[Seq[Int]] = {
    val conn = new Connection(address+"/actions"+Client.sessionToUrl(session))
    val bytes = Marshal(actions)
    conn.post(bytes).flatMap(Unmarshal[Seq[Int]])
  }

  /**
   * Gets the action with the given id on the drawing that the session is authenticated with
   * @param actionId  The id of the action to fetch from the server
   * @param session  The current session
   */
  def getAction(actionId:Int,session:Session) : Option[RemoteAction] = {
    val conn = new Connection(address+"/action/"+actionId+Client.sessionToUrl(session))
    conn.get.flatMap(Unmarshal[RemoteAction])
  }

  /**
   * Gets one or more actions with the given id on the drawing that the session is authenticated with
   * @param actionIds  The ids of the actions to fetch from the server
   * @param session  The current session
   */
  def getActions(actionIds : Seq[Int], session : Session) : Option[Seq[RemoteAction]] = {
    val conn = new Connection(address+"/actions/"+actionIds.mkString("/")+Client.sessionToUrl(session))
    conn.get.flatMap(b => Unmarshal[Seq[RemoteAction]](b))
  }

  /**
   * Get the next action id for the drawing set in the session
   * @param session: specifies the drawing
   */
  def getActionId(session:Session) : Option[Int] = {
    val conn = new Connection(address+"/actionId"+Client.sessionToUrl(session))
    conn.get.map(new String(_).toInt)
  }

}

object Client {


  /**
   * Simple function to extract a session into a string for url in get requests
   * format: ?session={drawingId},{userId},{username},{userkey}
   * @param ses
   */
  def sessionToUrl(ses:Session) = {
    def encode(str : String) = URLEncoder.encode(str, "UTF8")
    ses match {
      case Session(drawing,User(id,name,token)) => {
        s"?session=$drawing,$id,${encode(name)},${encode(token)}"
      }
      case _ => "session=invalid"
    }
  }
  
}
