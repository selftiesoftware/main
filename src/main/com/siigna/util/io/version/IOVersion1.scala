package com.siigna.util.io.version

import com.siigna.util.io.{Type, SiignaInputStream, SiignaOutputStream}
import org.ubjson.io.UBJFormatException
import com.siigna.app.model.action._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.controller.remote
import collection.mutable
import java.awt.geom.AffineTransform
import com.siigna.app.model.Model
import com.siigna.app.model.server.User
import remote.{Session, RemoteConstants}
import java.awt.Color
import reflect.runtime.universe._

/**
 * The first take at implementing I/O functionality in Siigna.
 */
object IOVersion1 extends IOVersion {

  def readSiignaObject(in : SiignaInputStream, members : Int) : (Type, Any) = {
    // TODO: Use the member-count

    // Retrieve the type of the siigna object
    in.checkMemberName("type")
    val byte = in.readByte()

    // Match the type
    byte match {
      case Type.AddAttributes    => typeOf[AddAttributes] -> new AddAttributes(in.readMember[Map[Int, Attributes]]("shapes"), in.readMember[Attributes]("attributes"))
      case Type.ArcShape         => typeOf[ArcShape] -> new ArcShape(in.readMember[Vector2D]("center"), in.readMember[Double]("radius"),
                     in.readMember[Double]("startAngle"), in.readMember("angle"), in.readMember[Attributes]("attributes"))
      case Type.ArcShapePart     => typeOf[ArcShape.Part] -> new ArcShape.Part(in.readMember[Byte]("part"))
      case Type.Array            => typeOf[Array[Any]] -> {
        in.checkMemberName("array")
        new Array[Any](in.readArrayLength()).map(_ => in.readObject._2)
      }
      case Type.Attributes       => typeOf[Attributes] -> new Attributes(in.readMember[Map[String, Any]]("self"))
      case Type.CircleShape      => typeOf[CircleShape] -> new CircleShape(in.readMember[Vector2D]("center"), in.readMember("radius"), in.readMember[Attributes]("attributes"))
      case Type.CircleShapePart  => typeOf[CircleShape.Part] -> new CircleShape.Part(in.readMember[Byte]("part"))
      case Type.Color            => typeOf[Color] -> new Color(in.readMember[Int]("color"), true)
      case Type.CreateShape      => typeOf[CreateShape] -> new CreateShape(in.readMember[Int]("id"), in.readMember[Shape]("shape"))
      case Type.CreateShapes     => typeOf[CreateShapes] -> new CreateShapes(in.readMember[Map[Int, Shape]]("shapes"))
      case Type.DeleteShape      => typeOf[DeleteShape] -> new DeleteShape(in.readMember[Int]("id"), in.readMember[Shape]("shape"))
      case Type.DeleteShapePart  => typeOf[DeleteShapePart] -> new DeleteShapePart(in.readMember[Int]("id"), in.readMember[Shape]("shape"), in.readMember[ShapePart]("part"))
      case Type.DeleteShapeParts => typeOf[DeleteShapeParts] -> new DeleteShapeParts(in.readMember[Map[Int, Shape]]("newShapes"), in.readMember[Map[Int, Shape]]("oldShapes"))
      case Type.DeleteShapes     => typeOf[DeleteShapes] -> new DeleteShapes(in.readMember[Map[Int, Shape]]("shapes"))
      case Type.Error            => typeOf[remote.Error] -> remote.Error(in.readMember[Int]("message"), in.readMember[String]("message"), in.readMember[Session]("session"))
      case Type.Get              => typeOf[remote.Get] -> remote.Get(RemoteConstants(in.readMember[Int]("constant")), in.readMember[Any]("value"), in.readMember[Session]("session"))
      case Type.GroupShape       => typeOf[GroupShape] -> new GroupShape(in.readMember[Seq[Shape]]("shapes"), in.readMember[Attributes]("attributes"))
      case Type.GroupShapePart   => typeOf[GroupShape.Part] -> GroupShape.Part(in.readMember[Map[Int, ShapePart]]("shapes"))
      //case Type.ImageShape       => // Nothing here yet
      //case Type.ImageShapePart   => // Nothing here yet
      case Type.Iterable         => typeOf[Iterable[Any]] -> {
        in.checkMemberName("array")
        new Array[Any](in.readArrayLength()).map(_ => in.readObject._2).toIterable
      }
      case Type.LineShape        => typeOf[LineShape] -> new LineShape(in.readMember[Vector2D]("p1"), in.readMember[Vector2D]("p2"), in.readMember[Attributes]("attributes"))
      case Type.LineShapePart    => typeOf[LineShape.Part] -> LineShape.Part(readBoolean())
      case Type.Map              => typeOf[Map[Any, Any]] -> {
        in.checkMemberName("map")
        val size = in.readArrayLength() / 2 // We read two items at the time
        new Array[Any](size).map(_ => in.readObject._2 -> in.readObject._2).toMap
      }
      case Type.Model            => typeOf[Model] -> {
        new Model(in.readMember[Map[Int, Shape]]("shapes"), in.readMember[Seq[Action]]("executed"),
                  in.readMember[Seq[Action]]("undone"), in.readMember[Attributes]("attributes"))
      }
      case Type.PolylineArcShape    => typeOf[PolylineArcShape] -> new PolylineArcShape(in.readMember[Vector2D]("point"), in.readMember[Vector2D]("middle"))
      case Type.PolylineLineShape   => typeOf[PolylineLineShape] -> new PolylineLineShape(in.readMember[Vector2D]("point"))
      case Type.PolylineShapeClosed => typeOf[PolylineShape.PolylineShapeClosed] -> new PolylineShape.PolylineShapeClosed(in.readMember[Vector2D]("startPoint"), in.readMember[Seq[InnerPolylineShape]]("innerShapes"), in.readMember[Attributes]("attributes"))
      case Type.PolylineShapeOpen   => typeOf[PolylineShape.PolylineShapeOpen] -> new PolylineShape.PolylineShapeOpen(in.readMember[Vector2D]("startPoint"), in.readMember[Seq[InnerPolylineShape]]("innerShapes"), in.readMember[Attributes]("attributes"))
      case Type.PolylineShapePart   => typeOf[PolylineShape.Part] -> PolylineShape.Part(mutable.BitSet() ++ in.readMember[Iterable[Int]]("xs"))
      //case Type.RectangleShapeComplex => // Nothing here yet
      //case Type.RectangleShapePart    => // Nothing here yet
      //case Type.RectangleShapeSimple  => // Nothing here yet
      case Type.RemoteAction     => typeOf[RemoteAction] -> new RemoteAction(in.readMember[Action]("action"), in.readMember[Boolean]("undo"))
      case Type.SequenceAction   => typeOf[SequenceAction] -> new SequenceAction(in.readMember[Seq[Action]]("actions"))
      case Type.Session          => typeOf[Session] -> new Session(in.readMember[Long]("drawing"), in.readMember[User]("user"))
      case Type.Set              => typeOf[remote.Set] -> remote.Set(RemoteConstants(in.readMember[Int]("constant")), in.readMember[Any]("value"), in.readMember[Session]("session"))
      case Type.SetAttributes    => typeOf[SetAttributes] -> new SetAttributes(in.readMember[Map[Int, Attributes]]("shapes"), in.readMember[Attributes]("attributes"))
      case Type.TextShape        => typeOf[TextShape] -> new TextShape(in.readMember[String]("text"), in.readMember[Vector2D]("position"), in.readMember("scale"), in.readMember[Attributes]("attributes"))
      case Type.TextShapePart    => typeOf[TextShape.Part] -> TextShape.Part(in.readMember[Byte]("part"))
      case Type.TransformationMatrix => typeOf[TransformationMatrix] -> new TransformationMatrix(new AffineTransform(in.readMember[Array[Double]]("matrix")))
      case Type.TransformShape       => typeOf[TransformShape] -> new TransformShape(in.readMember[Int]("id"), in.readMember[TransformationMatrix]("matrix"))
      case Type.TransformShapeParts  => typeOf[TransformShapeParts] -> new TransformShapeParts(in.readMember[Map[Int, ShapePart]]("shapes"), in.readMember[TransformationMatrix]("transformation"))
      case Type.TransformShapes      => typeOf[TransformShapes] -> new TransformShapes(in.readMember[Traversable[Int]]("ids"), in.readMember[TransformationMatrix]("transformation"))
      case Type.User     => typeOf[User] -> new User(in.readMember[Long]("id"), in.readMember[String]("name"), in.readMember[String]("token"))
      case Type.Vector2D => typeOf[Vector2D] -> new Vector2D(in.readMember[Double]("x"), in.readMember[Double]("y"))
      case e => throw new UBJFormatException(in.getPosition, "SiignaInputStream: Unknown type: " + e)
    }
  }

