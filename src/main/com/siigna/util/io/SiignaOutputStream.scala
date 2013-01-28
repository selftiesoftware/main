package com.siigna.util.io

import org.ubjson.io.UBJOutputStream
import java.io.OutputStream
import com.siigna.app.controller.remote
import com.siigna.util.geom.{Vector2D, TransformationMatrix}
import java.awt.Color
import remote.RemoteCommand
import com.siigna.app.model.shape._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.action._
import com.siigna.app.model.Model
import java.nio.ByteBuffer
import java.util

/**
 * An output stream capable of writing Siigna types to the underlying [[org.ubjson.io.UBJOutputStream]].
 * @param out  The OutputStream to write to.
 * @see [[http://ubjson.org]] Universal Binary JSON
 */
class SiignaOutputStream(out : OutputStream) extends UBJOutputStream(out) {

  /**
   * Writes a given object to the underlying output stream.
   * @param obj  The object to write
   * @throws IllegalArgumentException  If the object could not be recognized.
   */
  def write(obj : Any) {
    obj match {

      // Siigna types
      case a : Action => writeAction(a)
      case a : Attributes => writeAttributes(a)
      case i : InnerPolylineShape => writeInnerPolylineShape(i)
      case m : Model => writeModel(m)
      case r : RemoteAction => writeRemoteAction(r)
      case r : RemoteCommand => writeRemoteCommand(r)
      case s : Shape => writeShape(s)
      case s : ShapePart => writeShapePart(s)
      case t : TransformationMatrix => writeTransformationMatrix(t)
      case v : Vector2D => writeVector2D(v)

      // Simple types
      case null => writeNull()
      case b : Boolean => writeBoolean(b)
      case b : Byte => writeByte(b)
      case c : Char => writeByte(c.asInstanceOf[Byte])
      case s : Short => writeInt16(s)
      case i : Int => writeInt32(i)
      case l : Long => writeInt64(l)
      case d : Double => writeDouble(d)
      case f : Float => writeFloat(f)
      case b : BigDecimal => writeHuge(b.bigDecimal)
      case b : BigInt => writeHuge(b.bigInteger)
      case s : String => writeString(s)

      // Scala types
      case (t1 : Any, t2 : Any) => writeByte(Type.Tuple2); write(t1); write(t2)
      case (t1 : Any, t2 : Any, t3 : Any) => writeByte(Type.Tuple3); write(out, t1); write(out, t2); write(out, t3)
      case i : Iterable[Any] => i.foreach(write(out, _))

      // Java types
      case c : Color => writeInt32((c.getAlpha << 24) + (c.getBlue << 16) + (c.getGreen << 8) + c.getBlue)

      // Fall-through
      case e => throw new IllegalArgumentException("SiignaOutputStream: Unknown object type: " + e)
    }
  }

  /**
   * Writes an action to the output stream by writing the action contents.
   * @param action  The action to write.
   */
  def writeAction(action : Action) {
    action match {
      case v : VolatileAction => throw new IllegalArgumentException("Cannot persist VolatileActions: " + v)
      case AddAttributes(shapes, attributes) => {
        writeByte(Type.AddAttributes)
        write(shapes)
        writeAttributes(attributes)
      }
      case SetAttributes(shapes, attributes) => {
        writeByte(Type.SetAttributes)
        write(shapes)
        writeAttributes(attributes)
      }
      case CreateShape(id, shape) => {
        writeByte(Type.CreateShape)
        writeInt32(id)
        writeShape(shape)
      }
      case CreateShapes(shapes) => {
        writeByte(Type.CreateShapes)
        write(shapes)
      }
      case DeleteShape(id, shape) => {
        writeByte(Type.DeleteShape)
        writeInt32(id)
        writeShape(shape)
      }
      case DeleteShapePart(id, shape, part) => {
        writeByte(Type.DeleteShapePart)
        writeInt32(id)
        writeShape(shape)
        writeShapePart(part)
      }
      case DeleteShapeParts(oldShapes, newShapes) => {
        writeByte(Type.DeleteShapeParts)
        write(oldShapes)
        write(newShapes)
      }
      case SequenceAction(actions) => {
        writeByte(Type.SequenceAction)
        write(actions)
      }
      case TransformShape(id, transformation) => {
        writeByte(Type.TransformShape)
        writeInt32(id)
        writeTransformationMatrix(transformation)
      }
      case TransformShapeParts(shapes, transformation) => {
        writeByte(Type.TransformShapeParts)
        write(shapes)
        writeTransformationMatrix(transformation)
      }
      case TransformShapes(ids, transformation) => {
        writeByte(Type.TransformShapes)
        write(ids)
        writeTransformationMatrix(transformation)
      }

      // Fall-through
      case e => throw new UnsupportedOperationException("SiignaOutputStream: Did not recognize Action: " + e)
    }
  }

  /**
   * Writes attributes to the output stream.
   * @param attributes  The attributes to write.
   */
  def writeAttributes(attributes : Attributes) {
    writeByte(Type.Attributes)
    writeInt32(attributes.size)
    attributes foreach (t => {
      writeString(t._1)
      write(t._2)
    })
  }

