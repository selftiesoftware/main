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

import com.siigna.util.logging.Log
import com.siigna.util.event.End
import com.siigna.app.controller.Controller
import java.io.FileNotFoundException
import java.net.URLClassLoader

/**
 * A ClassLoader for [[com.siigna.module.ModulePackage]]s and [[com.siigna.module.ModuleInstance]]s.
 * This class loader loads and caches modules in Siigna.
 * @todo implement caching on a per package basis
 */
object ModuleLoader {

  // The underlying class loader
  protected var loader = new URLClassLoader(Array(), Controller.getClass.getClassLoader)

  /**
   * The loaded classes.
   */
  protected lazy val modules = collection.mutable.Map[ModulePackage, collection.mutable.Map[Symbol, Class[_ <: Module]]]()

  /**
   * A dummy module to use if the loading fails.
   */
  val DummyModule : Module = new Module {
    val stateMap : StateMap = Map('Start -> { case _ => End })
  }

  // Create a default packages
  //load(ModulePackage('base, "rls.siigna.com/com/siigna/siigna-base_2.9.2/nightly", "siigna-base_2.9.2-nightly.jar", false))
  //
  // load(ModulePackage('cad, "rls.siigna.com/com/siigna/siigna-cad-suite_2.9.2/nightly", "siigna-cad-suite_2.9.2-nightly.jar", false))
  //load(ModulePackage('base, "c:/workspace/siigna/main/out/artifacts", "base.jar", true))
  //load(ModulePackage('cad, "c:/workspace/siigna/main/out/artifacts", "cad-suite.jar", true))

  // ****** OLE DESKTOP ******

  load(ModulePackage('base, "c:/siigna/main/out/artifacts", "base.jar", true))
  load(ModulePackage('cad, "c:/siigna/main/out/artifacts", "cad_suite.jar", true))
  //load(ModulePackage('base, "c:/siigna/siigna-modules/out/artifacts", "base.jar", true))
  //load(ModulePackage('cad, "c:/siigna/siigna-modules/out/artifacts", "cad-suite.jar", true))
  //load(ModulePackage('porter, "c:/siigna/siigna-modules/out/artifacts", "porter.jar", true))
  //load(ModulePackage('base, "/home/jens/workspace/siigna/main/project/target/artifacts", "base.jar", true))
  //load(ModulePackage('cad, "/home/jens/workspace/siigna/main/project/target/artifacts", "cad-suite.jar", true))
  //load(ModulePackage('porter, "/home/jens/workspace/siigna/main/project/target/artifacts", "porter.jar", true))

  //Niels' modules:
  //load(ModulePackage('base, "c:/siigna/main/out/artifacts", "base.jar", true))
  //load(ModulePackage('cadSuite, "c:/siigna/main/out/artifacts", "cad-suite.jar", true))
  //load(ModulePackage('porter, "c:/siigna/main/out/artifacts", "porter.jar", true))


  /**
   * Attempt to cast a class to a [[com.siigna.module.Module]].
   * @param clazz  The class to cast
   * @return  A Module (hopefully), otherwise probably a nasty error
   */
  protected def classToModule(clazz : Class[_]) = clazz.newInstance().asInstanceOf[Module]

  /**
   * Attempts to load a [[com.siigna.module.Module]] by looking through all the available packages,
   * starting with the ones loaded first. If the module could not be found we insert a dummy module
   * to provide certainty that a module will be returned.
   * @param name  The symbolic name of the module
   * @param classPath  The class path, e. g. "com.siigna.module.base"
   * @return  The module if it could be found, or a dummy module if no suitable entry existed.
   */
  def load(name : Symbol, classPath : String) : Module = {
    var module : Option[Module] = None
    val path = classPath + "." + name.name

    modules.values.find(_.contains(name)) match {
      case Some(map) => module = Some(classToModule(map(name)))
      case _ => {
        // Try to load the class from the loaded class definitions
        try {
          module = Some(classToModule(loader.loadClass(path)))
        } catch {
          case e : ClassNotFoundException => Log.debug("ModuleLoader: Class " + path + " could not be found.")
          case e : InstantiationException => Log.debug("ModuleLoader: Class " + path + " could not be converted to a Module.")
          case e : Exception => Log.debug("ModuleLoader: Error when loading module ", e)
        }
      }
    }

    module match {
      case Some(m) => m  // Gotcha!
      case _ => {        // No module was found
        Log.warning("ModuleLoader: Could not find module " + path + ", inserting dummy module instead.")
        DummyModule
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
   * @throws IOException  If something went wrong while trying to download the .jar file
   */
  def load(pack : ModulePackage) {
    if (!modules.contains(pack)) {
      try {
        val url = pack.toURL

        // Does the content exist?
        url.openConnection().connect()

        // Add package to URL base
        loader = new URLClassLoader(loader.getURLs.:+(url), Controller.getClass.getClassLoader)

        // Check for ModuleInit in that package
        try {
          val c = loader.loadClass("com.siigna.module.ModuleInit")
          val m = classToModule(c)
          Controller.initModule = new ModuleInstance('ModuleInit, m)
          Log.success("ModuleLoader: Reloaded init module.")
        } catch {
          // No module found
          case e : ClassNotFoundException => Log.info("ModuleLoader: No ModuleInit class found in package " + pack)
        }

        // Add to cache
        modules += pack -> collection.mutable.Map()

        Log.success("ModuleLoader: Sucessfully loaded the module package " + pack)
      } catch {
        case e : FileNotFoundException => Log.error("ModuleLoader: Could not find module package at URL: " + pack.toURL)
      }
    } else {
      Log.info("ModuleLoader: Unnecessary loading of package " + pack + " - already in cache.")
    }
  }

  /**
   * A list of the loaded packages.
   * @return  An Iterable[ModulePackage].
   */
  def packages = modules.keys

  /**
   * Unloads a [[com.siigna.module.ModulePackage]] so all modules created in the future cannot derive from this
   * package.
   * @param pack  The package to unload.
   */
  def unload(pack : ModulePackage) {
    modules -= pack
    loader = new URLClassLoader(loader.getURLs.filter(_ != pack.toURL), Controller.getClass.getClassLoader)
  }

}