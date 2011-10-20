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

package com.siigna.app.controller

import java.net.URL

import com.siigna.app.controller.remote._
import com.siigna.module._
import com.siigna.util.logging.Log
import collection.mutable.Stack

/**
 * A bank for modules. An instance of the class is stored in the Controller, so the (pre)loading is done in the
 * Controller-thread. Access to this class should be achieved through Commands to the Controller.
 */
class ModuleBank {

  /**
   * The stored modules.
   */
  private val modules = scala.collection.mutable.Map[Symbol, Module]()

  /**
   * The class loader used to load classes.
   */
  private val classLoader = new ScalaClassLoader(Array(new URL("jar:file:/C:/workspace/siigna/siigna-modules/out/artifacts/endogenous/endogenous.jar!/"),
	  new URL("jar:file:/E:/OLE/06_Siigna/06_programmering/06_endogenous/out/artifacts/endogenous.jar!/"),
	  new URL("jar:file:/C:/siigna/endogenous/out/artifacts/endogenous.jar!/")))

  /**
   * Examines whether the bank contains a given module.
   */
  def contains(module : Symbol) = modules contains module

  /**
   * Loads a given class and returns it.
   *
   * @param name  The symbolic representation, used to store and retrieve the class from the ModuleBank
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
        Log.warning("ModuleBank: We found your class but please preload your modules in the future to prevent sudden errors.")
        val module = modules(name)
        Some(module)
      } else {
        None
      }
    }

  /**
   * Preloads a module and store it in the ModuleBank. Please use this function before you forward to a module, to
   * be sure to avoid annoying errors.
   * <br />
   * This function differs from the <code>load</code>-method in that it returns
   * a boolean flag instead of an Option. It's just more pretty.
   *
   * @param name  The symbolic representation, used to store and retrieve the class from the ModuleBank.
   * @param classPath  The path to the class.
   * @param filePath  The path to the file.
   */
  def preload(name : Symbol, classPath : String, filePath : String) : Boolean = {
    if (modules.contains(name)) {
      Log.success("ModuleBank: Successfully preloaded class "+name+".")
      true
    } else {
      // Try to load the module from the JarLoader
      val loadedClass = if (classPath == "com.siigna.module.endogenous")
        classLoader.loadModule(classPath + "." + name.name, filePath)
      else if (classPath.eq(""))
        classLoader.loadModule("com.siigna.module.endogenous." + name.name, filePath)
      else classLoader.loadModule(classPath, filePath)

      // Save the class if the class loader succeeds
      if (loadedClass.isDefined) {
        Log.success("ModuleBank: Successfully preloaded class "+name+".")
        modules += (name -> toModule(loadedClass.get))
        true
      } else {
        Log.warning("ModuleBank: Failed to preload class: ", name, classPath, filePath)
        false
      }
    }
  }

  /**
   * Creates a new instance of a module.
   */
  private def toModule(module : Module) = module.getClass.newInstance().asInstanceOf[Module]

}
