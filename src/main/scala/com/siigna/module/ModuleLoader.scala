/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.module

import java.io.FileNotFoundException
import java.net.URLClassLoader
import com.siigna.app.Siigna
import com.siigna.util.Log

/**
 * A ClassLoader for [[com.siigna.module.ModulePackage]]s and [[com.siigna.module.Module]]s.
 * This class loader loads and caches modules in Siigna.
 * @todo implement caching on a per package basis
 */
object ModuleLoader {

  // The private init module.. ssshhh
  private var _initModule : Option[Module] = None

  /**
   * The init [[com.siigna.module.Module]] that we're sending events to.
   * Call <code>initModule_=()</code> if you want to change the behavior.
   * @see [[com.siigna.module.Module]]
   */
  def initModule : Option[Module] = _initModule

  // The underlying class loader
  protected var loader = new URLClassLoader(Array(), this.getClass.getClassLoader)

  /**
   * The loaded classes ordered in the name of the module packages and a map of class paths and classes (modules).
   */
  protected lazy val modules = collection.mutable.Map[Symbol, collection.mutable.Map[String, Class[Module]]]()

  /**
   * The class path to siigna modules. Currently set to "com.siigna.module".
   */
  final val modulePath = "com.siigna.module"

  // Create a default packages
  load(ModulePackage('base, "rls.siigna.com/com/siigna/siigna-base_2.10/nightly", "siigna-base_2.10-nightly.jar", local = false))
  load(ModulePackage('cad, "rls.siigna.com/com/siigna/siigna-cad-suite_2.10/nightly", "siigna-cad-suite_2.10-nightly.jar", local = false))
  load(ModulePackage('porter, "rls.siigna.com/com/siigna/siigna-porter_2.10/nightly", "siigna-porter_2.10-nightly.jar", local = false))

  // ****** OLE DESKTOP ******

  //load(ModulePackage('porter, "c:/siigna/main/out/artifacts", "porter.jar", true))
  //load(ModulePackage('base, "c:/siigna/main/out/artifacts", "base.jar", true))
  //load(ModulePackage('cad, "c:/siigna/main/out/artifacts", "cad_suite.jar", true))

  //load(ModulePackage('base, "c:/workspace/siigna/main/out/artifacts", "base.jar", true))
  //load(ModulePackage('cad, "c:/workspace/siigna/main/out/artifacts", "cad-suite.jar", true))
  //load(ModulePackage('porter, "c:/workspace/siigna/main/out/artifacts", "porter.jar", true))

  //load(ModulePackage('base, "/home/jens/workspace/siigna/main/project/target/artifacts", "base.jar", true))
  //load(ModulePackage('cad, "/home/jens/workspace/siigna/main/project/target/artifacts", "cad-suite.jar", true))
  //load(ModulePackage('porter, "/home/jens/workspace/siigna/main/project/target/artifacts", "porter.jar", true))

  //Niels' modules:
  //load(ModulePackage('base, "c:/siigna/main/out/artifacts", "base.jar", true))
  //load(ModulePackage('cad, "c:/siigna/main/out/artifacts", "cad-suite.jar", true))
  //load(ModulePackage('porter, "c:/siigna/main/out/artifacts", "porter.jar", true))


  /**
   * Attempt to cast a class to a [[com.siigna.module.Module]].
   * @param clazz  The class to cast
   * @return  A Module (hopefully), otherwise probably a nasty error
   */
  protected def classToModule(clazz : Class[_]) = clazz.newInstance().asInstanceOf[Module]

  /**
   * Attempts to load a [[com.siigna.module.Module]] by looking through the given package for resources at the given
   * class path.
   * @param packageName The symbolic name of the package in which the module belongs, e. g. 'base.
   * @param classPath  The class path, e. g. "Menu".
   * @return  Some[Module] if a module could be found, None otherwise
   */
  def load(packageName : Symbol, classPath : String) : Option[Module] = {
    modules.get(packageName) match {
      case Some(p) => {
        // Retrieve the module class
        val clazz = p.get(classPath) match {
          // Return the class
          case s : Some[Class[Module]] => s

          // Search for the class in the package
          case _ => try {
            val path = modulePath + "." + packageName.name + "." + classPath
            val c = loader.loadClass(path).asInstanceOf[Class[Module]]
            modules(packageName) += classPath -> c
            Some(c)
          } catch {
            case e : ClassNotFoundException => {
              Log.debug("ModuleLoader: Class " + classPath + " could not be found in package " + packageName)
              None
            }
          }
        }

        // Attempt to initialize the class, if found
        clazz match {
          case Some(c) => {
            try {
              Some(classToModule(c))
            } catch {
              case _ : InstantiationException | _ : ClassCastException => {
                Log.debug("ModuleLoader: Class " + classPath + " in package " + packageName + " could not be converted to a Module.")
                None
              }
              case e : Exception => {
                Log.debug("ModuleLoader: Error when loading module ", e)
                None
              }
            }
          }

          case _ => None
        }

      }
      case None => {
        Log.warning("ModuleLoader: Package " + packageName + " not loaded.")
        None
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
    if (!modules.contains(pack.name)) {
      try {
        val url = pack.toURL

        // Does the content exist?
        url.openConnection().connect()

        // Add package to URL base
        loader = new URLClassLoader(loader.getURLs.:+(url), this.getClass.getClassLoader)

        // Check for ModuleInit in that package
        try {
          val c = loader.loadClass("com.siigna.module.ModuleInit")
          val m = classToModule(c)
          _initModule = Some(m)
          Siigna.setInterface(m.interface)
          Log.success("ModuleLoader: Reloaded init module from " + pack + ".")
        } catch {
          // No module found
          case e : ClassNotFoundException => Log.info("ModuleLoader: No ModuleInit class found in package " + pack)
          // Modules are out of date
          case e : AbstractMethodError => Log.warning("ModuleLoader: ModuleInit from package " + pack.name + "' is incompatible with the current version of Siigna")
        }

        // Add to cache
        modules += pack.name -> collection.mutable.Map()

        Log.success("ModuleLoader: Sucessfully loaded the module package " + pack)
      } catch {
        case e : FileNotFoundException => Log.error("ModuleLoader: Could not find module package at URL: " + pack.toURL)
      }
    } else {
      Log.info("ModuleLoader: A package named '" + pack + "' has already been loaded.")
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
    modules -= pack.name
    loader = new URLClassLoader(loader.getURLs.filter(_ != pack.toURL), this.getClass.getClassLoader)
  }

}