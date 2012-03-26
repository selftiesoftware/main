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

package com.siigna.app.controller

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

import java.lang.ClassLoader
import java.net.{URL, URLClassLoader}
import com.siigna.util.logging.Log

/**
 * An object that loads scala class files from remote locations.
 * TODO: Split the tasks up, so the actual class-loading is done in one function etc.
 * TODO: Add a remote system for finding vars - includes setting up a server!
 * -> val url = new URL("jar:http://siigna.com/module/"+moduleName+".jar!/")
 */
class ModuleLoader(private var urls : Array[URL], loader : ClassLoader) {

  private var classLoader = URLClassLoader.newInstance(urls, loader)

  private def addURL(url : URL) {
    urls = urls.+:(url)
    classLoader = URLClassLoader.newInstance(urls, loader)
  }
  
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
   * @return Option[T] Some[T] if the module loads successfully and the resulting class is an instance of T, else None.
   */
  def loadModule[Module : Manifest](classPath : String, filePath : String) : Option[Module] = {
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

      // Get the module as an object and then a class respectively
      // Thanks to: http://stackoverflow.com/questions/3039822/how-do-i-call-a-scala-object-method-using-reflection
      val resource : Option[Class[_]] = try {
        // Load the object and not the class
        Some(classLoader.loadClass(classPath + "$"))
      } catch {
        case e => errors += ("JarLoader: No resource could be found at the given location: " + classPath -> Some(e))
        None
      }

      // Try and load the module!
      val result : Option[Module] = if (resource.isEmpty) None else try {
        val field = resource.get.getField("MODULE$")
        val instance = field.get(manifest.erasure).asInstanceOf[Module]
        // Return!
        Some(instance)
      } catch {
        case e : ExceptionInInitializerError => {
          // If constructing via the MODULE$ field fails, try using the constructor instead.
          try {
            val constructors = resource.get.getDeclaredConstructors
            constructors(0).setAccessible(true)
            Some(constructors(0).newInstance())
            None
          } catch {
            case err => errors += ("ModuleLoader: Module " + classPath + " was found but could not be initialized through MODULE$ field or constructor." -> Some(err))
            None
          }
        }
        case e => errors += ("ModuleLoader: Class found, but failed to cast to type T." -> Some(e)); None
      }

      // Print errors in case no modules was found
      if (result.isEmpty) {
        errors.foreach(e => if (e._2.isDefined) Log.error(e._1, e._2.get) else Log.error(e._1))
      }

      // Return the result
      result
    }
  }

}