  /**
   * Write an inner polyline shape to the output stream.
   * @param shape  The inner polyline shape to write
   */
  def writeInnerPolylineShape(shape : InnerPolylineShape) {
    shape match {
      case s : PolylineArcShape => {
        writeByte(Type.PolylineArcShape)
        writeVector2D(s.point)
        writeVector2D(s.middle)
      }
      case s : PolylineLineShape => {
        writeByte(Type.PolylineLineShape)
        writeVector2D(s.point)
      }
    }
  }

  /**
   * Writes a model to this output stream.
   * @param model  The model to write.
   */
  def writeModel(model : Model) {
    writeByte(Type.Model)
    write(model.shapes)
    write(model.executed)
    write(model.undone)
  }

  /**
   * Write a remote action by writing the inner action and the boolean undo-value.
   * @param action  The remote action to write.
   */
  def writeRemoteAction(action : RemoteAction) {
    writeByte(Type.RemoteAction)
    writeAction(action.action)
    writeBoolean(action.undo)
  }

  /**
   * Writes a RemoteCommand by writing the remote constant, the data of the command and the session.
   * @param command  The session to write
   */
  def writeRemoteCommand(command : remote.RemoteCommand) {
    command match {
      case remote.Error(code, message, session) => {
        writeByte(Type.Error)
        writeString(message)
        writeSession(session)
      }
      case remote.Get(const, value, session) => {
        writeByte(Type.Get)
        writeInt32(const.id)
        write(value)
        writeSession(session)
      }
      case remote.Set(const, value, session) => {
        writeByte(Type.Set)
        writeInt32(const.id)
        write(value)
        writeSession(session)
      }
    }
  }

  /**
   * Writes a session to the output stream.
   * @param session  The session to write.
   */
  def writeSession(session : remote.Session) {
    writeByte(Type.Session)
    writeInt64(session.drawing)
    writeInt64(session.user.id)
    writeString(session.user.name)
    writeString(session.user.token)
  }

  /**
   * Writes a shape to the output stream.
   * @param shape  The shape to write.
   */
  def writeShape(shape : Shape) {
    shape match {
      case ArcShape(center, radius, startAngle, angle, attributes) => {
        writeByte(Type.ArcShape)
        writeVector2D(center)
        writeDouble(radius)
        writeDouble(startAngle)
        writeDouble(angle)
        writeAttributes(attributes)
      }
      case CircleShape(center, radius, attributes) => {
        writeByte(Type.CircleShape)
        writeVector2D(center)
        writeDouble(radius)
        writeAttributes(attributes)
      }
      case GroupShape(shapes, attributes) => {
        writeByte(Type.GroupShape)
        write(shapes)
        writeAttributes(attributes)
      }
      case LineShape(p1, p2, attributes) => {
        writeByte(Type.LineShape)
        writeVector2D(p1)
        writeVector2D(p2)
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
        writeVector2D(p.startPoint)
        write(p.innerShapes)
      }
      case TextShape(text, position, scale, attributes) => {
        writeByte(Type.TextShape)
        writeString(text)
        writeVector2D(position)
        writeDouble(scale)
        writeAttributes(attributes)
      }
    }
  }

  /**
   * Writes a shape selector to the output stream.
   * @param part  The part to write
   */
  def writeShapePart(part : ShapePart) {
    part match {
      case ArcShape.Part(b) => {
        writeByte(Type.ArcShapePart)
        writeByte(b)
      }
      case CircleShape.Part(b) => {
        writeByte(Type.CircleShapePart)
        writeByte(b)
      }
      case GroupShape.Part(parts) => {
        writeByte(Type.GroupShapePart)
        write(parts)
      }
      case LineShape.Part(b) => {
        writeByte(Type.LineShapePart)
        writeBoolean(b)
      }
      case PolylineShape.Part(xs) => {
        writeByte(Type.PolylineShapePart)
        write(xs)
      }
      case TextShape.Part(b) => {
        writeByte(Type.TextShapePart)
        writeByte(b)
      }
    }
  }

  /**
   * Writes a TransformationMatrix to the output stream by writing the six underlying double-values of the
   * [[java.awt.geom.AffineTransform]].
   * @param matrix  The TransformationMatrix to write.
   */
  def writeTransformationMatrix(matrix : TransformationMatrix) {
    val m = new Array[Double](6)
    matrix.t.getMatrix(m)
    writeByte(Type.TransformationMatrix)
    writeDouble(m(0))
    writeDouble(m(1))
    writeDouble(m(2))
    writeDouble(m(3))
    writeDouble(m(4))
    writeDouble(m(5))
  }

  /**
   * Writes a Vector2D to the output stream.
   * @param vector  The vector to write - 2D
   */
  def writeVector2D(vector : Vector2D) {
    writeByte(Type.Vector2D)
    writeDouble(vector.x)
    writeDouble(vector.y)
  }

}
