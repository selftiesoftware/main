/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.controller.remote

import java.lang.ClassLoader
import java.io.{File}
import java.net.{JarURLConnection, URL, URLClassLoader}
import java.lang.ClassLoader

import com.siigna.module.Module
import com.siigna.util.logging.Log

/**
 * An object that loads scala class files from remote locations.
 * TODO: Split the tasks up, so the actual class-loading is done in one function etc.
 * TODO: Add a remote system for finding vars - includes setting up a server!
 * -> val url = new URL("jar:http://siigna.com/module/"+moduleName+".jar!/")
 */
class ScalaClassLoader(urls : Array[URL]) extends URLClassLoader(urls) {

  /**
   * Load a module class with the given name from a given location.
   * <b>Note:</b> Siigna tries to locate the class from specific paths in the file. Ordered, they are:
   *  <ul>
   *    <li>com.siigna.module ("com.siigna.module.<moduleName>." exact)</li>
   *    <li>module ("module.<moduleName>." exact)</li>
   *    <li>name of the module ("<moduleName>." exact) and</li>
   *    <li>root ("" exact)</li>
   *  </ul>
   * If you wish to add your own path.
   *
   * @param classPath  the path to the module to load.
   * @param filePath  the path to the module as file or URL.
   *
   * @return Option[Module] Some[Module] if the module loads successfully and the resulting class is an instance of Module, else None.
   */
  def loadModule(classPath : String, filePath : String) : Option[Module] = {
    if (classPath == "") {
      Log.warning("JarLoader: Cannot load modules with empty classPath.")
      None
    } else {
	    // Defines a list of errors in case no result will be found
	    var errors = Map[String, Option[Throwable]]();

      // Use the given url if defined
      if (filePath != null && filePath != "") {
        try {
          addURL(new URL(filePath))
        } catch {
          case e => {
            errors += ("JarLoader: The given path is malformed and was not included: " + filePath -> Some(e))
          }
        }
      }
      
      // Tries to load the class from a series of different locations.
      var result : Option[Module] = None
      
      // If the result hasn't been found yet, continue the loop
      try {
        // Save the class
        val loadedClass = loadClass(classPath)
        
        // Try and load it!
        result = Some(loadedClass.newInstance().asInstanceOf[Module])
      } catch {
        case e => errors += ("JarLoader: No resource could be found at the given location: " + classPath -> Some(e))
      }
      
      // Print errors in case no modules was found
      if (result.isEmpty) {
        errors.foreach(e => if (e._2.isDefined) Log.error(e._1, e._2) else Log.error(e._1))
      }
      
      // Return the result
      result
    }
  }

}
