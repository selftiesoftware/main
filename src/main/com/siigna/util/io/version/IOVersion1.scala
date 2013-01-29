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

/**
 * The first take at implementing I/O functionality in Siigna.
 */
object IOVersion1 extends IOVersion {

  def readSiignaObject(in : SiignaInputStream) : Any = {
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
      case Type.Model            => {
        new Model(in.readType[Map[Int, Shape]](s => s.readInt32() -> readType[Shape]), readType[Seq[Action]], readType[Seq[Action]], readType[Attributes])
      }
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
        writeSiignaObject(out, shapes)
        out.writeString("attributes")
        writeSiignaObject(out, attributes)
      }
      case SetAttributes(shapes, attributes) => {
        out.writeByte(Type.SetAttributes)
        out.writeString("shapes")
        writeSiignaObject(out, shapes)
        out.writeString("attributes")
        writeSiignaObject(out, attributes)
      }
      case CreateShape(id, shape) => {
        out.writeByte(Type.CreateShape)
        out.writeString("id")
        out.writeInt32(id)
        out.writeString("shape")
        writeSiignaObject(out, shape)
      }
      case CreateShapes(shapes) => {
        out.writeByte(Type.CreateShapes)
        out.writeString("shapes")
        writeSiignaObject(out, shapes)
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
        writeSiignaObject(out, oldShapes)
        out.writeString("newShapes")
        writeSiignaObject(out, newShapes)
      }
      case SequenceAction(actions) => {
        out.writeByte(Type.SequenceAction)
        out.writeString("actions")
        writeSiignaObject(out, actions)
      }
      case TransformShape(id, transformation) => {
        out.writeByte(Type.TransformShape)
        out.writeString("id")
        out.writeInt32(id)
        out.writeString("transformation")
        writeSiignaObject(out, transformation)
      }
      case TransformShapeParts(shapes, transformation) => {
        out.writeByte(Type.TransformShapeParts)
        out.writeString("shapes")
        writeSiignaObject(out, shapes)
        out.writeString("transformation")
        writeSiignaObject(out, transformation)
      }
      case TransformShapes(ids, transformation) => {
        out.writeByte(Type.TransformShapes)
        out.writeString("ids")
        writeSiignaObject(out, ids)
        out.writeString("transformation")
        writeSiignaObject(out, transformation)
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
        out.writeString("center")
        writeSiignaObject(out, center)
        out.writeString("radius")
        out.writeDouble(radius)
        out.writeString("startAngle")
        out.writeDouble(startAngle)
        out.writeString("angle")
        out.writeDouble(angle)
        out.writeString("attributes")
        writeSiignaObject(out, attributes)
      }
      case CircleShape(center, radius, attributes) => {
        out.writeByte(Type.CircleShape)
        out.writeString("center")
        writeSiignaObject(out, center)
        out.writeString("radius")
        out.writeDouble(radius)
        out.writeString("attributes")
        writeSiignaObject(out, attributes)
      }
      case GroupShape(shapes, attributes) => {
        out.writeByte(Type.GroupShape)
        out.writeString("shapes")
        writeSiignaObject(out, shapes)
        out.writeString("attributes")
        writeSiignaObject(out, attributes)
      }
      case LineShape(p1, p2, attributes) => {
        out.writeByte(Type.LineShape)
        out.writeString("p1")
        writeSiignaObject(out, p1)
        out.writeString("p2")
        writeSiignaObject(out, p2)
        out.writeString("attributes")
        writeSiignaObject(out, attributes)
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
        out.writeString("startPoint")
        writeSiignaObject(out, p.startPoint)
        out.writeString("innerShapes")
        writeSiignaObject(out, p.innerShapes)
      }
      case TextShape(text, position, scale, attributes) => {
        out.writeByte(Type.TextShape)
        out.writeString("text")
        out.writeString(text)
        out.writeString("position")
        writeSiignaObject(out, position)
        out.writeString("scale")
        out.writeDouble(scale)
        out.writeString("attributes")
        writeSiignaObject(out, attributes)
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
        writeSiignaObject(out, a.self)
      }
      case i : InnerPolylineShape => {
        i match {
          case s : PolylineArcShape => {
            out.writeByte(Type.PolylineArcShape)
            out.writeString("point")
            writeSiignaObject(out, s.point)
            out.writeString("middle")
            writeSiignaObject(out, s.middle)
          }
          case s : PolylineLineShape => {
            out.writeByte(Type.PolylineLineShape)
            out.writeString("point")
            writeSiignaObject(out, s.point)
          }
        }
      }
      case m : Model => {
        out.writeByte(Type.Model)
        out.writeString("shapes")
        writeSiignaObject(out, m.shapes)
        out.writeString("executed")
        writeSiignaObject(out, m.executed)
        out.writeString("undone")
        writeSiignaObject(out, m.undone)
        out.writeString("attributes")
        writeSiignaObject(out, m.attributes)
      }
      case r : RemoteAction => {
        out.writeByte(Type.RemoteAction)
        out.writeString("action")
        writeSiignaObject(out, r.action)
        out.writeString("undo")
        out.writeBoolean(r.undo)
      }
      case remote.Error(code, message, session) => {
        out.writeByte(Type.Error)
        out.writeString("message")
        out.writeString(message)
        out.writeString("session")
        writeSiignaObject(out, session)
      }
      case remote.Get(const, value, session) => {
        out.writeByte(Type.Get)
        out.writeString("constant")
        out.writeInt32(const.id)
        out.writeString("value")
        writeSiignaObject(out, value)
        out.writeString("session")
        writeSiignaObject(out, session)
      }
      case remote.Set(const, value, session) => {
        out.writeByte(Type.Set)
        out.writeString("constant")
        out.writeInt32(const.id)
        out.writeString("value")
        writeSiignaObject(out, value)
        out.writeString("session")
        writeSiignaObject(out, session)
      }
      case remote.Session(drawing, user) => {
        out.writeByte(Type.Session)
        out.writeInt64(drawing)
        writeSiignaObject(out, user)
      }
      case s : Shape => writeShape(out, s)
      case s : ShapePart => writeShapePart(out, s)
      case t : TransformationMatrix => {
        val m = new Array[Double](6)
        t.t.getMatrix(m)
        out.writeByte(Type.TransformationMatrix)
        out.writeArrayHeader(6)
        out.writeDouble(m(0))
        out.writeDouble(m(1))
        out.writeDouble(m(2))
        out.writeDouble(m(3))
        out.writeDouble(m(4))
        out.writeDouble(m(5))
      }
      case User(id, name, token) => {
        out.writeByte(Type.User)
        out.writeInt64(id)
        out.writeString(name)
        out.writeString(token)
      }
      case v : Vector2D => {
        out.writeByte(Type.Vector2D)
        out.writeString("x")
        out.writeDouble(v.x)
        out.writeString("y")
        out.writeDouble(v.y)
      }

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
        out.writeArrayHeader(i.size)
        i foreach out.writeObject
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

}