  /**
   * Retrieves the amount of members to be written by the given object. Used to write the object header as
   * described in the UBJSON format. In Siigna most of the objects to be serialized are case classes, which means
   * that the number of members should equal the number of parameters in the default constructor, which is what
   * we return.
   * @param obj  The object to examine
   * @return  A number describing the amount of objects to be written inside the object
   */
  protected def getMemberCount(obj : Any) : Int = {
    obj match {
      case a : Array[_] => 1 // One for the array of contents
      case a : Attributes => 1 // One for the actual map
      case c : Color => 1 // One for the color as an int
      case t : TransformationMatrix => 6 // A matrix has 6 double values underneath.. FYI
      case i : Iterable[Any] => 1 // One for the array of contents
      case _ => try {
        obj.getClass.getConstructors.apply(0).getParameterTypes.size
      } catch {
        case e : ArrayIndexOutOfBoundsException => {
          val tpe = obj.getClass.getName
          throw new IllegalArgumentException(s"IOVersion1: Could not understand object $obj of type $tpe.")
        }
      }
    }
  }

  /**
   * Writes an action to the output stream by writing the action contents.
   * @param out  The siigna output stream to write to
   * @param action  The action to write.
   */
  protected def writeAction(out : SiignaOutputStream, action : Action) {
    action match {
      case v : VolatileAction => throw new IllegalArgumentException("Cannot persist VolatileActions: " + v)

      case AddAttributes(shapes, attributes) => {
        out.writeByte(Type.AddAttributes)
        out.writeMember("shapes", shapes)
        out.writeMember("attributes", attributes)
      }
      case SetAttributes(shapes, attributes) => {
        out.writeByte(Type.SetAttributes)
        out.writeMember("shapes", shapes)
        out.writeMember("attributes", attributes)
      }
      case CreateShape(id, shape) => {
        out.writeByte(Type.CreateShape)
        out.writeMember("id", id)
        out.writeMember("shape", shape)
      }
      case CreateShapes(shapes) => {
        out.writeByte(Type.CreateShapes)
        out.writeMember("shapes", shapes)
      }
      case DeleteShape(id, shape) => {
        out.writeByte(Type.DeleteShape)
        out.writeMember("id", id)
        out.writeMember("shape", shape)
      }
      case DeleteShapePart(id, shape, part) => {
        out.writeByte(Type.DeleteShapePart)
        out.writeMember("id", id)
        out.writeMember("shape", shape)
        out.writeMember("part", part)
      }
      case DeleteShapeParts(oldShapes, newShapes) => {
        out.writeByte(Type.DeleteShapeParts)
        out.writeMember("oldShapes", oldShapes)
        out.writeMember("newShapes", newShapes)
      }
      case SequenceAction(actions) => {
        out.writeByte(Type.SequenceAction)
        out.writeMember("actions", actions)
      }
      case TransformShape(id, transformation) => {
        out.writeByte(Type.TransformShape)
        out.writeMember("id", id)
        out.writeMember("transformation", transformation)
      }
      case TransformShapeParts(shapes, transformation) => {
        out.writeByte(Type.TransformShapeParts)
        out.writeMember("shapes", shapes)
        out.writeMember("transformation", transformation)
      }
      case TransformShapes(ids, transformation) => {
        out.writeByte(Type.TransformShapes)
        out.writeMember("ids", ids)
        out.writeMember("transformation", transformation)
      }

      // Fall-through
      case e => throw new UnsupportedOperationException("SiignaOutputStream: Did not recognize Action: " + e)
    }
  }

