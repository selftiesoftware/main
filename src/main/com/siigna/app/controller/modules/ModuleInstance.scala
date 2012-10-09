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

package com.siigna.app.controller.modules

/**
 * <p>A ModuleInstance is an entry in a [[com.siigna.app.controller.modules.ModulePackage]].
 * This class is made to make sure that we can identify different Modules with same name from each other. A ModuleInstance
 * allows us to plug several modules from different packages into the same running instance of Siigna.</p>
 *
 * <p>To provide the right information make sure the class path is given as the exact folder-location in the .jar file,
 * nothing else. Similarly the class name should be the name of the class. So if I were to load a class with the full
 * name of ''com.siigna.module.base.Default'' the class path would be ''com.siigna.module.base'' and the
 * class name would be ''Default''.</p>
 *
 * @param pack  The [[com.siigna.app.controller.modules.ModulePackage]] in which the module lies
 * @param classPath  The class path to the module (e. g. ''com.siigna.module.base'')
 * @param className  The name of the class (e. g. ''Default'' - without .class)
 */
case class ModuleInstance(pack : ModulePackage, classPath : String, className : String) {

  /**
   * Gets the full class path for the module.
   * @return  The class path concatenated with the class name with a "."
   */
  override def toString = classPath + "." + className

}
