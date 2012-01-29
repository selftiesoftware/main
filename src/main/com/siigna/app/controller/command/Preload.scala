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
package com.siigna.app.controller.command

/**
 * Asks the controller to preload a given module.
 *
 * @param name  The symbolic representation of the module used inside Siigna to recognize the module.
 * @param classPath  The name of the path to load the class from, including the name of the class itself.
 * @param filePath  The name and the place of the given file to load.
 */
case class Preload(name : Symbol, classPath : String = "com.siigna.module.base", filePath : String = "") extends Command