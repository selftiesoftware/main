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

import java.net.URL

import com.siigna.module._
import com.siigna.util.logging.Log

/**
 * A bank for modules. An instance of the class is stored in the Controller, so the (pre)loading is done in the
 * Controller-thread. Access to this class should be achieved through Commands to the Controller.
 */
object ModuleLoader extends ClassLoader {


  /**
   * The base module package.
   */
  var base : ModulePackage = ModulePackage('base, "siigna.com", "applet/base.jar")

  /**
   * The stored modules.
   */
  protected val modules = scala.collection.mutable.Map[Symbol, Module]()

  /**
   * The base url
   */
  var baseURL = new URL("jar:http://siigna.com/applet/base.jar!/")

  /**
   * The class loader used to load classes.
   */
  protected val classLoader = ModuleClassLoader

  /**
   * Examines whether the bank contains a given module.
   */
  def contains(module : Symbol) = modules contains module

  /**
   * Loads a given class and returns it.
   *
   * @param name  The symbolic representation, used to store and retrieve the class from the ModuleLoader
   * @param classPath  The path to the class.
   * @param filePath  The path to the file.
   *
   * TODO: How do we differ between endogenous and exogenous modules?
   */
  def load(name : Symbol, classPath : String = "", filePath : String = "") : Option[Module] =
    if (modules.contains(name)) {
      val module = modules(name)
      Some(module)
    } else {
      // Try to preload the module
      preload(name, classPath, filePath)

      // Try again
      if (modules.contains(name)) {
        Log.warning("ModuleLoader: We found your class but please preload your modules in the future to prevent sudden errors.")
        val module = modules(name)
        Some(module)
      } else {
        None
      }
    }

  /**
   * Preloads a module and store it in the ModuleLoader. Please use this function before you forward to a module, to
   * be sure to avoid annoying errors.
   * <br />
   * This function differs from the <code>load</code>-method in that it returns
   * a boolean flag instead of an Option. It's just more pretty.
   *
   * @param name  The symbolic representation, used to store and retrieve the class from the ModuleLoader.
   * @param classPath  The path to the class.
   * @param filePath  The path to the file.
   */
  def preload(name : Symbol, classPath : String, filePath : String) : Boolean = {
    if (modules.contains(name)) {
      Log.success("ModuleLoader: Successfully preloaded class "+name+".")
      true
    } else {
      // Try to load the module from the JarLoader
      val loadedClass = classLoader.loadModule(classPath + "." + name.name, filePath)

      // Save the class if the class loader succeeds
      if (loadedClass.isDefined) {
        Log.success("ModuleLoader: Successfully preloaded class "+name+".")
        modules += (name -> loadedClass.get)
        true
      } else {
        Log.warning("ModuleLoader: Failed to preload class: ", name, classPath, filePath)
        false
      }
    }
  }
}
