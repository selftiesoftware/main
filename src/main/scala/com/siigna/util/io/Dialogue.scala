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

package com.siigna.util.io

import io.Source
import java.nio.file.{OpenOption, StandardOpenOption, Files}
import java.nio.channels.{FileChannel, ReadableByteChannel}
import java.util
import java.nio.charset.Charset
import javax.swing.filechooser.FileNameExtensionFilter
import java.nio.ByteBuffer
import java.io._
import java.awt.Toolkit

/**
 * <h2>Dialogue</h2>
 * <p>
 *   The Dialogue object is an utility to reading and writing single files through a file-dialogue that allows
 *   the user to determine which file should be read or written to. The Dialogue object is meant to form the
 *   basics for I/O operations such as import, export etc.
 * </p>
 * <p>
 *   The class has also been created to allow I/O operations in restricted environments, such as applets.
 *   The Dialogue object will be loaded at runtime which ensures that whatever code being run here have been
 *   approved by the user. Since I/O is near-to impossible to achieve outside restricted environments it also
 *   gives the user (some) certainty that every I/O operation performed in Siigna is defined at the mainline
 *   (which is publicly available and open source) and not in potentially proprietary modules.
 *   So: Please use this object for I/O in sandboxed environments.
 * </p>
 *
 * <h2>Using FileFilters</h2>
 * Before using the methods below, we need to specify which to which file-formats we can write to or read from,
 * and which extension we accept. We have made it mandatory to use those filters for write operations to maintain
 * complete control over which extensions the user chooses to write to. This is done via the
 * [[javax.swing.filechooser.FileNameExtensionFilter]], which can be used like so:
 *
 * {{{
 *   val pdfFilter  = new FileNameExtensionFilter("PDF files, "pdf")
 *   val textFilter = new FileNameExtensionFilter("Text files", "txt", "nfo", "npp")
 * }}}
 *
 * <b>Note:</b> The FileFilters are mandatory for write operations, but can also be used to read-operations.
 *
 * <h2>Reading to a file</h2>
 * <h4>Read a small text file</h4>
 * If you have a small text-file you can easily read it into a string like so:
 * {{{
 *   Dialogue.readText(textFilter) match {
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
 * All the <code>write*</code> methods returns a boolean which is true on success and false on failure. For all write
 * methods at least one FileFilter is mandatory (see above).
 * <h4>Writing a string</h4>
 * To write a string you simply call the writeText method:
 * {{{
 *   Dialogue.writeText("Hej Verden!", Seq(textFilter))
 * }}}
 * <h4>Writing lines</h4>
 * If you wish to write a number of lines you are probably looking for the writeLines method:
 * {{{
 *   val lines = Seq("Hej", "Verden", "!")
 *   Dialogue.writeLines(lines, Seq(textFilter))
 * }}}
 * <h4>Writing byte arrays</h4>
 * To write byte arrays that is convenient enough to be kept in memory you can use the writeBytes method
 * {{{
 *   val bytes = Array[Byte](...)
 *   Dialogue.writeBytes(byte, Seq(pdfFilter))
 * }}}
 * <h4>Writing to a channel or output stream</h4>
 * If you have special needs or if your data is just too large to be handled in strings or byte arrays you can
 * use the <code>writeChannel</code> or <code>writeOutputStream</code>. These methods takes a callback-method as
 * a parameter in which you can write to a WritableByteChannel or OutputStream. This will allow you to pack
 * much more data into a file if you wish.
 *
 * Using the <code>writeOutputStream</code> could look like this:
 * {{{
 *   // First define the output mapping
 *   val writePdf  = (out : OutputStream) => {
 *     val bytes = Array[Byte](...)
 *     out.write(bytes)
 *   }
 *   // Link the file-filter(s) with the output function(s)
 *   val exporters = Map(pdfFilter -> writePdf)
 *
 *   // Call the dialogue with the exporters
 *   Dialogue.writeOutputStream(exporters)
 * }}}
 *
 * Using the <code>writeChannel</code> could look like this:
 * {{{
 *   // First define the output mapping
 *   val writePdfChannel = (channel : WritableOutputChannel) => {
 *     val buffer = ByteBuffer.allocate(2048) // First allocate some space to a byte buffer
 *     buffer.putString("Hi there")           // Then feed in data.
 *     ...
 *     channel.write(buffer)                  // Write the buffer to the file
 *     // Repeat if you wish
 *   }
 *
 *   // Link the file-filter(s) with one or more output-function
 *   val exporters = Map(pdfFilter -> writePdfChannel)
 *
 *   // Call the dialogue with the exporters
 *   Dialogue.writeChannel(exporters)
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

  /**
   * Opens a JFileChooser for reading operations and lets the user choose a file to read. If the dialogue is not
   * interrupted and the file matches the given file extensions (if any), the function f is called and the value
   * Some[T] is returned.
   * @param f  The function to perform on the file to retrieve the data.
   * @param filters  The extension-name filter of the files to read from. Can be empty.
   * @tparam T  The return type of the method
   * @return  Some[T] if the user correctly selected a file that matched the filter (if any). If the file did
   *          not match, the user pressed cancel or an error occurred we return None.
   * @throws IOException  If an I/O error occurred when trying to read/write
   * @throws IllegalArgumentException  If no parsers are given, i. e. <code>parsers</code> is empty.
   */
  protected def openDialogueRead[T](f : File => T, filters : Traversable[FileNameExtensionFilter]) : Option[T] = {
    openDialogue[T](DialogueFunctionRead(f, filters))
  }

  /**
   * Opens a JFileChooser for writing operations and lets the user choose a file to write to. If the dialogue is not
   * interrupted and the file matches the given file extensions (if any), the function f is called and the value
   * Some[T] is returned.
   * @param parsers  The extension-name filter of the files to write to mapped to a function that does
   *                 something with that file. Cannot be empty.
   * @tparam T  The type of the data to retrieve.
   * @return  Some[T] if the user correctly selected a file that matched the filter (if any). If the file did
   *          not match, the user pressed cancel or an error occurred we return None.
   * @throws IOException  If an I/O error occurred when trying to read/write
   * @throws IllegalArgumentException  If no parsers are given, i. e. <code>parsers</code> is empty.
   */
  protected def openDialogueWrite[T](parsers : Map[FileNameExtensionFilter, FileChannel => T], options : Set[OpenOption]) : Option[T] = {
    // Make sure we have at least one file filter
    require(!parsers.isEmpty, "Needs at least one parser to operate on, none were given.")
    openDialogue[T](
      DialogueFunctionWrite(parsers,
        if (options.isEmpty) Set(StandardOpenOption.TRUNCATE_EXISTING) else options
      )
    )
  }

  /**
   * Opens a dialogue with a given dialogue function.
   * @param function The function to perform on the File.
   * @tparam T  The type of data to return.
   * @return Some[T] if the data was successfully returned and parsed to type T, None otherwise.
   */
  protected def openDialogue[T](function : DialogueFunction) : Option[T] = {
    // Continue to open the dialogue
    val result = IOActor !? function
    result match {
      case i : InterruptedException => None
      case e => {
        try {
          Some(e.asInstanceOf[T])
        } catch {
          case _ : Throwable => None
        }
      }
    }
  }

  /**
   * Attempts to read a file as an array of bytes. This is useful for small files that can fit in memory.
   * This can be efficiently coupled with the [[com.siigna.util.io.Unmarshal]] object of Siigna to read the item
   * from the retrieved byte-array. See above for examples.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  Some[Array[Byte] ] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readBytes(filters : FileNameExtensionFilter*) : Option[Array[Byte]] =
    openDialogueRead(f => Files.readAllBytes(f.toPath), filters)

  /**
   * Attempts to read a file as a byte channel. This is useful when dealing with large binary files that can be parsed
   * to chunks. See above for examples.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  Some[ReadableByteChannel] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readByteChannel(filters : FileNameExtensionFilter*) : Option[ReadableByteChannel] =
    openDialogueRead(f => Files.newByteChannel(f.toPath, util.EnumSet.of(StandardOpenOption.READ)), filters)

  /**
   * Attempts to read an image file.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  Some[java.awt.Image] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readImage(filters : FileNameExtensionFilter*) : Option[java.awt.Image] =
    openDialogueRead(f => Toolkit.getDefaultToolkit.getImage(f.toString), filters)

  /**
   * Attempts get the input channel of a file. This is useful when dealing with larger binary or textual
   * files. See above for examples.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  Some[InputStream] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readInputStream(filters : FileNameExtensionFilter*) : Option[InputStream] = {
    openDialogueRead(file => Files.newInputStream(file.toPath, StandardOpenOption.READ), filters)
  }

  /**
   * Attempts to read a file to a number of lines. This is useful when dealing with larger textual files which
   * might be too big to be efficiently handled in one take. See above for examples.
   * @param encoding  An optional parameter to specify the encoding of the read-operation. Defaults to UTF-8.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  Some[Iterator[String] ] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readLines(encoding : String = "UTF-8", filters : Seq[FileNameExtensionFilter]) : Option[Iterator[String]] =
    readSource(encoding, filters).map(_.getLines())

  /**
   * Attempts to read a file to a scala [[scala.io.Source]]. This is useful if you want an iterable representation
   * of the data and more options for reading a file then we are able to provide here.
   * @param encoding  An optional parameter to specify the encoding of the read-operation. Defaults to UTF-8.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  Some[Source] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readSource(encoding : String = "UTF-8", filters : Seq[FileNameExtensionFilter] = Nil) : Option[Source] =
    openDialogueRead(file => Source.fromFile(file, encoding), filters)

  /**
   * Attempts to read a file into a string. This is useful when dealing with small textual files that can be
   * handled in memory. See above for examples.
   * @param encoding  An optional parameter to specify the encoding of the read-operation. Defaults to UTF-8.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  Some[String] if the user correctly selected a file and we have sufficient permissions to read it
   *          None otherwise.
   */
  def readText(encoding : String = "UTF-8", filters : Seq[FileNameExtensionFilter]) : Option[String] =
    readSource(encoding, filters).map(_.getLines().mkString("\n"))

  /**
   * Writes the given byte array to a file the user chooses. This can be efficiently coupled with the
   * [[com.siigna.util.io.Marshal]] object of Siigna to cast the item you want to store into a byte-array.
   *
   * The method allows you to specify the method with which to open the file ([[java.nio.file.StandardOpenOption]])
   * which is set to truncate all the existing content before reading, as default. If you wish to change that
   * behaviour, you should set the parameter.
   * @param bytes  The bytes to write to file.
   * @param options  Specifies how the bytes are written. Defaults to StandardOpenOption.TRUNCATE_EXISTING which
   *                 truncates all the content of the file away before writing.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeBytes(bytes : Array[Byte], filters : Seq[FileNameExtensionFilter],
                 options : StandardOpenOption*) : Boolean = {
    openDialogueWrite(filters.map(t =>
      t -> ((channel : FileChannel) => channel.write(ByteBuffer.wrap(bytes)))).toMap, options.toSet).isDefined
  }

  /**
   * Provides a [[java.nio.channels.WritableByteChannel]] that can be used to write any content to disc. The
   * parameter callback is a callback function that will be called with a byte channel when available.
   * @param extensions  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away
   *                   unwanted files, or help the user choose a file with a certain file-ending, for instance,
   *                   paired with the functions that takes a byte channel that can be used to store any number of
   *                   data into the file the user have chosen.
   * @param options  Zero or more options with which to open the file. Defaults to StandardOpenOption.TRUNCATE_EXISTING
   *                 which truncates all the content of the file away before writing.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeChannel(extensions : Map[FileNameExtensionFilter, FileChannel => Unit],
                   options : OpenOption*) : Boolean = {
    openDialogueWrite(extensions, options.toSet).isDefined
  }

  /**
   * Writes the given lines to a file the user chooses.
   *
   * The method allows you to specify the method with which to open the file ([[java.nio.file.StandardOpenOption]])
   * which is set to truncate all the existing content before reading, as default. If you wish to change that
   * behaviour, you should set the parameter.
   *
   * @param lines  The lines to write to the file chosen by the user.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @param encoding  The encoding with which to write the string. Defaults to "UTF-8".
   *                choose a file with a certain file-ending, for instance.
   * @param options  Specifies how the bytes are written. Defaults to StandardOpenOption.TRUNCATE_EXISTING which
   *                truncates all the content of the file away before writing.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeLines(lines : Iterable[String], filters : Seq[FileNameExtensionFilter] = Nil,
                 encoding : String = "UTF-8", options : Set[OpenOption] = Set()) : Boolean = {
    openDialogueWrite(filters.map(t => t -> ((channel : FileChannel) => {
      val bytes = ByteBuffer.wrap(lines.mkString("\n").getBytes(Charset.forName(encoding)))
      channel.write(bytes)
    })).toMap, options.toSet).isDefined
  }

  /**
   * Provides a [[java.io.OutputStream]] that can be used to write any content to disc. The
   * parameter callback is a callback function that will be called with a output stream, when available.
   * @param extensions  A seq of optional [[javax.swing.filechooser.FileNameExtensionFilter]]s that can filter away
   *                   unwanted files, or help the user choose a file with a certain file-ending, for instance,
   *                   mapped with functions to export
   * @param options  The option with which to open the file. Defaults to StandardOpenOption.TRUNCATE_EXISTING
   *                 which truncates all the content of the file away before writing..
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeOutputStream(extensions : Map[FileNameExtensionFilter, OutputStream => Unit],
                        options : OpenOption*) : Boolean = {
    openDialogueWrite(extensions.map(t => t._1 -> ((channel : FileChannel) => {
      val bytes = new ByteArrayOutputStream()
      t._2(bytes)
      channel.write(ByteBuffer.wrap(bytes.toByteArray))
    })).toMap, options.toSet).isDefined
  }

  /**
   * Writes the given lines to a file the user chooses.
   *
   * The method allows you to specify the method with which to open the file ([[java.nio.file.StandardOpenOption]])
   * which is set to truncate all the existing content before reading, as default. If you wish to change that
   * behaviour, you should set the parameter.
   *
   * @param text  The text write to the file chosen by the user.
   * @param filters  A number of [[javax.swing.filechooser.FileNameExtensionFilter]]s that provides the user with a
   *                 file-extension, filters away unwanted files, and helps the user choose a file with a
   *                 certain file-ending, for instance.
   * @param encoding  The encoding with which to write the string. Defaults to "UTF-8".
   * @param options  Specifies how the bytes are written. Defaults to StandardOpenOption.TRUNCATE_EXISTING which
   *                truncates all the content of the file away before writing.
   * @return  True if the data was successfully written to the file, false if an error occurred.
   */
  def writeText(text : String, filters : Seq[FileNameExtensionFilter] = Nil, encoding : String = "UTF-8",
                options : Set[OpenOption] = Set()) : Boolean = {
    writeLines(Seq(text), filters, encoding, options)
  }

}