  /**
   * Writes a shape to the output stream.
   * @param out  The output stream to write to
   * @param shape  The shape to write.
   */
  protected def writeShape(out : SiignaOutputStream, shape : Shape) {
    shape match {
      case ArcShape(center, radius, startAngle, angle, attributes) => {
        out.writeByte(Type.ArcShape)
        out.writeMember("center", center)
        out.writeMember("radius", radius)
        out.writeMember("startAngle", startAngle)
        out.writeMember("angle", angle)
        out.writeMember("attributes", attributes)
      }
      case CircleShape(center, radius, attributes) => {
        out.writeByte(Type.CircleShape)
        out.writeMember("center", center)
        out.writeMember("radius", radius)
        out.writeMember("attributes", attributes)
      }
      case GroupShape(shapes, attributes) => {
        out.writeByte(Type.GroupShape)
        out.writeMember("shapes", shapes)
        out.writeMember("attributes", attributes)
      }
      case LineShape(p1, p2, attributes) => {
        out.writeByte(Type.LineShape)
        out.writeMember("p1", p1)
        out.writeMember("p2", p2)
        out.writeMember("attributes", attributes)
      }
      case p : PolylineShape => {
        p match {
          case _ : PolylineShape.PolylineShapeClosed => {
            out.writeByte(Type.PolylineShapeClosed)
          }
          case _ : PolylineShape.PolylineShapeOpen => {
            out.writeByte(Type.PolylineShapeOpen)
          }
        }
        out.writeMember("startPoint", p.startPoint)
        out.writeMember("innerShapes", p.innerShapes)
      }
      case TextShape(text, position, scale, attributes) => {
        out.writeByte(Type.TextShape)
        out.writeMember("text", text)
        out.writeMember("position", position)
        out.writeMember("scale", scale)
        out.writeMember("attributes", attributes)
      }
    }
  }

