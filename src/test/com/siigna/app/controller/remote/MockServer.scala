package com.siigna.app.controller.remote

import actors.DaemonActor
import actors.remote.RemoteActor._
import com.siigna.app.model.action.CreateShape
import com.siigna.app.model.shape.LineShape
import com.siigna.app.model.{Model, RemoteModel}
import com.siigna.util.collection.Attributes
import com.siigna.app.controller.remote.{RemoteConstants => RC}
import com.siigna.util.Serializer

/**
 * A mock server
 */
object MockServer extends DaemonActor {

  val ids = Stream.from(0, 1).iterator

  private var error : Option[Error] = None

  def action = CreateShape(ids.next(), shape)
  val drawing = new RemoteModel(new Model(Map(ids.next -> shape), Nil, Nil), Attributes())
  val shape = LineShape(0, 0, 10, 10)

  def act() {
    register('siigna, this)
    alive(20004)

    loop {
      react {
        case _ if (error.isDefined) => reply(error.get)
        case Get(RC.Drawing, _, s) => reply(Set(RC.Drawing, Serializer.writeDrawing(drawing), s))
        case _ => reply(error)
      }
    }
  }

  def returnError(e : Error) { error = Some(e) }

  def stopReturnError { error = None }

}
