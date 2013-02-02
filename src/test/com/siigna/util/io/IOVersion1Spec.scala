package com.siigna.util.io

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}
import org.ubjson.io.{ByteArrayInputStream, ByteArrayOutputStream}
import version.{IOVersion1, IOVersion}
import com.siigna.app.model.server.User
import com.siigna.app.model.action.RemoteAction
import com.siigna.app.controller.remote
import remote.{RemoteConstants, Session}
import java.awt.Color

/**
 * Tests IO version 1.
 */
class IOVersion1Spec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  var arr : ByteArrayOutputStream = null
  var out : SiignaOutputStream = null
  var in : SiignaInputStream = null

  before {
    arr = new ByteArrayOutputStream()
    out = new SiignaOutputStream(arr, IOVersion1)
    in = new SiignaInputStream(new ByteArrayInputStream(arr.getArray), IOVersion1)
  }

  describe("IO version 1") {

    it ("can read and write AddAttributes") { }
    it ("can read and write ArcShape") { }
    it ("can read and write ArcShapePart") { }
    it ("can read and write an array") {
      val a = Array[Byte](13, 14, 15, 0, 14)
      out.writeObject(a)
      in.readObject[Array[Byte]] should equal(a)
    }
    it ("can read and write Attributes") { }
    it ("can read and write CircleShape") { }
    it ("can read and write CircleShape part") { }
    it ("can read and write Color") { }
    it ("can read and write CreateShape") { }
    it ("can read and write CreateShapes") { }
    it ("can read and write DeleteShape") { }
    it ("can read and write DeleteShapePart") { }
    it ("can read and write DeleteShapeParts") { }
    it ("can read and write DeleteShapes") { }
    it ("can read and write a remote Error") { }
    it ("can read and write the remote Get") { }
    it ("can read and write a GroupShape") { }
    it ("can read and write a GroupShape part") { }
    it ("can read and write an Iterable") { }
    it ("can read and write a LineShape") { }
    it ("can read and write a LineShape part") { }
    it ("can read and write a Map") {
      val m = Map("Color" -> Color.red)
      out.writeObject(m)
      in.readObject[Map[String, Color]] should equal(m)
    }
    it ("can read and write a Model") { }
    it ("can read and write PolylineArcShape") { }
    it ("can read and write PolylineLineShape") { }
    it ("can read and write PolylineShapeClosed") { }
    it ("can read and write PolylineShapeOpen") { }
    it ("can read and write PolylineShape part") { }
    it ("can read and write a RemoteAction") { }
    it ("can read and write a SequenceAction") { }
    it ("can read and write a Session") {
      val a = Session(14L, User(60L, "DinMor", "Hej Verden"))
      out.writeObject(a)
      in.readObject[Session] should equal(a)
    }
    it ("can read and write a remote Set") {
      val set = remote.Set(RemoteConstants.ActionId, null, Session(14L, User(60L, "DinMor", "Hej Verden")))
      out.writeObject(set)
      in.readObject[remote.Set] should equal(set)
    }
    it ("can read and write a SetAttributes") { }
    it ("can read and write a TextShape") { }
    it ("can read and write a TextShape part") { }
    it ("can read and write a TransformationMatrix") { }
    it ("can read and write TransformShape") { }
    it ("can read and write TransformShapeParts") { }
    it ("can read and write TransformShapes") { }
    it ("can read and write a User") { }
    it ("can read and write a Vector2D") { }
    it ("can fail when reading an unknown type") {}
    it ("can fail when writing an unknown type") {}

  }

}
