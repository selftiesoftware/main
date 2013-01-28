package com.siigna.util.io.version

import java.io.OutputStream
import com.siigna.util.io.{Type, SiignaInputStream, SiignaOutputStream}
import org.ubjson.io.UBJFormatException
import com.siigna.app.model.action._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.controller.remote
import remote.RemoteCommand
import scala.remote
import collection.mutable
import java.awt.geom.AffineTransform
import com.siigna.app.model.Model

/**
 * The first take at implementing I/O functionality in Siigna.
 */
object IOVersion1 extends IOVersion {

  def readSiignaObject(input : SiignaInputStream) : Any = {
    val shapeExtractor = (s : SiignaInputStream) => s.readInt32() -> input.readType[Shape]

    // Match the type
    readByte() match {
      case Type.AddAttributes    => {
        new AddAttributes(input.readType[Map[Int, Attributes]], input.readType[Attributes])
      }
      case Type.ArcShape         => new ArcShape(readType[Vector2D], readDouble(), readDouble(), readDouble(), readType[Attributes])
      case Type.ArcShape         => new ArcShape(readType[Vector2D], readDouble(), readDouble(), readDouble(), readType[Attributes])
      case Type.ArcShapePart     => ArcShape.Part(readByte())
      case Type.Attributes       => Attributes(readMap[String, Any](s => s.readString() -> s.readObject))
      case Type.CircleShape      => new CircleShape(readType[Vector2D], readDouble(), readType[Attributes])
      case Type.CircleShapePart  => CircleShape.Part(readByte())
      case Type.CreateShape      => new CreateShape(readInt32(), readType[Shape])
      case Type.CreateShapes     => new CreateShapes(readMap[Int, Shape](shapeExtractor))
      case Type.DeleteShape      => new DeleteShape(readInt32(), readType[Shape])
      case Type.DeleteShapePart  => new DeleteShapePart(readInt32(), readType[Shape], readType[ShapePart])
      case Type.DeleteShapeParts => new DeleteShapeParts(readMap[Int, Shape](shapeExtractor), readMap[Int, Shape](shapeExtractor))
      case Type.DeleteShapes     => new DeleteShapes(readMap[Int, Shape](s => s.readInt32() -> s.readType[Shape]))
      case Type.Error            => remote.Error(readInt32(), readString(), readType[Session])
      case Type.Get              => remote.Get(RemoteConstants(readInt32()), readObject, readType[Session])
      case Type.GroupShape       => new GroupShape(readType[Seq[Shape]], readType[Attributes])
      case Type.GroupShapePart   => GroupShape.Part(readMap[Int, ShapePart](s => s.readInt32() -> s.readType[ShapePart]))
      case Type.ImageShape       => // Nothing here yet
      case Type.ImageShapePart   => // Nothing here yet
      case Type.LineShape        => new LineShape(readType[Vector2D], readType[Vector2D], readType[Attributes])
      case Type.LineShapePart    => LineShape.Part(readBoolean())
      case Type.Model            => readModel()
      case Type.PolylineArcShape      => new PolylineArcShape(readType[Vector2D], readType[Vector2D])
      case Type.PolylineLineShape     => new PolylineLineShape(readType[Vector2D])
      case Type.PolylineShapeClosed   => new PolylineShape.PolylineShapeClosed(readType[Vector2D], readType[Seq[InnerPolylineShape]], readType[Attributes])
      case Type.PolylineShapeOpen     => new PolylineShape.PolylineShapeOpen(readType[Vector2D], readType[Seq[InnerPolylineShape]], readType[Attributes])
      case Type.PolylineShapePart     => PolylineShape.Part(readType[mutable.BitSet])
      case Type.RectangleShapeComplex => // Nothing here yet
      case Type.RectangleShapePart    => // Nothing here yet
      case Type.RectangleShapeSimple  => // Nothing here yet
      case Type.RemoteAction     => new RemoteAction(readType[Action], readBoolean())
      case Type.SequenceAction   => new SequenceAction(readType[Seq[Action]])
      case Type.Session          => new Session(readInt64(), readType[User])
      case Type.Set              => remote.Set(RemoteConstants(readInt32()), readObject, readType[Session])
      case Type.SetAttributes    => new SetAttributes(readMap[Int, Attributes](s => s.readInt32() -> s.readType[Attributes]), readType[Attributes])
      case Type.TextShape        => new TextShape(readString(), readType[Vector2D], readDouble(), readType[Attributes])
      case Type.TextShapePart    => TextShape.Part(readByte())
      case Type.TransformationMatrix => new TransformationMatrix(new AffineTransform(readDouble, readDouble, readDouble, readDouble, readDouble, readDouble))
      case Type.TransformShape       => new TransformShape(readInt32(), readType[TransformationMatrix])
      case Type.TransformShapeParts  => new TransformShapeParts(readMap[Int, ShapePart](s => s.readInt32() -> s.readType[ShapePart]), readType[TransformationMatrix])
      case Type.TransformShapes      => new TransformShapes(readType[Traversable[Int]], readType[TransformationMatrix])
      case Type.User             => new User(readInt64(), readString(), readString())
      case Type.Vector2D         => new Vector2D(readDouble, readDouble)
      case e => throw new UBJFormatException(pos, "SiignaInputStream: Unknown type: " + e)
    }
  }

