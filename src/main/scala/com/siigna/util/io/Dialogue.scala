package com.siigna.util.io

import io.Source
import com.siigna.util.Log
import java.nio.file.{StandardOpenOption, Files}
import java.nio.channels.{WritableByteChannel, ReadableByteChannel}
import java.util
import java.nio.charset.Charset
import collection.JavaConversions
import scala.Some
import javax.swing.{UIManager, JFileChooser}
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.{OutputStream, InputStream, IOException, File}

/**
 * <h2>Dialogue</h2>
 * <p>
 *   The Dialogue object is an utility to reading and writing single files through a file-dialogue that allows
 *   the user to determine which file should be read or written to. The Dialogue object is meant to form the
 *   basics for I/O operations such as import, export etc.
 * </p>
 * <p>
 *   The class have also been created to allow I/O operations in restricted environments, such as applets.
 *   The Dialogue object will be loaded at runtime which ensures that whatever code being run here have been
 *   approved by the user. Since I/O is near-to impossible to achieve outside restricted environments it also
 *   gives the user (some) certainty that every I/O operation performed in Siigna is defined at the mainline
 *   (which is publicly available and open source) and not in potentially proprietary modules.
 *   So: Please use this object for I/O in sandboxed environments.
 * </p>
 *
 * <h2>Reading to a file</h2>
 * <h4>Read a small text file</h4>
 * If you have a small text-file you can easily read it into a string like so:
 * {{{
 *   Dialogue.readText() match {
 *     case Some(text) => // Success! The text is stored in 'text' value
 *     case _ => // Failure to retrieve the text
 *   }
 * }}}
 * <h4>Read a large text file</h4>
 * Instead of throwing the entire content to a string (which can be very expensive) you can get the lines one-by-one
 * and parse them for whatever purpose you want.
 * {{{
 *   Dialogue.readLines() match {
 *     case Some(lines) => // Success! You can iterate on the lines in the 'lines' value
 *     case _ => // Failure to retrieve the lines
 *   }
 * }}}
 * <h4>Read small binary files</h4>
 * To read files that are small enough to be placed in a single byte-array you can use the following:
 * {{{
 *   Dialogue.readBytes() match {
 *     case Some(bytes) => // Success! The content of the file is stored in the byte-array 'bytes'
 *     case _ => // Failure to retrieve the bytes
 *   }
 * }}}
 * Using the [[com.siigna.util.io.Unmarshal]] object from Siigna you can easily retrieve objects:
 * {{{
 *   Dialogue.readBytes().map(Unmarshal[Double])
 * }}}
 * <h4>Read larger files</h4>
 * To read larger files - both binary and textual - you can retrieve an input stream and get bytes from that
 * {{{
 *   Dialogue.readInputStream() match {
 *     case Some(inputStream) => // Success! The contents are in the 'inputStream' value and can be read from there
 *     case _ => // Failure to retrieve the stream
 *   }
 * }}}
 * We also provide a means to retrieve a scala [[scala.io.Source]] which is handy when working with character
 * encoding:
 * {{{
 *
 *   Dialogue.readSource() match {
 *     case Some(source) => // Success! The source can be taken and used as you see fit
 *     case _ => // Failure to retrieve the source
 *   }
 * }}}
 *
 * <h4>Read larger binary files (the advanced version)</h4>
 * To read files that can be parsed in chunks (ordered or structured data for instance), we recommend using
 * [[java.nio.channels.ReadableByteChannel]]s which are very efficient at reading bytes. They use
 * [[java.nio.ByteBuffer]]s to retrieve the content. A short example could look like this:
 * {{{
 *   Dialogue.readByteChannel() match {
 *     case Some(channel) => { // Success! We'll now try to get some content
 *       val buffer = ByteBuffer.allocate(2048) // First we allocate some room for the buffer
 *       val bytesRead = channel.read(buffer)   // Then we read 'buffer.remaining()' bytes from the file into our buffer
 *       // Now we can retrieve the data...
 *       val int = buffer.getInt()              // For instance an int
 *       // And so on...
 *     }
 *     case _ => // Failure to retrieve the byte channel
 *   }
 * }}}
 *
 * <h2>Writing to file</h2>
 * All the <code>write*</code> methods returns a boolean which is true on success and false on failure.
 * <h4>Writing a string</h4>
 * To write a string you simply call the writeText method:
 * {{{
 *   Dialogue.writeText("Hej Verden!")
 * }}}
 * <h4>Writing lines</h4>
 * If you wish to write a number of lines you are probably looking for the writeLines method:
 * {{{
 *   val lines = Seq("Hej", "Verden", "!")
 *   Dialogue.writeLines(lines)
 * }}}
 * <h4>Writing byte arrays</h4>
 * To write byte arrays that is convenient enough to be kept in memory you can use the writeBytes method
 * {{{
 *   val bytes = Array[Byte](...)
 *   Dialogue.writeBytes(bytes)
 * }}}
 * <h4>Writing to a channel or output stream</h4>
 * If you have special needs or if your data is just too large to be handled in strings or byte arrays you can
 * use the <code>writeChannel</code> or <code>writeOutputStream</code>. These methods takes a callback-method as
 * a parameter in which you can write to a WritableByteChannel or OutputStream. This will allow you to pack
 * much more data into a file if you wish.
 *
 * Using the <code>writeOutputStream</code> could look like this:
 * {{{
 *   Dialogue.writeOutputStream(out => {
 *     val bytes = Array[Byte](...)
 *     out.write(bytes)
 *   })
 * }}}
 *
 * Using the <code>writeChannel</code> could look like this:
 * {{{
 *   Dialogue.writeChannel(channel => {
 *     val buffer = ByteBuffer.allocate(2048) // First allocate some space to a byte buffer
 *     buffer.putString("Hi there")           // Then feed in data.
 *     ...
 *     channel.write(buffer)                  // Write the buffer to the file
 *     // Repeat if you wish
 *   })
 * }}}
 * In this way you can write small chunks at the time.
 *
 * <h4>Appending to files</h4>
 * Every method you call via the <code>write*</code> truncates all the content of the file when writing. If you
 * want another behaviour you can pass another StandardOpenOption to the write methods. Please refer to
 * [[http://docs.oracle.com/javase/7/docs/api/java/nio/file/StandardOpenOption.html the JavaDoc on StandardOpenOption]]
 * for more details on possible options.
 */
