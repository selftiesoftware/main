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

import actors.Futures._
import com.siigna.module.{ModuleInstance, ModulePackage, Module}
import com.siigna.util.logging.Log
import java.util.jar.{JarEntry, JarFile}
import scala.Some
import java.io.FileNotFoundException

/**
 * A ClassLoader for [[com.siigna.module.ModulePackage]]s and [[com.siigna.module.ModuleInstance]]s.
 * This class loader loads and caches modules in Siigna.
 */
object ModuleLoader extends ClassLoader(Controller.getClass.getClassLoader) {

  /**
   * The base module package.
   */
  var base : Option[ModulePackage] = try {
    Some(ModulePackage('base, "rls.siigna.com", "base/com/siigna/siigna-module_2.9.2/preAlpha/siigna-module_2.9.2-preAlpha.jar"))
  } catch {
    case e : FileNotFoundException => {
      Log.error("ModuleLoader: Base module pack could not be found: " + e.getMessage)
      None
    }
    case e => {
      Log.error("ModuleLoader: Base module failed to load: " + e.getMessage)
      None
    }
  }

  protected val modules = collection.mutable.HashMap[Symbol, Module]()

  /**
   * Attempt to cast a class to a [[com.siigna.module.Module]].
   * @param clazz  The class to cast
   * @return  A Module (hopefully), otherwise probably a nasty exception
   */
  protected def classToModule(clazz : Class[_]) = clazz.getField("MODULE$").get(manifest.erasure).asInstanceOf[Module]

  /**
   * Fetches the bytes associated with a [[java.util.jar.JarEntry]] in a [[java.util.jar.JarFile]] and
   * attempts to define it as a class in the class loader.
   * @param file  The JarFile that contains the entry
   * @param entry  The JarEntry to read
   * @return  The class that was defined
   */
  protected def defineClass(file : JarFile, entry : JarEntry) : Class[_] = {
    val input = file.getInputStream(entry)
    val bytes = Stream.continually(input.read).takeWhile(-1 !=).map(_.toByte).toArray
    val name  = entry.getName
    val clazz = name.substring(0, name.indexOf('.')).replace('/', '.') // Replace package delimiters and remove .class
    try {
      defineClass(clazz, bytes, 0, bytes.size)
    } catch {
      case e : LinkageError => // This is caused by an attempt to duplicate classes
                               // caused by the additional "$" above. Unfortunately scala object are postfixed
                               // with a "$" so there's not really anything we can do about it...
                               // So we just return the same class.. tihiii
      loadClass(clazz)
    }
  }

  /**
   * <p>Loads all the resources in the given package by loading all the module-classes in the [[java.util.jar.JarFile]]
   * of the package into the system.</p>
   *
   * <p><b>This method runs asynchronously.</b></p>
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
            defineClass(jar, entry)
          }
        })
        Log.success("ModuleLoader: Sucessfully loaded entire module package " + pack)
      }
    } catch {
      case e : Exception => Log.error("ModuleLoader: Failed to load module pack " + e)
    }
  }

  /**
   * Attempts to load a module from the information in the given [[com.siigna.module.ModuleInstance]].
   * <p><b>This method runs synchronously.</b></p>
   *
   * @param entry  The [[com.siigna.module.ModuleInstance]] to load
   * @return  The module to load
   * @throws NoSuchElementException  If no module could be loaded from the given resource
   */
  def load(entry : ModuleInstance) : Module = {
    // Try to load the module from cache
    if (modules.contains(entry.className)) {
      modules.apply(entry.className)
    } else {
      // Try to fetch it from the class loader with the right name
      try {
        classToModule(loadClass(entry.toString + "$"))
      } catch {
        case _ : Exception => {
          // Failure means that we have to try to fetch it from the jar
          // Force-load the jar file if it hasn't already been downloaded
          val jar = entry.pack.jar()
          var module : Option[Module] = None

          opOnJarEntries(jar, zip => {
            if (!zip.isDirectory && zip.getName.contains(entry.className.name + "$.class")) {
              module = loadModuleFromJar(jar, zip)
            }
          })

          module match {
            case Some(m) => m
            case None    => throw new NoSuchElementException("ModuleLoader: Could not find module " + entry + " in package " + entry.pack)
          }
        }
      }
    }
  }

  /**
   * Loads a module from a jar file
   * @param jar  The jar file to load the entry from
   * @param entry  The entry in the .jar file representing the module
   * @return  Some[Module] if the module was found, None otherwise
   * @see http://stackoverflow.com/questions/3039822/how-do-i-call-a-scala-object-method-using-reflection
   */
  protected def loadModuleFromJar(jar : JarFile, entry : JarEntry) : Option[Module] = {
    try {
      val clazz : Class[_] = defineClass(jar, entry)

      val module : Option[Module] = try {
        Some(classToModule(clazz))
      } catch {
        case e : ExceptionInInitializerError => {
          // If constructing via the MODULE$ field fails, try using the constructor instead.
          try {
            val constructors = clazz.getDeclaredConstructors
            constructors(0).setAccessible(true)
            Some(constructors(0).newInstance().asInstanceOf[Module])
          } catch {
            case e : Exception => Log.error("ModuleLoader: Module " + entry.toString + " was found but could not be initialized through MODULE$ field or constructor.", e)
            None
          }
        }
        case e => Log.error("ModuleLoader: Class found, but failed to cast to Module.", e); None
      }

      // Add the module to the cache
      if (module.isDefined) {
        modules += Symbol(module.get.toString) -> module.get
      }

      module
    } catch {
      case e : Exception => {
        Log.error("ModuleLoader: Unknown error when loading module entry " + entry + " from file " + jar)
        None
      }
    }
  }

  /**
   * Execute a function on each [[java.util.jar.JarEntry]] elements in the jar file
   * @param file  The jar file to read the entries from
   * @param f  The function to apply on the entries
   */
  protected def opOnJarEntries(file : JarFile, f : JarEntry => Unit) {
    val entries = file.entries
    while (entries.hasMoreElements) { f(entries.nextElement) }
  }

}