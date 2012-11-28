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

import java.io.InputStream

/**
 * <p>The Importer trait defined an overall interface for everyone who wants to import code. When an importer
 * is created a hook for it's <code>extension</code> is added, so, depending on the file-extension the
 * appropriate importer is given an InputStream, representing the content of a loaded file. The importer
 * should then execute the appropriate [[com.siigna.app.model.action.Action]]a to create one or more shapes,
 * resulting from the analysis of the given lines. The InputStream is automatically closed.</p>
 *
 * <p><b>Note:</b> When an instance of the <code>Importer</code> is created, a hook is automatically added to
 * the specified file-type. If the extension is already defined in the Import module, creating another
 * Importer will override the old implementation.</p>
 *
 * @see [[com.siigna.module.io.Import]]
 */
trait Importer extends (InputStream => Unit) {

  // Adds the importer on instantiation.
  var imp = new Import
  imp.addImporter(this)

  /**
   * The extension of the file-type, for example <code>dxf</code>, <code>pdf</code> or even <code>doc</code>.
   * @return  A string, normally three letters.
   */
  def extension : String

  override def toString() = "Importer for file-type " + extension

}
