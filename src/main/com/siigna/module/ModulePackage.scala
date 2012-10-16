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

import java.net.{JarURLConnection, URL}
import java.util.jar.JarFile

import actors.Futures._
import actors.Future

/**
 * <p>A ModulePackage is a number of modules grouped in a ''.jar'' file. This class represents the package and
 * its version number, title and location of that file and thus a means to retrieve it. A ModulePackage can be
 * downloaded through the [[com.siigna.app.controller.modules.ModuleClassLoader]] via its
 * <code>load</code> method. A ModulePackage can also be transformed to a URL via the <code>toURL</code> method.</p>

 * <p>The last two parameter ''domain'' and ''path'' are meant to be understood like a Uniform Resource Locator (URL)
 * where the domain comes first (fx ''www.example.org'') followed by the path (fx ''files/example.jar'').
 * So if I wanted to load a bundle of modules called ''ExampleModules'' from http://example.org/modules/example.jar,
 * this class should be instantiated like so:
 * {{{
 *   ModulePackage("ExampleModules", "1.0", "example.org", "modules/example.jar")
 * }}}
 * </p>
 *
 * <p>
 *   Lastly the ModulePackage can be set to local which means that the protocol for getting the resource changes.
 *   In other words the <code>toURL</code> method returns a location on the disk instead of a global resource
 *   available on the www.
 * </p>
 *
 * @see http://en.wikipedia.org/wiki/Uniform_resource_locator
 * @param name  The name of the modules pack, e. g. <i>'base</i> or <i>'randomModules</i>
 * @param domain  The www-domain of the pack, e. g. ''www.example.org''.
 * @param path  The path to the resource inside the domain, e. g. ''modules/example.jar''.
 * @param local  If set to true we treat this resource as local on the current machine, see <code>toURL</code>
 * @throws IOException  If the jarFile could not be downloaded
 */
final case class ModulePackage(name : Symbol, domain : String, path : String, local : Boolean = false) {

  /**
   * The [[java.util.jar.JarFile]] represented as a [[scala.actors.Future]]. Be careful to force-load the value
   * since it might block the calling thread.
   */
  val jar : Future[JarFile] = future { toURL.openConnection().asInstanceOf[JarURLConnection].getJarFile }

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