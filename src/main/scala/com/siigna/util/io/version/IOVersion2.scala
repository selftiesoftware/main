/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.util.io.version

import com.siigna.util.io.{ObjectType, SiignaInputStream, SiignaOutputStream}
import org.ubjson.io.UBJFormatException
import com.siigna.app.model.action._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.controller.remote
import java.awt.geom.AffineTransform
import com.siigna.app.model.Model
import com.siigna.app.model.server.User
import remote.{Session, RemoteConstants}
import java.awt.Color
import reflect.runtime.universe._
import com.siigna.app.model.selection.{BitSetShapeSelector, ShapeSelector, FullShapeSelector, EmptyShapeSelector}
import scala.collection.immutable.BitSet

/**
 * The second version of I/O functionality in Siigna.
 * This version differs from the first by moving from ShapeParts to ShapeSelectors.
 */
object IOVersion2 extends IOVersion {

  /**
   * Attempts to retrieve the type parameters for the given type. Useful for extracting types from classes with
   * type parameters like Array[Int] => Int
   * @tparam E  The type to extract the type parameters from.
   * @return  The types of the type parameters for the given type E.
   * @throws IllegalArgumentException  If no type parameter could be found for type E
   */
  protected def getTypeParameters[E : TypeTag] = {
    val tpe = reflect.runtime.universe.typeOf[E]
    tpe match {                // The returned Type is not an instance of universe.Type - hence the cast
      case TypeRef(_, _, args) => args
      case _ => throw new IllegalArgumentException("Could not find type parameters in type " + tpe)
    }
  }

  /**
   * Attempt to verify the type of the given element <code>elem</code> as the same type, or subtype, of the
   * expected type <code>tpe</code>. If the element could not be recognized as a subtype, we throw an exception.
   * @param elem  The element to match.
   * @param expected  The expected type.
   * @throws ClassCastException  If the element could not be correctly verified to be a subtype of E
   */
  protected def verifyType(elem : Any, expected : Type) {
    val actual = com.siigna.util.io.mirror.reflect(elem).symbol.toType
    actual match {
      case x if x <:< expected =>   // We're good!
      case x if x =:= typeOf[java.lang.Byte]    && expected =:= typeOf[Byte]    => // We're also good
      case x if x =:= typeOf[java.lang.Boolean] && expected =:= typeOf[Boolean] => // We're also good
      case x if x =:= typeOf[java.lang.Integer] && expected =:= typeOf[Int]     => // We're also good
      case x if x =:= typeOf[java.lang.Long]    && expected =:= typeOf[Long]    => // We're also good
      case x if x =:= typeOf[java.lang.Double]  && expected =:= typeOf[Double]  => // We're also good
      case x if x =:= typeOf[java.lang.Float]   && expected =:= typeOf[Float]   => // We're also good
      case x              => { // We're not good - the types differ!
        throw new ClassCastException(s"Could not cast $x to expected type $expected.")
      }
    }
  }

  /**
   * Attempts to verify the given collection as an instance of E by retrieving the inner type-parameter for the
   * expected collection and matching that with each element.
   * @param col  The collection whose elements we want to verify.
   * @tparam E  The expected type of the collection.
   * @return  A collection of type E.
   * @throws  IllegalArgumentException  If we could not retrieve enough type parameters to match the inner type.
   */
  protected def verifyCollectionType[E : TypeTag](col : Traversable[Any]) : E = {
    // Avoid arrays since we cannot cast them
    require(!(typeOf[E] <:< typeOf[Array[_]]), "Cannot cast native java arrays - please use scala collections.")
    val types = getTypeParameters[E]
    require(types.size == 1, "Could not retrieve the necessary number of type parameters (1) from type " + typeOf[E])
    val tpe = types(0)
    col.foreach(e => verifyType(e, tpe))
    col.asInstanceOf[E]
  }

  /**
   * Attempts to verify the given map as an instance of by retrieving the inner type-parameters for the expected
   * map and matching that with each element.
   * @param map  The map whose elements we want to verify.
   * @tparam E  The expected type of the map.
   * @return  A map of type E.
   * @throws  IllegalArgumentException  If we could not retrieve enough type parameters to match the inner type.
   */
  protected def verifyMapType[E : TypeTag](map : Map[Any, Any]) : E = {
    val types = getTypeParameters[E]
    require(types.size == 2, "Could not retrieve the necessary number of type parameters (2) from type " + typeOf[E])
    val t1 = types(0)
    val t2 = types(1)
    map.foreach(t => { verifyType(t._1, t1); verifyType(t._2, t2) })
    map.asInstanceOf[E]
  }

