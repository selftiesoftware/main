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

import java.net.{JarURLConnection, URL}

/**
 * <p>A ModulePackage is a number of modules grouped in a ''.jar'' file. This class represents the package and
 * its version number, title and location of that file and thus a means to retrieve it. A ModulePackage can be
 * downloaded
 * transformed to a URL so the resource can be extracted by the [[com.siigna.app.controller.modules.ModuleLoader]].</p>

 * <p>The last two parameter ''domain'' and ''path'' are meant to be understood like a Uniform Resource Locator (URL)
 * where the domain comes first (fx ''www.example.org'') followed by the path (fx ''files/example.jar'').
 * So if I wanted to load a bundle of modules called ''ExampleModules'' from http://example.org/modules/example.jar,
 * this class should be instantiated like so:
 * {{{
 *   ModulePackage("ExampleModules", "1.0", "example.org", "modules/example.jar")
 * }}}
 * </p>
 *
 * <p><b>Note:</b> On startup the class attempts to load the module from the given domain and path, so
 * watch out for errors when creating the class.</p>
 *
 * @see http://en.wikipedia.org/wiki/Uniform_resource_locator
 * @param domain  The www-domain of the pack, e. g. ''www.example.org''.
 * @param path  The path to the resource inside the domain, e. g. ''modules/example.jar''.
 * @throws IOException  If the jarFile could not be downloaded
 */
case class ModulePackage(name : String, domain : String, path : String) {

  /**
   * The jar file containing the module classes. This file is loaded on class initialization which can result
   * in Exceptions.
   * @throws ClassCastException  If the connection could not find a .jar resource
   * @throws IOException  If an error occurred while downloading the .jar
   */
  val jarFile = toURL.openConnection().asInstanceOf[JarURLConnection].getJarFile

  /**
   * Converts this ModulePackage to a URL while appending "''jar:http://''" to the domain, inserting a "''/''"
   * between the domain and the path and lastly appending "''!/''" to indicate that it is a .jar resource.
   * @see http://en.wikipedia.org/wiki/Uniform_resource_locator
   * @return  A URL specifying the location of the resource.
   */
  def toURL = new URL("jar:http://" + domain + "/" + path + "!/")

}
