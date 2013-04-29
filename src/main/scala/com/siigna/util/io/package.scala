package com.siigna.util

import java.io.File
import actors.Actor
import javax.swing.{UIManager, JFileChooser}
import javax.swing.filechooser.FileNameExtensionFilter
import java.security.{PrivilegedAction, AccessController}

/**
 * The persistence package is capable of converting objects into byte arrays (marshaling), reading objects from
 * byte arrays (unmarshaling) and storing and reading content from disc.
 *
 * <h2>Marshaling and unmarshalling</h2>
 * <p>
 *   - is very simple to achieve. We have designed the library to be as independent as possible. No classes needs to
 *   inherit any interface or implement methods. This also gives us the power to version the (un)marshal(l)ing, which
 *   is done beneath the [[com.siigna.util.io.version]] package.
 * </p>
 * <p>
 *   It is currently only possible to marshal and unmarshal primitives and selected Java and Scala classes. Both
 *   Map and Traversable are one of these examples, so if you lack any implementation it is always possible to throw the
 *   object data into a collection. A complete overview of which types are supported can be found in the
 *   [[com.siigna.util.io.ObjectType]] object, which reference the currently used data constants for object identification.
 * </p>
 * <h3>Examples on (un)marshaling</h3>
 * <p>
 *   To marshal objects you simply call the [[com.siigna.util.io.Marshal]] object with the data you would like to
 *   marshal like so:
 *   {{{
 *     import com.siigna.util.io.Marshal
 *     Marshal(123456789L)         // Array[Byte]
 *     Marshal("Hej Verden!")      // Array[Byte]
 *     Marshal(Seq(13, 142, 1392)) // Array[Byte]
 *   }}}
 *   The above mentioned examples will produce an array of bytes which can be used to send over network, store to
 *   a file etc.
 * </p>
 * <p>
 *   To unmarshal objects you simply call the [[com.siigna.util.io.Unmarshal]] object with the type of the
 *   object you expect to get back and the byte-array/byte buffer containing the marshaled data:
 *   {{{
 *     import com.siigna.util.io.Unmarshal
 *     Unmarshal[Long](byteArray)     // Some(123456789L)
 *     Unmarshal[String](byteArray)   // Some("Hej Verden!")
 *     Unmarshal[Seq[Int]](byteArray) // Some(Seq(13, 142, 1392))
 *   }}}
 *   The above mentioned examples will produce an Option[T] where T is the requested type. If any errors occurs the
 *   returned data will be None and a description of the error will be written to the [[com.siigna.util.Log]].
 * </p>
 * <p>
 *   It is important to note that for the time being it is not possible to retrieve native Arrays from the
 *   [[com.siigna.util.io.Unmarshal]] object. Instead, retrieve the data as a type like Traversable and use
 *   <code>.toArray</code> to cast it. The reason is that native java arrays cannot be casted at runtime, which
 *   makes is slighty difficult to cast the final objects. We will get around to doing this later, but if you have
 *   a specific need you are welcome to contact us at [[http://siigna.com/development]].
 * </p>
 *
 * <h2>Storing to and reading contents from disc</h2>
 *
 */