object Dialogue {

  // Initialize look & feel
  val t = new Thread() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    } catch {
      case e : Throwable => Log.warning("Dialogue: Error when setting the Look and Feel. Reverting to default.")
    }
  }
  t.setPriority(Thread.MIN_PRIORITY)
  t.start()

  /**
   * Opens a JFileChooser and lets the user choose a file to read. If the dialogue is not interrupted and
   * the file matches the given filter (if any), the function f is called and the value Some[T] is returned.
   * @param f  A callback function to extract data - if everything goes well.
   * @param read  The mode of the file dialogue, set as either read/load (true) or write/save (false)
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @tparam T  The return type of the method
   * @return  Some[File] if the user correctly selected a file that matched the filter (if any). If the file did
   *          not match or the user pressed cancel we return None.
   * @throws IOException  If an I/O error occurred when trying to read/write
   * @throws IllegalArgumentException  If the mode of the dialogue could not be recognized.
   */
  protected def openDialogue[T](f : File => T, read : Boolean, filters : Seq[FileNameExtensionFilter] = Nil) : Option[T] = {
    // Makes sure the look and feel have been set.
    t.join()
    try {
      // Create a dialogue
      val dialogue = new JFileChooser()

      // Set the parameters
      dialogue.setAcceptAllFileFilterUsed(false) // Do not accept files outside the filter range
      dialogue.setDialogTitle("Siigna file chooser")
      dialogue.setMultiSelectionEnabled(false)

      // Set the filters
      filters.foreach(dialogue.addChoosableFileFilter(_))

      // Activate it
      val result = if (read) dialogue.showOpenDialog(null) else dialogue.showSaveDialog(null)

      if (result == JFileChooser.APPROVE_OPTION) {
        val file = dialogue.getSelectedFile

        // If the file does not end with the right extension, append the extension
        val extensions = dialogue.getFileFilter.asInstanceOf[FileNameExtensionFilter].getExtensions
        val newFile    = if (!extensions.exists(file.getName.endsWith(_))) {
          new File(file.getAbsolutePath + "." + extensions.head)
        } else file

        val res : Any = IOActor !? IOAction(newFile, read, f)
        Some(res.asInstanceOf[T])
      } else {
        Log.info("Dialogue: User aborted dialogue.")
        None
      }
    } catch {
      case e : Throwable if !e.isInstanceOf[IOException] => {
        val doing = if (read) "retrieving" else "storing"
        Log.warning(s"Dialogue: Error when $doing data: $e.")
        None
      }
    }
  }

  /**
   * Attempts to read a file as an array of bytes. This is useful for small files that can fit in memory.
   * This can be efficiently coupled with the [[com.siigna.util.io.Unmarshal]] object of Siigna to read the item
   * from the retrieved byte-array. See above for examples.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @return  Some[Array[Byte] ] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readBytes(filters : FileNameExtensionFilter*) : Option[Array[Byte]] =
    openDialogue(f => Files.readAllBytes(f.toPath), read = true, filters)

  /**
   * Attempts to read a file as a byte channel. This is useful when dealing with large binary files that can be parsed
   * to chunks. See above for examples.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @return  Some[ReadableByteChannel] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readByteChannel(filters : FileNameExtensionFilter*) : Option[ReadableByteChannel] =
    openDialogue(f => Files.newByteChannel(f.toPath, util.EnumSet.of(StandardOpenOption.READ)), read = true, filters)

  /**
   * Attempts get the input channel of a file. This is useful when dealing with larger binary or textual
   * files. See above for examples.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @return  Some[InputStream] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readInputStream(filters : FileNameExtensionFilter*) : Option[InputStream] =
    openDialogue(f => Files.newInputStream(f.toPath, StandardOpenOption.READ), read = true, filters)

  /**
   * Attempts to read a file to a number of lines. This is useful when dealing with larger textual files which
   * might be too big to be efficiently handled in one take. See above for examples.
   * @param encoding  An optional parameter to specify the encoding of the read-operation. Defaults to UTF-8.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @return  Some[Iterator[String] ] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readLines(encoding : String = "UTF-8", filters : Seq[FileNameExtensionFilter] = Nil) : Option[Iterator[String]] =
    readSource(encoding, filters).map(_.getLines())

  /**
   * Attempts to read a file to a scala [[scala.io.Source]]. This is useful if you want an iterable representation
   * of the data and more options for reading a file then we are able to provide here.
   * @param encoding  An optional parameter to specify the encoding of the read-operation. Defaults to UTF-8.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @return  Some[Source] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readSource(encoding : String = "UTF-8", filters : Seq[FileNameExtensionFilter] = Nil) : Option[Source] =
    openDialogue(f => Source.fromFile(f, encoding), read = true, filters)

  /**
   * Attempts to read a file into a string. This is useful when dealing with small textual files that can be
   * handled in memory. See above for examples.
   * @param encoding  An optional parameter to specify the encoding of the read-operation. Defaults to UTF-8.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @return  Some[String] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readText(encoding : String = "UTF-8", filters : Seq[FileNameExtensionFilter] = Nil) : Option[String] =
    readSource(encoding, filters).map(_.getLines().mkString("\n"))

  /**
   * Writes the given byte array to a file the user chooses. This can be efficiently coupled with the
   * [[com.siigna.util.io.Marshal]] object of Siigna to cast the item you want to store into a byte-array.
   *
   * The method allows you to specify the method with which to open the file ([[java.nio.file.StandardOpenOption]])
   * which is set to truncate all the existing content before reading, as default. If you wish to change that
   * behaviour, you should set the parameter.
   * @param bytes  The bytes to write to file.
   * @param option  Specifies how the bytes are written. Defaults to StandardOpenOption.TRUNCATE_EXISTING which
   *                truncates all the content of the file away before writing.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeBytes(bytes : Array[Byte], filters : Seq[FileNameExtensionFilter] = Nil,
                 option : StandardOpenOption = StandardOpenOption.TRUNCATE_EXISTING) : Boolean = {
    openDialogue(f => Files.write(f.toPath, bytes, option), read = false, filters).isDefined
  }

  /**
   * Provides a [[java.nio.channels.WritableByteChannel]] that can be used to write any content to disc. The
   * parameter callback is a callback function that will be called with a byte channel when available.
   * @param callback  A function that takes a byte channel that can be used to store any number of data into the
   *                  file the user have chosen.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @param option  The option with which to open the file. Defaults to StandardOpenOption.WRITE.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeChannel(callback : WritableByteChannel => Unit, filters : Seq[FileNameExtensionFilter] = Nil,
                   option : StandardOpenOption = StandardOpenOption.TRUNCATE_EXISTING) : Boolean = {
    openDialogue(f => callback(Files.newByteChannel(f.toPath, option)), read = false, filters).isDefined
  }

  /**
   * Writes the given lines to a file the user chooses.
   *
   * The method allows you to specify the method with which to open the file ([[java.nio.file.StandardOpenOption]])
   * which is set to truncate all the existing content before reading, as default. If you wish to change that
   * behaviour, you should set the parameter.
   *
   * @param lines  The lines to write to the file chosen by the user.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @param encoding  The encoding with which to write the string. Defaults to "UTF-8".
   *                choose a file with a certain file-ending, for instance.
   * @param option  Specifies how the bytes are written. Defaults to StandardOpenOption.TRUNCATE_EXISTING which
   *                truncates all the content of the file away before writing.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeLines(lines : Iterable[String], filters : Seq[FileNameExtensionFilter] = Nil, encoding : String = "UTF-8",
                 option : StandardOpenOption = StandardOpenOption.TRUNCATE_EXISTING) : Boolean = {
    openDialogue(f => {
      val iterable = JavaConversions.asJavaIterable(lines)
      Files.write(f.toPath, iterable, Charset.forName(encoding), option)
    }, read = false, filters).isDefined
  }

  /**
   * Provides a [[java.io.OutputStream]] that can be used to write any content to disc. The
   * parameter callback is a callback function that will be called with a output stream, when available.
   * @param callback  A function that takes a byte channel that can be used to store any number of data into the
   *                  file the user have chosen.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @param option  The option with which to open the file. Defaults to StandardOpenOption.WRITE.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeOutputStream(callback : OutputStream => Unit, filters : Seq[FileNameExtensionFilter] = Nil,
                        option : StandardOpenOption = StandardOpenOption.TRUNCATE_EXISTING) : Boolean = {
    openDialogue(f => callback(Files.newOutputStream(f.toPath, option)), read = false, filters).isDefined
  }

  /**
   * Writes the given lines to a file the user chooses.
   *
   * The method allows you to specify the method with which to open the file ([[java.nio.file.StandardOpenOption]])
   * which is set to truncate all the existing content before reading, as default. If you wish to change that
   * behaviour, you should set the parameter.
   *
   * @param text  The text write to the file chosen by the user.
   * @param filters  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away unwanted files, or
   *                 help the user choose a file with a certain file-ending, for instance.
   * @param encoding  The encoding with which to write the string. Defaults to "UTF-8".
   * @param option  Specifies how the bytes are written. Defaults to StandardOpenOption.TRUNCATE_EXISTING which
   *                truncates all the content of the file away before writing.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeText(text : String, filters : Seq[FileNameExtensionFilter] = Nil, encoding : String = "UTF-8",
                option : StandardOpenOption = StandardOpenOption.TRUNCATE_EXISTING) : Boolean = {
    writeLines(Seq(text), filters, encoding, option)
  }

}