  /**
   * Attempts to read a Model type from the input stream.
   * @return  A Model containing shapes, executed and undone actions and attributes.
   */
  protected def readModel() : Model = {
    new Model(readMap[Int, Shape](s => s.readInt32() -> readType[Shape]), readType[Seq[Action]], readType[Seq[Action]], readType[Attributes])
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
      case a : Attributes => 2 // One for the type and one for the actual map
      case t : TransformationMatrix => 6 // A matrix has 6 double values underneath.. FYI
      case m : Map[_, _] => m.size * 2
      case i : Iterable[_] => i.size
      case _ => obj.getClass.getConstructors.apply(0).getParameterTypes.size
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
        out.writeString("shapes")
        out.writeObject(shapes)
        out.writeString("attributes")
        writeAttributes(out, attributes)
      }
      case SetAttributes(shapes, attributes) => {
        out.writeByte(Type.SetAttributes)
        out.writeString("shapes")
        out.writeObject(shapes)
        out.writeString("attributes")
        writeAttributes(out, attributes)
      }
      case CreateShape(id, shape) => {
        out.writeByte(Type.CreateShape)
        out.writeString("id")
        out.writeInt32(id)
        out.writeString("shape")
        writeShape(out, shape)
      }
      case CreateShapes(shapes) => {
        out.writeByte(Type.CreateShapes)
        out.writeString("shapes")
        out.writeObject(shapes)
      }
      case DeleteShape(id, shape) => {
        out.writeByte(Type.DeleteShape)
        out.writeString("id")
        out.writeInt32(id)
        out.writeString("shape")
        writeShape(out, shape)
      }
      case DeleteShapePart(id, shape, part) => {
        out.writeByte(Type.DeleteShapePart)
        out.writeString("id")
        out.writeInt32(id)
        out.writeString("shape")
        writeShape(out, shape)
        out.writeString("part")
        writeShapePart(out, part)
      }
      case DeleteShapeParts(oldShapes, newShapes) => {
        out.writeByte(Type.DeleteShapeParts)
        out.writeString("oldShapes")
        out.writeObject(oldShapes)
        out.writeString("newShapes")
        out.writeObject(newShapes)
      }
      case SequenceAction(actions) => {
        out.writeByte(Type.SequenceAction)
        out.writeString("actions")
        out.writeObject(actions)
      }
      case TransformShape(id, transformation) => {
        out.writeByte(Type.TransformShape)
        out.writeString("id")
        out.writeInt32(id)
        out.writeString("transformation")
        writeTransformationMatrix(out, transformation)
      }
      case TransformShapeParts(shapes, transformation) => {
        out.writeByte(Type.TransformShapeParts)
        out.writeString("shapes")
        out.writeObject(shapes)
        out.writeString("transformation")
        writeTransformationMatrix(out, transformation)
      }
      case TransformShapes(ids, transformation) => {
        out.writeByte(Type.TransformShapes)
        out.writeString("ids")
        out.writeObject(ids)
        out.writeString("transformation")
        writeTransformationMatrix(out, transformation)
      }

