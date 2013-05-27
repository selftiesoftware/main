/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.module

import java.net.{JarURLConnection, URL}
import java.util.jar.JarFile

import actors.Futures._
import actors.Future
import com.siigna.util.Log

/**
 * <p>A ModulePackage is a number of modules grouped in a ''.jar'' file. This class represents the package and
 * its version number, title and location of the .jar file and thus a means to retrieve it via a URL given in
 * the <code>toURL</code> method.</p>

 * <p>The last two parameter ''domain'' and ''path'' are meant to be understood like a Uniform Resource Locator (URL)
 * where the domain comes first (fx ''www.example.org'') followed by the path (fx ''files/example.jar'').
 * So if I wanted to load a bundle of modules called ''ExampleModules'' in the file ''example.jar''
 * from http://example.org/modules/example.jar, the ModulePackage equivalent should be instantiated like so:
 * {{{
 *   ModulePackage("ExampleModules", "example.org", "modules/example.jar")
 * }}}
 * </p>
 *
 * <p>
 *   Lastly the ModulePackage can be set to local which means that the protocol for getting the resource changes.
 *   In other words the <code>toURL</code> method returns a location on the disk instead of a global resource
 *   available on the www.
 * </p>
 *
 * <h2>Overriding or replacing default behaviour</h2>
 * <p>
 *   It is possible to override the default behaviour for modules and [[com.siigna.module.ModulePackage]]s. Every
 *   time a ModulePackage is loaded we check for a class called <code>ModuleInit</code> in the
 *   <code>com.siigna.module</code> package. If this class exist we assume that it can work as the base of the package
 *   and will override every other init-modules previously implemented.
 * </p>
 * <p>
 *   So: If a [[com.siigna.module.ModulePackage]] wishes to define or override the
 *   initializing module, <b>a module needs to be placed in the <code>com.siigna.module</code>
 *   package under the name <code>ModuleInit</code>!</b>. If it is not, the init module will not work as intended.
 * </p>
 *
 * @see http://en.wikipedia.org/wiki/Uniform_resource_locator
 * @param name  The name of the modules pack, e. g. <i>'base</i> or <i>'randomModules</i>
 * @param domain  The www-domain of the pack, e. g. ''www.example.org''.
 * @param path  The path to the resource inside the domain, e. g. ''modules/example.jar''.
 * @param local  If set to true we treat this resource as local on the current machine, see <code>toURL</code>
 */
final case class ModulePackage(name : Symbol, domain : String, path : String, local : Boolean = false) {

  override def toString = "ModulePackage " + name + ": (" + domain + "/" + path + ")"

  /**
   * <p>
   *   Converts this ModulePackage to a URL while appending "''jar:http://''" to the domain, inserting a "''/''"
   *   between the domain and the path and lastly appending "''!/''" to indicate that it is a .jar resource.
   *   If the <code>local</code> parameter has been set the protocol is set to "''jar:file:''" instead.
   * </p>
   *
   * @see http://en.wikipedia.org/wiki/Uniform_resource_locator
   * @return  A URL specifying the location of the resource.
   */
  def toURL = new URL((if (local) "jar:file:" else "jar:http://") + domain + "/" + path + "!/")

}