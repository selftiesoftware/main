package com.siigna.util.io

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}
import org.ubjson.io.{ByteArrayInputStream, ByteArrayOutputStream}
import version.IOVersion2
import com.siigna.app.controller.remote
import java.awt.Color
import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.server.User
import com.siigna.app.model.action._
import com.siigna.app.model.Model
import com.siigna.app.model.selection._
import scala.collection.immutable.BitSet
import com.siigna.app.model.shape.PolylineShape.{PolylineShapeOpen, PolylineShapeClosed}

/**
 * Tests IO version 2.
 */
class IOVersion2Spec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  var arr : ByteArrayOutputStream = null
  var out : SiignaOutputStream = null
  var in : SiignaInputStream = null

  before {
    arr = new ByteArrayOutputStream()
    out = new SiignaOutputStream(arr, IOVersion2)
    in = new SiignaInputStream(new ByteArrayInputStream(arr.getArray), IOVersion2)
  }

  describe("IO version 2") {

    def user = User(16739213L, "John the Doe Doe", "WithADoePassWord")
    def attributes = Attributes("Color" -> new Color(132, 141, 255, 42), "LineWeight" -> 1.75)
    def session = remote.Session(876321L, user)
    def shape = PolylineShape(Vector2D(0, 0), Vector2D(10, 16), Vector2D(-1123.3218, 10238), Vector2D(0, 0))
    def shapes = Map[Int, Shape](13 -> LineShape(0, 0, 10, 10), -1 -> shape)
    def shapeParts = Map[Int, ShapeSelector](13 -> ShapeSelector(1), -1 -> ShapeSelector(BitSet(1, 3, 5)))
    def shapeSelector = ShapeSelector(BitSet(1, 3, 5, 2763, 12312))
    val transformation = TransformationMatrix(Vector2D(12931.3123, -123782.7653), -1273623.1234)
    def vector = Vector2D(-13213.123, 18301.239199)

    it ("can read AddAttributes") {
      val a = new AddAttributes(Map(0 -> attributes), attributes)
      out.writeObject(a)
      in.readObject[AddAttributes] should equal(a)
    }
    it ("can read and write ArcShape") {
      val a = new ArcShape(vector, 14123.1234, 199.5, 17.9, attributes)
      out.writeObject(a)
      in.readObject[ArcShape] should equal(a)
    }
    it ("can read and write ArcShapePart") {
      val a = ShapeSelector(4)
      out.writeObject(a)
      in.readObject[ShapeSelector] should equal(a)
    }
    it ("can read and write a Traversable") {
      val a = Traversable[Byte](13, 14, 15, 0, 14)
      out.writeObject(a)
      in.readObject[Traversable[Byte]] should equal(a)
    }

    it ("can read and write Attributes") {
      out.writeObject(attributes)
      in.readObject[Attributes] should equal(attributes)
    }
    it ("can read and write CircleShape") {
      val c = new CircleShape(vector, 123971.1234, attributes)
      out.writeObject(c)
      in.readObject[CircleShape] should equal(c)
    }
    it ("can read and write CircleShape part") {
      val a = ShapeSelector(4)
      out.writeObject(a)
      in.readObject[ShapeSelector] should equal(a)
    }
    it ("can read and write Color") {
      val c = new Color(108, 132, 234, 42)
      out.writeObject(c)
      in.readObject[Color] should equal(c)
    }
    it ("can read and write CreateShape") {
      val c = new CreateShape(1, shape)
      out.writeObject(c)
      in.readObject[CreateShape] should equal(c)
    }
    it ("can read and write CreateShapes") {
      val x = new CreateShapes(shapes)
      out.writeObject(x)
      in.readObject[CreateShapes] should equal(x)
    }
    it ("can read and write DeleteShape") {
      val x = new DeleteShape(14, shape)
      out.writeObject(x)
      in.readObject[DeleteShape] should equal(x)
    }
    it ("can read and write DeleteShapePart") {
      val x = new DeleteShapePart(14, shape, ShapeSelector(BitSet(1,3)))
      out.writeObject(x)
      in.readObject[DeleteShapePart] should equal(x)
    }
    it ("can read and write DeleteShapeParts") {
      val newShapes = Map(123 -> LineShape(0, 13, 14, -3))
      val x = new DeleteShapeParts(shapes, newShapes)
      out.writeObject(x)
      val y = in.readObject[DeleteShapeParts]
      y should equal(x)
    }
    it ("can read and write DeleteShapes") {
      val x = new DeleteShapes(shapes)
      out.writeObject(x)
      in.readObject[DeleteShapes] should equal(x)
    }
    it ("can read and write an empty shape part") {
      val x = EmptyShapeSelector
      out.writeObject(x)
      in.readObject[EmptyShapeSelector.type] should equal(x)
    }
    it ("can read and write a remote Error") {
      val x = remote.Error(420, "Enhance your calm", session)
      out.writeObject(x)
      in.readObject[remote.Error] should equal(x)
    }
    it ("can read and write a full shape part") {
      val x = FullShapeSelector
      out.writeObject(x)
      in.readObject[FullShapeSelector.type] should equal(x)
    }
    it ("can read and write a full shape part as a part of an action") {
      val x = new TransformShapeParts(Map(0 -> FullShapeSelector), transformation)
      out.writeObject(x)
      in.readObject[TransformShapeParts] should equal(x)
    }
    it ("can read and write the remote Get") {
      val x = remote.Get(remote.RemoteConstants.Drawing, 14, session)
      out.writeObject(x)
      in.readObject[remote.Get] should equal(x)
    }
    it ("can read and write a GroupShape") {
      val x = GroupShape(shapes.values)
      out.writeObject(x)
      in.readObject[GroupShape] should equal(x)
    }
    it ("can read and write a GroupShape Selector") {
      val x = shapeSelector
      out.writeObject(x)
      in.readObject[ShapeSelector] should equal(x)
    }
    it ("can read and write an Traversable") {
      val x = Traversable(1, 2, 5000)
      out.writeObject(x)
      in.readObject[Traversable[Int]] should equal(x)
    }
    it ("can read and write a LineShape") {
      val x = LineShape(10, 15, -12314, 21239, attributes)
      out.writeObject(x)
      in.readObject[LineShape] should equal(x)
    }
    it ("can read and write a LineShape part") {
      val x = ShapeSelector(1)
      out.writeObject(x)
      in.readObject[ShapeSelector] should equal(x)
    }
    it ("can read and write a Map") {
      val m = Map("Color" -> Color.red)
      out.writeObject(m)
      in.readObject[Map[String, Color]] should equal(m)
    }
    it ("can read and write a Model") {
      val executed = Seq(new CreateShape(3, shape))
      val undone = Seq(new TransformShape(31, TransformationMatrix(Vector2D(0, 13), 131.41)))
      val x = new Model(shapes, executed, undone, attributes)
      out.writeObject(x)
      in.readObject[Model] should equal(x)
    }
    it ("can read and write PolylineArcShape") {
      val x = PolylineArcShape(Vector2D(13, 123), Vector2D(-123, 87431))
      out.writeObject(x)
      in.readObject[PolylineArcShape] should equal(x)
    }
    it ("can read and write PolylineLineShape") {
      val x = PolylineLineShape(Vector2D(-131, 4123819))
      out.writeObject(x)
      in.readObject[PolylineLineShape] should equal(x)
    }
    it ("can read and write PolylineShapeClosed") {
      val x = new PolylineShape.PolylineShapeClosed(Vector2D(48183, 3180),
        Seq(PolylineLineShape(Vector2D(-123, 193)), PolylineArcShape(Vector2D(7813, -13712), Vector2D(13, 0))), attributes)
      out.writeObject(x)
      in.readObject[PolylineShapeClosed] should equal(x)
    }
    it ("can read and write PolylineShapeOpen") {
      val x = new PolylineShape.PolylineShapeOpen(Vector2D(48183, 3180),
        Seq(PolylineLineShape(Vector2D(-123, 193)), PolylineArcShape(Vector2D(7813, -13712), Vector2D(13, 0))), attributes)
      out.writeObject(x)
      in.readObject[PolylineShapeOpen] should equal(x)
    }
    it ("can read and write PolylineShape part") {
      val x = ShapeSelector(BitSet(13, 131, 1826, 1373672, 978637))
      out.writeObject(x)
      in.readObject[ShapeSelector] should equal(x)
    }
    it ("can read and write a Range") {
      val x = Range(1, 10)
      out.writeObject(x)
      in.readObject[Range] should equal(x)
    }
    it ("can read and write a RemoteAction") {
      val x = new RemoteAction(new CreateShape(13, LineShape(0, 0, 10, 10)))
      out.writeObject(x)
      in.readObject[RemoteAction] should equal(x)
    }
    it ("can read and write a SequenceAction") {
      val x = new SequenceAction(Seq(new CreateShape(13, LineShape(0, 0, 10, 10)), new DeleteShape(13, LineShape(0, 0, 10, 10))))
      out.writeObject(x)
      in.readObject[SequenceAction] should equal(x)
    }
    it ("can read and write a Session") {
      val a = remote.Session(14L, User(60L, "DinMor", "Hej Verden"))
      out.writeObject(a)
      in.readObject[remote.Session] should equal(a)
    }
    it ("can read and write a remote Set") {
      val set = remote.Set(remote.RemoteConstants.Action,
        new RemoteAction(new CreateShape(13, LineShape(0, 0, 10, 10)), false),
        remote.Session(14L, User(60L, "DinMor", "Hej Verden")))
      out.writeObject(set)
      in.readObject[remote.Set] should equal(set)
    }
    it ("can read and write a SetAttributes") {
      val x = new SetAttributes(Map(1 -> attributes), attributes)
      out.writeObject(x)
      in.readObject[SetAttributes] should equal(x)
    }
    it ("can read and write a TextShape") {
      val x = new TextShape("Hello world!", Vector2D(10, 320), 183021.32, attributes)
      out.writeObject(x)
      in.readObject[TextShape] should equal(x)
    }
    it ("can read and write a TextShape part") {
      val x = ShapeSelector(131)
      out.writeObject(x)
      in.readObject[ShapeSelector] should equal(x)
    }
    it ("can read and write a TransformationMatrix") {
      val x = transformation
      out.writeObject(x)
      in.readObject[TransformationMatrix] should equal(x)
    }
    it ("can read and write TransformShape") {
      val x = new TransformShape(123, transformation)
      out.writeObject(x)
      in.readObject[TransformShape] should equal(x)
    }
    it ("can read and write TransformShapeParts") {
      val x = new TransformShapeParts(shapeParts, transformation)
      out.writeObject(x)
      in.readObject[TransformShapeParts] should equal(x)
    }
    it ("can read and write TransformShapes") {
      val x = new TransformShapes(shapes.keys, transformation)
      out.writeObject(x)
      val y = in.readObject[TransformShapes]
      y.ids.toSeq should equal(x.ids.toSeq) // We can tolerate that the collection type is difference, hence toSeq
      y.transformation should equal(x.transformation)
    }
    it ("can read and write a User") {
      val x = user
      out.writeObject(x)
      in.readObject[User] should equal(x)
    }
    it ("can read and write a Vector2D") {
      val x = vector
      out.writeObject(x)
      in.readObject[Vector2D] should equal(x)
    }
    it ("can fail when writing an unknown type") {
      class Unknown(weirdParam : javax.naming.ConfigurationException)
      val x = new Unknown(new javax.naming.ConfigurationException("Unknown"))
      evaluating {
        out.writeObject(x)
      } should produce[IllegalArgumentException]
    }

    it ("can fail when attempting to persist volatile actions") {
      val x = new LoadDrawing(new Model())
      evaluating {
        out.writeObject(x)
      } should produce[IllegalArgumentException]
    }

  }

}
