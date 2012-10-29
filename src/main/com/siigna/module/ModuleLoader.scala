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

package com.siigna.module

import actors.Futures._
import com.siigna.util.logging.Log
import java.util.jar.{JarEntry, JarFile}
import scala.Some
import com.siigna.util.event.End
import com.siigna.app.controller.Controller
import actors.Future
import java.net.JarURLConnection

/**
 * A ClassLoader for [[com.siigna.module.ModulePackage]]s and [[com.siigna.module.ModuleInstance]]s.
 * This class loader loads and caches modules in Siigna.
 */
object ModuleLoader extends ClassLoader(Controller.getClass.getClassLoader) {

  // Load the default modules
  val base = ModulePackage('base, "c:/workspace/siigna/main/out/artifacts/", "base.jar", true)

  /**
   * Cached packages and their jar files
   */
  val _packages = collection.mutable.Map[ModulePackage, JarFile]()

  /**
   * A dummy module to use if the loading fails.
   */
  protected val dummyModule : Module = new Module {
    val stateMap : StateMap = Map('Start -> { case _ => End })
  }

  /**
   * Attempt to cast a class to a [[com.siigna.module.Module]].
   * @param clazz  The class to cast
   * @return  A Module (hopefully), otherwise probably a nasty exception
   */
  protected def classToModule(clazz : Class[_]) = clazz.newInstance().asInstanceOf[Module]

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
   * Attempts to load a module from the information in the given [[com.siigna.module.ModuleInstance]].
   * <p><b>This method runs synchronously.</b></p>
   *
   * @param entry  The name of the class to load
   * @return  The module to load
   * @throws NoSuchElementException  If no module could be loaded from the given resource
   */
  def load(entry : String, pack : ModulePackage) : Module = {
    // Failure means that we have to try to fetch it from the jar
    try {
      // If the package has not been loaded, we need to do so
      val jar = if (!_packages.contains(pack)) {
        load(pack)
      } else _packages(pack)

      var module : Option[Module] = None

      opOnJarEntries(jar, zip => {
        if (!zip.isDirectory && zip.getName.contains(entry + ".class")) {
          module = loadModuleFrom(defineClass(jar, zip))
        }
      })

      module match {
        case Some(m) => m // Success
        case None    => { // Failed! Log the error and return a simple dummy module
          Log.error("ModuleLoader: Could not find module and load " + entry + " in package " + pack)
          dummyModule
        }
      }
    } catch {
      case e : Exception => {
        Log.error("ModuleLoader: Error when loading module " + entry, e)
        dummyModule
      }
    }
  }

  /**
   * <p>Loads all the resources in the given package by loading all the module-classes in the [[java.util.jar.JarFile]]
   * of the package into the system.</p>
   *
   * <p><b>Parts of this method runs synchronously.</b></p>
   *
   * @param pack  The package to load
   * @throws IOException  If an error occurred while downloading the .jar
   */
  def load(pack : ModulePackage) = {
    if (!_packages.contains(pack)) {
      // Force-load the jar file if it hasn't already been downloaded
      val jar = pack.toURL.openConnection().asInstanceOf[JarURLConnection].getJarFile

      // Save the package
      _packages += pack -> jar

      // Load the classes inside
      opOnJarEntries(jar, entry => {
        if (!entry.isDirectory) {
          // Load the class into the class loader
          defineClass(jar, entry)
        }
      })

      Log.success("ModuleLoader: Sucessfully stored the module package " + pack)

      // Return the jar
      jar
    } else {
      _packages(pack)
    }
  }

  /**
   * Attempt to load and instantiate a new module from a given archetype class.
   * @param clazz  The class-information to create a module from
   * @return  Some[Module] if the module could be instantiated and cast, None otherwise
   */
  protected def loadModuleFrom(clazz : Class[_]) : Option[Module] = {
    try {
      Some(classToModule(clazz))
    } catch {
      case e : InstantiationException => {
        // If constructing via the class, try to force the constructor public.
        try {
          val constructors = clazz.getDeclaredConstructors
          constructors(0).setAccessible(true)
          Some(constructors(0).newInstance().asInstanceOf[Module])
        } catch {
          case e : Exception => Log.warning("ModuleLoader: Class " + clazz.getName + " found but failed to instantiate.", e)
          None
        }
      }
      case e => {
        Log.warning("ModuleLoader: Class " + clazz.getName + "found, but failed to cast to Module.", e)
        None
      }
    }
  }

  /**
   * A list of the loaded packages.
   * @return  An Iterable[ModulePackage] containing their respective modules.
   */
  def packages = _packages

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
   * Unloads a [[com.siigna.module.ModulePackage]] so all modules created in the future cannot derive from this
   * package.
   * @param pack  The package to unload.
   */
  def unload(pack : ModulePackage) {
    _packages.-=(pack)
  }

}