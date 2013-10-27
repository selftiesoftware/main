package com.siigna.app.controller.remote

import com.siigna.app.model.Model
import com.siigna.app.model.action.RemoteAction
import com.siigna.app.model.server.User
import com.siigna.util.io.{Marshal, Unmarshal}
import java.net.URLEncoder

/**
 * Http Client for siigna communication
 * @param address: Fully qualified domain name for the base address of siigna server to use (including port)
 */
class Client(val address:String) {

  val endpoint = new RESTEndpoint(address, 80)

  /**
   * @return whether the current server is alive
   */
  def alive = {
    val response = new Connection(address).get
    response.exists(new String(_).equals("Siigna"))
  }

  /**
   * Gets the action with the given id on the drawing that the session is authenticated with
   * @param actionId  The id of the action to fetch from the server
   * @param session  The current session
   */
  def getAction(actionId:Int, session:Session) : Either[RemoteAction, String] = {
    endpoint.get(address+"/action/"+actionId+Client.sessionToUrl(session)).left.flatMap(
      Unmarshal[RemoteAction](_) match {
        case Some(action) => Left(action)
        case _ => Right("Could not de-serialise to a remote action")
      }
    )
  }

  /**
   * Gets one or more actions with the given id on the drawing that the session is authenticated with
   * @param actionIds  The ids of the actions to fetch from the server
   * @param session  The current session
   */
  def getActions(actionIds : Seq[Int], session : Session) : Either[Seq[RemoteAction], String] = {
    endpoint.get(address+"/actions/"+actionIds.mkString("/")+Client.sessionToUrl(session)).left.flatMap(
      Unmarshal[Seq[RemoteAction]](_) match {
        case Some(actions) => Left(actions)
        case _ => Right("Could not de-serialise to a sequence of remote action")
      }
    )
  }

  /**
   * Get the next action id for the drawing set in the session
   * @param session: specifies the drawing
   */
  def getActionId(session:Session) : Either[Int, String] = {
    endpoint.get(address+"/actionId"+Client.sessionToUrl(session)).left.flatMap( bytes =>
      try {
        Left(java.lang.Integer.parseInt(new String(bytes)))
      } catch {
        case _ : Throwable => Right("Unable to case bytes to Int")
      }
    )
  }

  /**
   * Get the "next" drawing on the server. Meaning just a new one
   * @param session: Authentication is required
   */
  def getNewDrawing(session:Session) : Either[Model, String] = {
    endpoint.get(address+"/drawing/new"+Client.sessionToUrl(session)).left.flatMap(
      Unmarshal[Model](_) match {
        case Some(model) => Left(model)
        case _ => Right("Could not de-serialise the model")
      }
    )
  }

  /**
   * @param session: Optionally a session to authenticate with
   * @return a new drawing id from the server
   */
  def getNewDrawingId(session:Session) : Either[Long, String] = {
    endpoint.get(address+"/drawingId"+Client.sessionToUrl(session)).left.flatMap( bytes =>
      try {
        Left(java.lang.Long.parseLong(new String(bytes)))
      } catch {
        case _ : Throwable => Right("Unable to case bytes to long")
      }
    )
  }

  /**
   * Get an existing drawing which you have access to
   * @param drawingId: Drawing to get
   * @param session: Session with access to the drawng
   */
  def getDrawing(drawingId: Long, session:Session) : Either[Model, String] = {
    endpoint.get(address+"/drawing/"+drawingId+Client.sessionToUrl(session)).left.flatMap(
      Unmarshal[Model](_) match {
        case Some(model) => Left(model)
        case _ => Right("Could not de-serialise the model")
      }
    )
  }

  /**
   * Get an [amount] long range of shape.
   * @param amount: How many shapes to get
   * @param session: Requires auth
   */
  def getShapeIds(amount:Int, session:Session) : Either[Range, String] = {
    endpoint.get(address+"/shapeId/"+amount+Client.sessionToUrl(session)).left.flatMap(
      Unmarshal[Range](_) match {
        case Some(r) => Left(r)
        case _ => Right("Could not de-serialise the model")
      }
    )
  }

  /**
   * Performs a [[com.siigna.app.model.action.RemoteAction]] on the drawing id in the session
   * @param action
   * @param session
   * @return An action id from the server
   */
  def setAction(action:RemoteAction,session:Session) : Either[Int, String] = {
    val actionBytes = Marshal(action)
    endpoint.post(address+"/action"+Client.sessionToUrl(session), actionBytes).left.flatMap( bytes =>
      try {
        Left(java.lang.Integer.parseInt(new String(bytes)))
      } catch {
        case _ : Throwable => Right("Unable to case bytes to long")
      }
    )
  }

  /**
   * Performs a number or [[com.siigna.app.model.action.RemoteAction]]s on the drawing id in the session
   * @param actions
   * @param session
   * @return An action id from the server
   */
  def setActions(actions:Seq[RemoteAction],session:Session) : Either[Seq[Int], String] = {
    val actionBytes = Marshal(actions)
    endpoint.post(address+"/actions"+Client.sessionToUrl(session), actionBytes).left.flatMap(
      Unmarshal[Seq[Int]](_) match {
        case Some(r) => Left(r)
        case _ => Right("Could not de-serialise the model")
      }
    )
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