package object io {

  // A private class to perform type-safe callback invocations
  private[io] case class DialogueFunction[T](f : (File => T), read : Boolean, filters : Seq[FileNameExtensionFilter])

  private var dialogue : Option[JFileChooser] = None

  // Initialize the dialogue and the look and feel
  private val t = new Thread() {
    override def run() {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
      } catch {
        case e : Throwable => Log.warning("Dialogue: Error when setting the Look and Feel. Reverting to default.")
      }

      try {
        AccessController.doPrivileged(new PrivilegedAction[Unit] {
          def run() {
            val newDialogue = new JFileChooser()

            // Set the parameters
            newDialogue.setAcceptAllFileFilterUsed(false) // Do not accept files outside the filter range
            newDialogue.setDialogTitle("Siigna file chooser")
            newDialogue.setMultiSelectionEnabled(false)

            dialogue = Some(newDialogue)
          }
        })
      } catch {
        case e : Throwable => Log.warning(s"Dialogue: Error when creating dialogue instance: $e")
      }
    }
  }
  t.setPriority(Thread.MIN_PRIORITY)
  t.start()

  // A private IO actor which is used to execute IO functionality on files.
  // Not pretty, but can't think of an alternative
  private[io] val IOActor = new Actor() {
    def act() {
      loop {
        react {
          case DialogueFunction(f, read, filters) => {
            // Makes sure the look, feel and dialogue have been attempted to be set
            t.join()

            dialogue match {
              case Some(d) => {
                // Remove the old filters
                d.getChoosableFileFilters.foreach(d.removeChoosableFileFilter(_))

                // Set the filters
                filters.foreach(d.addChoosableFileFilter(_))

                val result = if (read) d.showOpenDialog(null) else d.showSaveDialog(null)

                if (result == JFileChooser.APPROVE_OPTION) {
                  val selectedFile = d.getSelectedFile

                  // If the file does not end with the right extension, append the extension
                  val extensions = d.getFileFilter.asInstanceOf[FileNameExtensionFilter].getExtensions
                  val file    = if (!extensions.exists(selectedFile.getName.endsWith(_))) {
                    new File(selectedFile.getAbsolutePath + "." + extensions.head)
                  } else selectedFile

                  if (!read && !file.exists()) file.createNewFile()
                  if (read) file.setReadable(true) else file.setWritable(true)

                  // Reply back
                  reply(f(file))
                } else {
                  reply("User aborted dialogue.")
                }
              }
              case e => reply(e)
            }
          }
        }
      }
    }
  }

  // Start the actor
  IOActor.start()

  /**
   * The ObjectType object provides enumerations for the types of objects we and read from byte-streams so we can
   * identify the type of the marshalled object when we unmarshal it. Used with the [[com.siigna.util.io.Marshal]] and
   * [[com.siigna.util.io.Unmarshal]] objects.
   */
  object ObjectType {
    // Remote package
    val Error   = 0.toByte
    val Get     = 1.toByte
    val Set     = 2.toByte
    val Session = 3.toByte
    val User    = 4.toByte

    // Util
    val Attributes = 50.toByte
    val TransformationMatrix = 51.toByte
    val Vector2D   = 52.toByte
    val Model      = 53.toByte

    // Scala
    val Traversable = 80.toByte
    val Map         = 81.toByte
    val Range       = 82.toByte

    // Java
    val Color    = 90.toByte

    // Actions
    val AddAttributes       = 100.toByte
    val SetAttributes       = 101.toByte
    val CreateShape         = 102.toByte
    val CreateShapes        = 103.toByte
    val DeleteShape         = 104.toByte
    val DeleteShapes        = 105.toByte
    val DeleteShapePart     = 106.toByte
    val DeleteShapeParts    = 107.toByte
    val RemoteAction        = 108.toByte
    val SequenceAction      = 109.toByte
    val TransformShape      = 110.toByte
    val TransformShapeParts = 111.toByte
    val TransformShapes     = 112.toByte

    // Shapes
    val ArcShape      = 200.toByte
    val CircleShape   = 201.toByte
    val GroupShape    = 202.toByte
    val ImageShape    = 203.toByte
    val LineShape     = 204.toByte
    val PolylineShapeClosed   = 205.toByte
    val PolylineShapeOpen     = 206.toByte
    val RectangleShapeSimple  = 207.toByte
    val RectangleShapeComplex = 208.toByte
    val TextShape     = 209.toByte

    // Inner polyline shapes
    val PolylineLineShape = 220.toByte
    val PolylineArcShape  = 221.toByte

    // Shape parts
    val ArcShapePart       = 230.toByte
    val CircleShapePart    = 231.toByte
    val GroupShapePart     = 232.toByte
    val ImageShapePart     = 233.toByte
    val LineShapePart      = 234.toByte
    val PolylineShapePart  = 235.toByte
    val RectangleShapePart = 236.toByte
    val TextShapePart      = 237.toByte
    val FullShapePart      = 238.toByte
    val EmptyShapePart     = 239.toByte
  }

}
