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

import com.siigna.app.controller.Controller
import com.siigna.util.logging.Log
import java.util.jar.{JarFile, JarEntry}

/**
 * A ClassLoader for modules
 */
class ModuleClassLoader(classLoader : ClassLoader = Controller.getClass.getClassLoader) extends ClassLoader(classLoader) {

  /**
   * Attempts to define a class from a given ModuleEntry
   * @param module  The ModuleEntry
   * @return  Some(Class[_]) if it could be found, None otherwise
   */
  protected def defineClass(module : ModuleEntry) : Option[Class[_]] = {
    executeOnEntries(module.pack.jarFile, entry => {
      if (entry.getName.equals(module.getFullClassName)) {
        return Some(defineClass(module.pack.jarFile, entry))
      }
    })
    None
  }

  /**
   * Execute a function on each [[java.util.jar.JarEntry]] elements in the jar file
   * @param file  The jar file to read the entries from
   * @param f  The function to apply on the entries
   */
  protected def executeOnEntries(file : JarFile, f : JarEntry => Unit) {
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
   * Initialize the given package by attempting to load all the classes into the system.
   * These operations are done asynchronously.
   * @param pack  The package to add.
   */
  def initPackage(pack : ModulePackage) {
    try {
      // Attempt to load the resources from the .jar in the background
      new Thread {
        override def run() {
          executeOnEntries(pack.jarFile, entry => {
            if (!entry.isDirectory) {
              defineClass(pack.jarFile, entry)
            }
          })
        }
      }.start()
    } catch {
      case e => Log.error("ModuleClassLoader: Failed to load module pack " + e)
    }
  }

  /**
   * Attempts to load a module from the information in the given [[com.siigna.app.controller.modules.ModuleEntry]].
   * @see http://stackoverflow.com/questions/3039822/how-do-i-call-a-scala-object-method-using-reflection
   */
  def loadModuleEntry(entry : ModuleEntry) : Option[Class[_]] = {
    // Try to load the module from memory
    try {
      Some(loadClass(entry.classPath + "." + entry.className))
    } catch {
      case _ => {
        // Failure means that we have to try to fetch it from the jar
        try {
          defineClass(entry)
        } catch {
          case _ => None
        }
      }
    }
  }

}