      // Fall-through
      case e => throw new UnsupportedOperationException("SiignaOutputStream: Did not recognize Action: " + e)
    }
  }

  /**
   * Write an inner polyline shape to the output stream.
   * @param out  The output stream to write to
   * @param shape  The inner polyline shape to write
   */
  protected def writeInnerPolylineShape(out : SiignaOutputStream, shape : InnerPolylineShape) {
    shape match {
      case s : PolylineArcShape => {
        writeByte(Type.PolylineArcShape)
        writeString("point")
        writeVector2D(s.point)
        writeString("middle")
        writeVector2D(s.middle)
      }
      case s : PolylineLineShape => {
        writeByte(Type.PolylineLineShape)
        writeString("point")
        writeVector2D(s.point)
      }
    }
  }

  /**
   * Writes a model to this output stream.
   * @param model  The model to write.
   */
  protected def writeModel(model : Model) {
    writeByte(Type.Model)
    writeMap(model.shapes)
    writeIterable(model.executed)
    writeIterable(model.undone)
    writeAttributes(model.attributes)
  }

  /**
   * Write a remote action by writing the inner action and the boolean undo-value.
   * @param action  The remote action to write.
   */
  protected def writeRemoteAction(action : RemoteAction) {
    writeByte(Type.RemoteAction)
    writeAction(action.action)
    writeBoolean(action.undo)
  }

  /**
   * Writes a RemoteCommand by writing the remote constant, the data of the command and the session.
   * @param command  The session to write
   */
  protected def writeRemoteCommand(command : remote.RemoteCommand) {
    command match {
      case remote.Error(code, message, session) => {
        writeByte(Type.Error)
        writeString(message)
        writeSession(session)
      }
      case remote.Get(const, value, session) => {
        writeByte(Type.Get)
        writeInt32(const.id)
        writeObject(value)
        writeSession(session)
      }
      case remote.Set(const, value, session) => {
        writeByte(Type.Set)
        writeInt32(const.id)
        writeObject(value)
        writeSession(session)
      }
    }
  }

  /**
   * Writes a session to the output stream.
   * @param session  The session to write.
   */
  protected def writeSession(session : remote.Session) {
    writeByte(Type.Session)
    writeInt64(session.drawing)
    writeByte(Type.User)
    writeInt64(session.user.id)
    writeString(session.user.name)
    writeString(session.user.token)
  }

  /**
   * Writes a shape to the output stream.
   * @param shape  The shape to write.
   */
  protected def writeShape(shape : Shape) {
    shape match {
      case ArcShape(center, radius, startAngle, angle, attributes) => {
        writeByte(Type.ArcShape)
        writeString("center")
        writeVector2D(center)
        writeString("radius")
        writeDouble(radius)
        writeString("startAngle")
        writeDouble(startAngle)
        writeString("angle")
        writeDouble(angle)
        writeString("attributes")
        writeAttributes(attributes)
      }
      case CircleShape(center, radius, attributes) => {
        writeByte(Type.CircleShape)
        writeString("center")
        writeVector2D(center)
        writeString("radius")
        writeDouble(radius)
        writeString("attributes")
        writeAttributes(attributes)
      }
      case GroupShape(shapes, attributes) => {
        writeByte(Type.GroupShape)
        writeString("shapes")
        writeObject(shapes)
        writeString("attributes")
        writeAttributes(attributes)
      }
      case LineShape(p1, p2, attributes) => {
        writeByte(Type.LineShape)
        writeString("p1")
        writeVector2D(p1)
        writeString("p2")
        writeVector2D(p2)
        writeString("attributes")
        writeAttributes(attributes)
      }
      case p : PolylineShape => {
        p match {
          case _ : PolylineShape.PolylineShapeClosed => {
            writeByte(Type.PolylineShapeClosed)
          }
          case _ : PolylineShape.PolylineShapeOpen => {
            writeByte(Type.PolylineShapeOpen)
          }
        }
        writeString("startPoint")
        writeVector2D(p.startPoint)
        writeString("innerShapes")
        writeObject(p.innerShapes)
      }
      case TextShape(text, position, scale, attributes) => {
        writeByte(Type.TextShape)
        writeString("text")
        writeString(text)
        writeString("position")
        writeVector2D(position)
        writeString("scale")
        writeDouble(scale)
        writeString("attributes")
        writeAttributes(attributes)
      }
    }
  }

  /**
   * Writes the header of the given object or array to the output stream.
   * @param obj  The object to write. If the object is a map or iterable we write it as an array type, otherwise
   *             we use the object mark. See the UBJSON specs.
   */
  protected def writeHeader(output : SiignaOutputStream, obj : Any) {
    val size = getMemberCount(obj)
    obj match {
      case _ : Map[_, _] | _ : Iterable[_] => output.writeArrayHeader(size)
      case _ => {
        output.writeObjectHeader(size)
        output.writeString("type") // Prepare for 1-byte type annotation
      }
    }
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
        out.writeString("self")
        writeMap(out, attributes.self)
      }
      case i : InnerPolylineShape => writeInnerPolylineShape(out, i)
      case m : Model => writeModel(out, m)
      case r : RemoteAction => writeRemoteAction(out, r)
      case r : RemoteCommand => writeRemoteCommand(out, r)
      case s : Shape => writeShape(s)
      case s : ShapePart => writeShapePart(s)
      case t : TransformationMatrix => writeTransformationMatrix(t)
      case v : Vector2D => writeVector2D(v)

      // Scala types
      case m : Map[_, _] => {
        out.writeByte(Type.Map)
        m foreach (t => {
          out.writeArrayHeader(2)
          out.writeObject(t._1)
          out.writeObject(t._2)
        })
      }
      case i : Iterable[_] => {
        out.writeByte(Type.Iterable)

        writeIterable(i)
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
        out.writeString("part")
        out.writeByte(b)
      }
      case CircleShape.Part(b) => {
        out.writeByte(Type.CircleShapePart)
        out.writeString("part")
        out.writeByte(b)
      }
      case GroupShape.Part(parts) => {
        out.writeByte(Type.GroupShapePart)
        out.writeString("part")
        out.writeObject(parts)
      }
      case LineShape.Part(b) => {
        out.writeByte(Type.LineShapePart)
        out.writeString("part")
        out.writeBoolean(b)
      }
      case PolylineShape.Part(xs) => {
        out.writeByte(Type.PolylineShapePart)
        out.writeString("part")
        out.writeObject(xs)
      }
      case TextShape.Part(b) => {
        out.writeByte(Type.TextShapePart)
        out.writeString("part")
        out.writeByte(b)
      }
    }
  }

  /**
   * Writes a TransformationMatrix to the output stream by writing the six underlying double-values of the
   * [[java.awt.geom.AffineTransform]].
   * @param out  The output stream to write to
   * @param matrix  The TransformationMatrix to write.
   */
  protected def writeTransformationMatrix(out : SiignaOutputStream, matrix : TransformationMatrix) {
    val m = new Array[Double](6)
    matrix.t.getMatrix(m)
    out.writeByte(Type.TransformationMatrix)
    out.writeArrayHeader(6)
    out.writeDouble(m(0))
    out.writeDouble(m(1))
    out.writeDouble(m(2))
    out.writeDouble(m(3))
    out.writeDouble(m(4))
    out.writeDouble(m(5))
  }

  /**
   * Writes a Vector2D to the output stream.
   * @param out  The Siigna output stream to write to
   * @param vector  The vector to write - 2D
   */
  protected def writeVector2D(out : SiignaOutputStream, vector : Vector2D) {
    out.writeByte(Type.Vector2D)
    out.writeString("x")
    out.writeDouble(vector.x)
    out.writeString("y")
    out.writeDouble(vector.y)
  }
}