  /**
   * Writes the header of the given object or array to the output stream.
   * @param obj  The object to write. If the object is a map or iterable we write it as an array type, otherwise
   *             we use the object mark. See the UBJSON specs.
   */
  protected def writeHeader(output : SiignaOutputStream, obj : Any) {
    val size = getMemberCount(obj) + 1 // Plus one for the type
    output.writeObjectHeader(size)
    output.writeString("type") // Prepare for 1-byte type annotation
  }

  def writeSiignaObject(out: SiignaOutputStream, obj : Any) {
    // Write the header of the object or array
    writeHeader(out, obj)

    // Write the object itself
    obj match {
      // Siigna types
      case a : Action => writeAction(out, a)
      case a : Attributes => {
        out.writeByte(Type.Attributes)
        out.writeMember("self", a.self)
      }
      case c : Color => {
        out.writeByte(Type.Color)
        out.writeMember("color", (c.getAlpha << 24) | (c.getRed << 16) | (c.getGreen << 8) | (c.getBlue))
      }
      case i : InnerPolylineShape => {
        i match {
          case s : PolylineArcShape => {
            out.writeByte(Type.PolylineArcShape)
            out.writeMember("point", s.point)
            out.writeMember("middle", s.middle)
          }
          case s : PolylineLineShape => {
            out.writeByte(Type.PolylineLineShape)
            out.writeMember("point", s.point)
          }
        }
      }
      case m : Model => {
        out.writeByte(Type.Model)
        out.writeMember("shapes", m.shapes)
        out.writeMember("executed", m.executed)
        out.writeMember("undone", m.undone)
        out.writeMember("attributes", m.attributes)
      }
      case r : RemoteAction => {
        out.writeByte(Type.RemoteAction)
        out.writeMember("action", r.action)
        out.writeMember("undo", r.undo)
      }
      case remote.Error(code, message, session) => {
        out.writeByte(Type.Error)
        out.writeMember("message", message)
        out.writeMember("session", session)
      }
      case remote.Get(const, value, session) => {
        out.writeByte(Type.Get)
        out.writeMember("constant", const.id)
        out.writeMember("value", value)
        out.writeMember("session", session)
      }
      case remote.Set(const, value, session) => {
        out.writeByte(Type.Set)
        out.writeMember("constant", const.id)
        out.writeMember("value", value)
        out.writeMember("session", session)
      }
      case remote.Session(drawing, user) => {
        out.writeByte(Type.Session)
        out.writeMember("drawing", drawing)
        out.writeMember("user", user)
      }
      case s : Shape => writeShape(out, s)
      case s : ShapePart => writeShapePart(out, s)
      case t : TransformationMatrix => {
        val m = new Array[Double](6)
        t.t.getMatrix(m)
        out.writeByte(Type.TransformationMatrix)
        out.writeMember("matrix", m)
      }
      case User(id, name, token) => {
        out.writeByte(Type.User)
        out.writeMember("id", id)
        out.writeMember("name", name)
        out.writeMember("token", token)
      }
      case v : Vector2D => {
        out.writeByte(Type.Vector2D)
        out.writeMember("x", v.x)
        out.writeMember("y", v.y)
      }

      // Scala types
      case m : Map[_, _] => {
        out.writeByte(Type.Map)
        out.writeString("map")
        out.writeArrayHeader(m.size * 2)
        m foreach (t => {
          out.writeObject(t._1)
          out.writeObject(t._2)
        })
      }
      case i : Iterable[_] => {
        out.writeByte(Type.Iterable)
        out.writeString("array")
        out.writeArrayHeader(i.size)
        i foreach out.writeObject
      }
      case a : Array[_] => {
        out.writeByte(Type.Array)
        out.writeString("array")
        out.writeArrayHeader(a.size)
        a foreach out.writeObject
      }

      // Fall-through
      case e => throw new IllegalArgumentException("SiignaOutputStream: Unknown object type: " + e)
    }
  }

  /**
   * Writes a shape selector to the output stream.
   * @param out  The output stream to write to
   * @param part  The part to write
   */
  protected def writeShapePart(out : SiignaOutputStream, part : ShapePart) {
    part match {
      case ArcShape.Part(b) => {
        out.writeByte(Type.ArcShapePart)
        out.writeMember("part", b)
      }
      case CircleShape.Part(b) => {
        out.writeByte(Type.CircleShapePart)
        out.writeMember("part", b)
      }
      case GroupShape.Part(parts) => {
        out.writeByte(Type.GroupShapePart)
        out.writeMember("part", parts)
      }
      case LineShape.Part(b) => {
        out.writeByte(Type.LineShapePart)
        out.writeMember("part", b)
      }
      case PolylineShape.Part(xs) => {
        out.writeByte(Type.PolylineShapePart)
        out.writeMember("part", xs)
      }
      case TextShape.Part(b) => {
        out.writeByte(Type.TextShapePart)
        out.writeMember("part", b)
      }
    }
  }

}
