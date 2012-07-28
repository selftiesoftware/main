/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.module.io

import java.io.OutputStream

/**
  * <p>An Exporter is capable of exporting a specific file-type to a file. When an Exporter is created
  * a hook for it's <code>extension</code> is added, so, depending on the file-extension the
  * appropriate exporter is given an OutputStream, representing a blank file to be written. The exporter
  * should then write the appropriate data to the OutputStream. The stream is automatically flushed and closed.</p>
  *
  * <p><b>Note:</b> When an instance of the <code>Exporter</code> is created, a hook is automatically added to
  * the specified file-type. If the extension is already defined in the Export module, creating another
  * Exporter will override the old implementation.</p>
 *
 * @see [[com.siigna.module.io.Export]]
 */
trait Exporter extends (OutputStream => Unit) {

  // Adds the exporter on instantiation.
  Export.addExporter(this)

  /**
   * The extension of the file-type, for example <code>dxf</code>, <code>pdf</code> or even <code>doc</code>.
   * @return  A string, normally three letters.
   */
  def extension : String

}