  def readSiignaObject[E : TypeTag](in : SiignaInputStream, members : Int) : E = {
    // Retrieve the type of the siigna object
    in.checkMemberName("type")
    val byte = in.readByte()

    // Match the type
    if      (byte == ObjectType.AddAttributes) in.readType[E, AddAttributes](new AddAttributes(in.readMember[Map[Int, Attributes]]("shapes"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.ArcShape)      in.readType[E, ArcShape](new ArcShape(in.readMember[Vector2D]("center"), in.readMember[Double]("radius"),
                     in.readMember[Double]("startAngle"), in.readMember[Double]("angle"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.Attributes)    in.readType[E, Attributes](new Attributes(in.readMember[Map[String, Any]]("self")))
    else if (byte == ObjectType.BitSetShapeSelector) in.readType[E, BitSetShapeSelector](BitSetShapeSelector(BitSet() ++ in.readMember[Iterable[Int]]("xs")))
    else if (byte == ObjectType.CircleShape)   in.readType[E, CircleShape](new CircleShape(in.readMember[Vector2D]("center"), in.readMember[Double]("radius"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.Color)         in.readType[E, Color](new Color(in.readMember[Int]("color"), true))
    else if (byte == ObjectType.CreateShape)   in.readType[E, CreateShape](new CreateShape(in.readMember[Int]("id"), in.readMember[Shape]("shape")))
    else if (byte == ObjectType.CreateShapes)  in.readType[E, CreateShapes](new CreateShapes(in.readMember[Map[Int, Shape]]("shapes")))
    else if (byte == ObjectType.DeleteShape)   in.readType[E, DeleteShape](new DeleteShape(in.readMember[Int]("id"), in.readMember[Shape]("shape")))
    else if (byte == ObjectType.DeleteShapePart)    in.readType[E, DeleteShapePart](new DeleteShapePart(in.readMember[Int]("id"), in.readMember[Shape]("shape"), in.readMember[ShapeSelector]("part")))
    else if (byte == ObjectType.DeleteShapeParts)   in.readType[E, DeleteShapeParts](new DeleteShapeParts(in.readMember[Map[Int, Shape]]("oldShapes"), in.readMember[Map[Int, Shape]]("newShapes")))
    else if (byte == ObjectType.DeleteShapes)       in.readType[E, DeleteShapes](new DeleteShapes(in.readMember[Map[Int, Shape]]("shapes")))
    else if (byte == ObjectType.EmptyShapeSelector) in.readType[E, EmptyShapeSelector.type](EmptyShapeSelector)
    else if (byte == ObjectType.Error)              in.readType[E, remote.Error](remote.Error(in.readMember[Int]("code"), in.readMember[String]("message"), in.readMember[Session]("session")))
    else if (byte == ObjectType.FullShapeSelector)  in.readType[E, FullShapeSelector.type](FullShapeSelector)
    else if (byte == ObjectType.Get)           in.readType[E, remote.Get](remote.Get(RemoteConstants(in.readMember[Int]("constant")), in.readMember[Any]("value"), in.readMember[Session]("session")))
    else if (byte == ObjectType.GroupShape)    in.readType[E, GroupShape](new GroupShape(in.readMember[Seq[Shape]]("shapes"), in.readMember[Attributes]("attributes")))
      //case Type.ImageShape       => // Nothing here yet
      //case Type.ImageShapePart   => // Nothing here yet
    else if (byte == ObjectType.Traversable) {
        in.checkMemberName("array")
        var s = Seq[Any]()
        for (i <- 0 until in.readArrayLength()) s:+= in.readObject[Any]
        verifyCollectionType[E](s)
      }
    else if (byte == ObjectType.LineShape) in.readType[E, LineShape](new LineShape(in.readMember[Vector2D]("p1"), in.readMember[Vector2D]("p2"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.Map) {
        in.checkMemberName("map")
        val size  = in.readArrayLength() / 2 // We read two items at the time
        val array = new Array[Any](size).map(_ => in.readObject[Any] -> in.readObject[Any]).toMap
        verifyMapType[E](array)
      }
    else if (byte == ObjectType.Model) in.readType[E, Model]{
        new Model(in.readMember[Map[Int, Shape]]("shapes"), in.readMember[Seq[Action]]("executed"),
                  in.readMember[Seq[Action]]("undone"), in.readMember[Attributes]("attributes"))
      }
    else if (byte == ObjectType.PolylineArcShape)    in.readType[E, PolylineArcShape](new PolylineArcShape(in.readMember[Vector2D]("middle"), in.readMember[Vector2D]("point")))
    else if (byte == ObjectType.PolylineLineShape)   in.readType[E, PolylineLineShape](new PolylineLineShape(in.readMember[Vector2D]("point")))
    else if (byte == ObjectType.PolylineShapeClosed) in.readType[E, PolylineShape.PolylineShapeClosed](new PolylineShape.PolylineShapeClosed(in.readMember[Vector2D]("startPoint"), in.readMember[Seq[InnerPolylineShape]]("innerShapes"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.PolylineShapeOpen)   in.readType[E, PolylineShape.PolylineShapeOpen](new PolylineShape.PolylineShapeOpen(in.readMember[Vector2D]("startPoint"), in.readMember[Seq[InnerPolylineShape]]("innerShapes"), in.readMember[Attributes]("attributes")))
      //case Type.RectangleShapeComplex => // Nothing here yet
      //case Type.RectangleShapePart    => // Nothing here yet
      //case Type.RectangleShapeSimple  => // Nothing here yet
    else if (byte == ObjectType.Range)          in.readType[E, Range](new Range(in.readMember[Int]("start"), in.readMember[Int]("end"), in.readMember[Int]("step")))
    else if (byte == ObjectType.RectangleShape) in.readType[E, RectangleShape](new RectangleShape(in.readMember[Vector2D]("center"), in.readMember[Double]("width"), in.readMember[Double]("height"), in.readMember[Double]("rotation"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.RemoteAction)   in.readType[E, RemoteAction](new RemoteAction(in.readMember[Action]("action"), in.readMember[Boolean]("undo")))
    else if (byte == ObjectType.SequenceAction) in.readType[E, SequenceAction](new SequenceAction(in.readMember[Seq[Action]]("actions")))
    else if (byte == ObjectType.Session)        in.readType[E, Session](new Session(in.readMember[Long]("drawing"), in.readMember[User]("user")))
    else if (byte == ObjectType.Set)            in.readType[E, remote.Set](remote.Set(RemoteConstants(in.readMember[Int]("constant")), in.readMember[Any]("value"), in.readMember[Session]("session")))
    else if (byte == ObjectType.SetAttributes)  in.readType[E, SetAttributes](new SetAttributes(in.readMember[Map[Int, Attributes]]("shapes"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.TextShape)      in.readType[E, TextShape](new TextShape(in.readMember[String]("text"), in.readMember[Vector2D]("position"), in.readMember[Double]("scale"), in.readMember[Attributes]("attributes")))
    else if (byte == ObjectType.TransformationMatrix) in.readType[E, TransformationMatrix](new TransformationMatrix(new AffineTransform(in.readMember[Seq[Double]]("matrix").toArray)))
    else if (byte == ObjectType.TransformShape)       in.readType[E, TransformShape](new TransformShape(in.readMember[Int]("id"), in.readMember[TransformationMatrix]("transformation")))
    else if (byte == ObjectType.TransformShapeParts)  in.readType[E, TransformShapeParts](new TransformShapeParts(in.readMember[Map[Int, ShapeSelector]]("shapes"), in.readMember[TransformationMatrix]("transformation")))
    else if (byte == ObjectType.TransformShapes)      in.readType[E, TransformShapes](new TransformShapes(in.readMember[Traversable[Int]]("ids"), in.readMember[TransformationMatrix]("transformation")))
    else if (byte == ObjectType.User)     in.readType[E, User](new User(in.readMember[Long]("id"), in.readMember[String]("name"), in.readMember[String]("token")))
    else if (byte == ObjectType.Vector2D) in.readType[E, Vector2D](new Vector2D(in.readMember[Double]("x"), in.readMember[Double]("y")))
    else throw new UBJFormatException(in.getPosition, "SiignaInputStream: Unknown type: " + byte)
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
      case EmptyShapeSelector | FullShapeSelector => 0 // No members for the empty/full shape selectors needed
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
        out.writeByte(ObjectType.AddAttributes)
        out.writeMember("shapes", shapes)
        out.writeMember("attributes", attributes)
      }
      case SetAttributes(shapes, attributes) => {
        out.writeByte(ObjectType.SetAttributes)
        out.writeMember("shapes", shapes)
        out.writeMember("attributes", attributes)
      }
      case CreateShape(id, shape) => {
        out.writeByte(ObjectType.CreateShape)
        out.writeMember("id", id)
        out.writeMember("shape", shape)
      }
      case CreateShapes(shapes) => {
        out.writeByte(ObjectType.CreateShapes)
        out.writeMember("shapes", shapes)
      }
      case DeleteShape(id, shape) => {
        out.writeByte(ObjectType.DeleteShape)
        out.writeMember("id", id)
        out.writeMember("shape", shape)
      }
      case DeleteShapes(shapes) => {
        out.writeByte(ObjectType.DeleteShapes)
        out.writeMember("shapes", shapes)
      }
      case DeleteShapePart(id, shape, part) => {
        out.writeByte(ObjectType.DeleteShapePart)
        out.writeMember("id", id)
        out.writeMember("shape", shape)
        out.writeMember("part", part)
      }
      case DeleteShapeParts(oldShapes, newShapes) => {
        out.writeByte(ObjectType.DeleteShapeParts)
        out.writeMember("oldShapes", oldShapes)
        out.writeMember("newShapes", newShapes)
      }
      case SequenceAction(actions) => {
        out.writeByte(ObjectType.SequenceAction)
        out.writeMember("actions", actions)
      }
      case TransformShape(id, transformation) => {
        out.writeByte(ObjectType.TransformShape)
        out.writeMember("id", id)
        out.writeMember("transformation", transformation)
      }
      case TransformShapeParts(shapes, transformation) => {
        out.writeByte(ObjectType.TransformShapeParts)
        out.writeMember("shapes", shapes)
        out.writeMember("transformation", transformation)
      }
      case TransformShapes(ids, transformation) => {
        out.writeByte(ObjectType.TransformShapes)
        out.writeMember("ids", ids)
        out.writeMember("transformation", transformation)
      }

      // Fall-through
      case e => throw new UnsupportedOperationException("SiignaOutputStream: Did not recognize Action: " + e)
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

  /**
   * Writes a shape to the output stream.
   * @param out  The output stream to write to
   * @param shape  The shape to write.
   */
  protected def writeShape(out : SiignaOutputStream, shape : Shape) {
    shape match {
      case ArcShape(center, radius, startAngle, angle, attributes) => {
        out.writeByte(ObjectType.ArcShape)
        out.writeMember("center", center)
        out.writeMember("radius", radius)
        out.writeMember("startAngle", startAngle)
        out.writeMember("angle", angle)
        out.writeMember("attributes", attributes)
      }
      case CircleShape(center, radius, attributes) => {
        out.writeByte(ObjectType.CircleShape)
        out.writeMember("center", center)
        out.writeMember("radius", radius)
        out.writeMember("attributes", attributes)
      }
      case GroupShape(shapes, attributes) => {
        out.writeByte(ObjectType.GroupShape)
        out.writeMember("shapes", shapes)
        out.writeMember("attributes", attributes)
      }
      case LineShape(p1, p2, attributes) => {
        out.writeByte(ObjectType.LineShape)
        out.writeMember("p1", p1)
        out.writeMember("p2", p2)
        out.writeMember("attributes", attributes)
      }
      case p : PolylineShape => {
        p match {
          case _ : PolylineShape.PolylineShapeClosed => {
            out.writeByte(ObjectType.PolylineShapeClosed)
          }
          case _ : PolylineShape.PolylineShapeOpen => {
            out.writeByte(ObjectType.PolylineShapeOpen)
          }
        }
        out.writeMember("startPoint", p.startPoint)
        out.writeMember("innerShapes", p.innerShapes)
        out.writeMember("attributes", p.attributes)
      }
      case RectangleShape(center, width, height, rotation, attributes) => {
        out.writeByte(ObjectType.RectangleShape)
        out.writeMember("center", center)
        out.writeMember("width", width)
        out.writeMember("height", height)
        out.writeMember("rotation", rotation)
        out.writeMember("attributes", attributes)
      }
      case TextShape(text, position, scale, attributes) => {
        out.writeByte(ObjectType.TextShape)
        out.writeMember("text", text)
        out.writeMember("position", position)
        out.writeMember("scale", scale)
        out.writeMember("attributes", attributes)
      }
    }
  }

  /**
   * Writes a shape selector to the output stream.
   * @param out  The output stream to write to
   * @param selector  The [[com.siigna.app.model.selection.ShapeSelector]] to write
   */
  protected def writeShapeSelector(out : SiignaOutputStream, selector : ShapeSelector) {
    selector match {
      case BitSetShapeSelector(xs) => {
        out.writeByte(ObjectType.BitSetShapeSelector)
        out.writeMember("xs", xs.toIterable)
      }
      case EmptyShapeSelector => {
        out.writeByte(ObjectType.EmptyShapeSelector)
      }
      case FullShapeSelector => {
        out.writeByte(ObjectType.FullShapeSelector)
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
        out.writeByte(ObjectType.Attributes)
        out.writeMember("self", a.self)
      }
      case c : Color => {
        out.writeByte(ObjectType.Color)
        out.writeMember("color", (c.getAlpha << 24) | (c.getRed << 16) | (c.getGreen << 8) | c.getBlue)
      }
      case i : InnerPolylineShape => {
        i match {
          case s : PolylineArcShape => {
            out.writeByte(ObjectType.PolylineArcShape)
            out.writeMember("middle", s.middle)
            out.writeMember("point", s.point)
          }
          case s : PolylineLineShape => {
            out.writeByte(ObjectType.PolylineLineShape)
            out.writeMember("point", s.point)
          }
        }
      }
      case m : Model => {
        out.writeByte(ObjectType.Model)
        out.writeMember("shapes", m.shapes)
        out.writeMember("executed", m.executed)
        out.writeMember("undone", m.undone)
        out.writeMember("attributes", m.attributes)
      }
      case r : Range => {
        out.writeByte(ObjectType.Range)
        out.writeMember("start", r.start)
        out.writeMember("end", r.end)
        out.writeMember("step", r.step)
      }
      case r : RemoteAction => {
        out.writeByte(ObjectType.RemoteAction)
        out.writeMember("action", r.action)
        out.writeMember("undo", r.undo)
      }
      case remote.Error(code, message, session) => {
        out.writeByte(ObjectType.Error)
        out.writeMember("code", code)
        out.writeMember("message", message)
        out.writeMember("session", session)
      }
      case remote.Get(const, value, session) => {
        out.writeByte(ObjectType.Get)
        out.writeMember("constant", const.id)
        out.writeMember("value", value)
        out.writeMember("session", session)
      }
      case remote.Set(const, value, session) => {
        out.writeByte(ObjectType.Set)
        out.writeMember("constant", const.id)
        out.writeMember("value", value)
        out.writeMember("session", session)
      }
      case remote.Session(drawing, user) => {
        out.writeByte(ObjectType.Session)
        out.writeMember("drawing", drawing)
        out.writeMember("user", user)
      }
      case s : Shape => writeShape(out, s)
      case s : ShapeSelector => writeShapeSelector(out, s)
      case t : TransformationMatrix => {
        out.writeByte(ObjectType.TransformationMatrix)
        val m = new Array[Double](6)
        t.t.getMatrix(m)
        out.writeMember("matrix", m)
      }
      case User(id, name, token) => {
        out.writeByte(ObjectType.User)
        out.writeMember("id", id)
        out.writeMember("name", name)
        out.writeMember("token", token)
      }
      case v : Vector2D => {
        out.writeByte(ObjectType.Vector2D)
        out.writeMember("x", v.x)
        out.writeMember("y", v.y)
      }

      // Scala types
      case m : Map[_, _] => {
        out.writeByte(ObjectType.Map)
        out.writeString("map")
        out.writeArrayHeader(m.size * 2)
        m foreach (t => {
          out.writeObject(t._1)
          out.writeObject(t._2)
        })
      }
      case i : Iterable[_] => {
        out.writeByte(ObjectType.Traversable)
        out.writeString("array")
        out.writeArrayHeader(i.size)
        i foreach out.writeObject
      }
      case a : Array[_] => {
        out.writeByte(ObjectType.Traversable)
        out.writeString("array")
        out.writeArrayHeader(a.size)
        a foreach out.writeObject
      }

      // Fall-through
      case e => throw new IllegalArgumentException("SiignaOutputStream: Unknown object: " + e)
    }
  }

}
