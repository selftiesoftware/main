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

import actors.Futures._

import com.siigna.app.controller.Controller
import com.siigna.util.logging.Log
import java.util.jar.{JarFile, JarEntry}
import com.siigna.module._

/**
 * A ClassLoader for [[com.siigna.module.ModulePackage]]s and
 * [[com.siigna.module.ModuleInstance]]s. This class loader is meant to
 */
object ModuleClassLoader extends ClassLoader(Controller.getClass.getClassLoader) {

  protected val modules = collection.mutable.Map[Symbol, Module]()

  /**
   * Execute a function on each [[java.util.jar.JarEntry]] elements in the jar file
   * @param file  The jar file to read the entries from
   * @param f  The function to apply on the entries
   */
  protected def opOnJarEntries(file : JarFile, f : JarEntry => Unit) {
    val entries = file.entries
    while (entries.hasMoreElements) { f(entries.nextElement) }
  }

  /**
   * Fetches the bytes associated with a [[java.util.jar.JarEntry]] in a [[java.util.jar.JarFile]] and
   * attempts to define it as a class in the class loader.
   * @param file  The JarFile that contains the entry
   * @param entry  The JarEntry to read
   * @return  The class that was defined
   */
  protected def defineClass(file : JarFile, entry : JarEntry) = {
    val input = file.getInputStream(entry)
    val bytes = Stream.continually(input.read).takeWhile(-1 !=).map(_.toByte).toArray
    defineClass(entry.getName, bytes, 0, bytes.size)
  }

  /**
   * <p>Loads all the resources in the given package by loading all the module-classes in the [[java.util.jar.JarFile]]
   * of the package into the system.</p>
   * <p><b>These operations are done asynchronously.</b></p>
   *
   * @param pack  The package to load
   * @throws IOException  If an error occurred while downloading the .jar
   */
  def load(pack : ModulePackage) {
    try {
      // Attempt to load the resources from the .jar in the background
      future {
        // Force-load the jar file if it hasn't already been downloaded
        val jar = pack.jar()

        opOnJarEntries(jar, entry => {
          if (!entry.isDirectory) {
            try {
              val clazz  = defineClass(jar, entry)
              val module = clazz.getField("MODULE$").get(manifest.erasure).asInstanceOf[Module]
              modules + (Symbol(module.toString) -> module)
            } catch {
              case e : Exception => {
                Log.error("ModuleLoader: Error when loading entry " + entry + " from file " + jar)
              }
            }
          }
        })
      }
      Log.success("ModuleClassLoader: Sucessfully loaded entire module package " + pack)
    } catch {
      case e : Exception => Log.error("ModuleClassLoader: Failed to load module pack " + e)
    }
  }

  /**
   * Attempts to load a module from the information in the given [[com.siigna.module.ModuleInstance]].
   * @see http://stackoverflow.com/questions/3039822/how-do-i-call-a-scala-object-method-using-reflection
   */
  def load[Module : Manifest](entry : ModuleInstance) : Module = {
    // Try to load the module from memory
    if (modules.contains(entry.className)) {
      modules.get(entry.className)
    } else {
      // Failure means that we have to try to fetch it from the jar
      try {
        loadSingle(entry)
      } catch {
        // Failure means that the module did not exist
        case _ => {
          Log.error("ModuleClassLoader: Module " + entry + " could not be found")
          None
        }
      }
    }
  }

}
