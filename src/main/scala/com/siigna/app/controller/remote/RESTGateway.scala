package com.siigna.app.controller.remote

import com.siigna.app.model.Model
import com.siigna.app.model.action.RemoteAction
import com.siigna.app.model.server.User
import com.siigna.util.io.{Marshal, Unmarshal}
import java.net.URLEncoder

/**
 * Http REST gateway for siigna communication.
 * @param address: Fully qualified domain name for the base address of siigna server to use (including port).
 */
class RESTGateway(val address : String) {

  // The endpoint to communicate with
  protected val endpoint = new RESTEndpoint()

  /**
   * @return whether the current server is alive
   */
  def alive : Boolean = {
    val response = new Connection(address).get
    response.left.exists(new String(_).equals("Siigna"))
  }

  /**
   * Forces the endpoint to clean a drawing.
   * @param drawingId  The id of the drawing to clean.
   * @param session  The session to authenticate the user.
   * @return  Either a boolean indicating success or failure or a String indicating an error.
   */
  def cleanDrawing(drawingId : Long, session : Session) : Either[Boolean, String] = {
    endpoint.put(s"$address/drawing/${session.drawing}/clean/$drawingId" + RESTGateway.sessionToUrl(session), Array()).left
            .flatMap( bytes => Left(new String(bytes).equals("ok")))
  }

  /**
   * Gets the action with the given id on the drawing that the session is authenticated with
   * @param actionId  The id of the action to fetch from the server
   * @param session  The current session
   */
  def getAction(actionId:Int, session:Session) : Either[RemoteAction, String] = {
    endpoint.get(s"$address/drawing/${session.drawing}/action/$actionId" + RESTGateway.sessionToUrl(session)).left.flatMap(
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
    endpoint.get(s"$address/drawing/${session.drawing}/actions/${actionIds.mkString("/")}" + RESTGateway.sessionToUrl(session))
      .left.flatMap( bytes =>
      try {
        Unmarshal[Seq[RemoteAction]](bytes) match {
          case Some(actions) => Left(actions)
          case _ => Right("Could not de-serialise to a sequence of remote action")
        }
      } catch {
        case e : Throwable => Right(new String(bytes))
      }
    )
  }

  /**
   * Get the next action id for the drawing set in the session
   * @param session: specifies the drawing
   */
  def getActionId(session:Session) : Either[Int, String] = {
    endpoint.get(s"$address/drawing/${session.drawing}/action/latestId"+RESTGateway.sessionToUrl(session)).left.flatMap( bytes =>
      try {
        Left(java.lang.Integer.parseInt(new String(bytes)))
      } catch {
        case _ : Throwable => Right(new String(bytes))
      }
    )
  }

  /**
   * @param session: Optionally a session to authenticate with
   * @return a new drawing id from the server
   */
  def getNewDrawingId(session:Session) : Either[Long, String] = {
    endpoint.get(s"$address/drawing/new" + RESTGateway.sessionToUrl(session)).left.flatMap( bytes =>
      try {
        Left(java.lang.Long.parseLong(new String(bytes)))
      } catch {
        case _ : Throwable => Right("Unable to cast bytes to long")
      }
    )
  }

  /**
   * Get an existing drawing which you have access to
   * @param drawingId: Drawing to get
   * @param session: Session with read access to the drawng
   */
  def getDrawing(drawingId: Long, session:Session) : Either[Model, String] = {

    // Try to get the data
    endpoint.get(s"$address/drawing/$drawingId" + RESTGateway.sessionToUrl(session)).left.flatMap(
      Unmarshal[Model](_) match {
        case Some(model) => {
          // We got the data (model), now get the meta data
          val a = s"$address/drawing/${session.drawing}/metaData" + RESTGateway.sessionToUrl(session)
          endpoint.get(a).left.flatMap( bytes => {
            Unmarshal[Map[String,String]](bytes) match {

              case Some(m:Map[String,Any]) => {

                (m.get("lastaction"),m.get("openness")) match {

                  case (Some(lastaction),Some(openness)) => {
                    Left(
                      model.addAttribute("lastAction",lastaction.toInt)
                           .addAttribute("openness",openness.head)
                           .addAttribute("id",drawingId)
                    )
                  }
                  case _ => Right("Couldn't get meta data from returned map")
                }

              }

              case None => Right("Couldn't get meta data")
            }
          })
        }
        case _ => Right("Could get model (data)")
      }
    )
  }

  /**
   * Get an [amount] long range of shape.
   * @param amount: How many shapes to get
   * @param session: Requires auth
   */
  def getShapeIds(amount:Int, session:Session) : Either[Range, String] = {
    endpoint.get(s"$address/drawing/${session.drawing}/shapeIds/$amount" + RESTGateway.sessionToUrl(session)).left.flatMap(
      Unmarshal[Range](_) match {
        case Some(r) => Left(r)
        case _ => Right("Could not de-serialise the model")
      }
    )
  }

  /**
   * Examines whether the client can connect to an underlying endpoint.
   * @return True if a connection has been made successfully, false otherwise.
   */
  def isConnected = endpoint.isConnected

  /**
   * Performs a [[com.siigna.app.model.action.RemoteAction]] on the drawing id in the session
   * @param action
   * @param session
   * @return An action id from the server
   */
  def setAction(action:RemoteAction,session:Session) : Either[Int, String] = {
    val actionBytes = Marshal(action)
    endpoint.post(s"$address/drawing/${session.drawing}/action" + RESTGateway.sessionToUrl(session), actionBytes)
      .left.flatMap( bytes =>
      try {
        Left(new String(bytes).toInt)
      } catch {
        case _ : Throwable => Right("Unable to cast bytes to long")
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
    endpoint.post(s"$address/drawing/${session.drawing}/actions" + RESTGateway.sessionToUrl(session), actionBytes)
      .left.flatMap( bytes =>
      try {
        Left(new String(bytes).split(",").map(_.toInt))
      } catch {
        case _ : Throwable => Right("Unable to cast bytes to sequence of longs")
      }
    )
  }

}

object RESTGateway {


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
