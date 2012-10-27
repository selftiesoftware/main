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

package com.siigna.util

import com.siigna.module.{ModulePackage, ModuleInstance}
import com.siigna.app.view.event.Event
import com.siigna.app.controller.ModuleLoader

/**
 * A utility object that includes utility methods and types for [[com.siigna.module.Module]]s.
 */
abstract class Modules {

  type Module = com.siigna.module.Module

  type StateMap = com.siigna.module.StateMap

  type State = com.siigna.module.State

  /**
   * @define moduleToModuleParameters  name
   * @define moduleToModuleInstance
   *         Creates a [[com.siigna.module.ModuleInstance]] of a module with the given $moduleToModuleParameters and
   *         returns it, so the controller can load and the new module. This is useful when modules needs to
   *         wrap the underlying understanding of [[com.siigna.module.ModuleInstance]]s and
   *         [[com.siigna.module.ModulePackage]]s away and maintain the simple module semantic.
   *
   * @param name  The name of the module.
   * @return  A [[com.siigna.module.ModuleInstance]] to be read by the controller.
   */
  def Module(name : Symbol) = ModuleInstance(ModuleLoader.base.get, "com.siigna.module" + ModuleLoader.base.get.name.name, name)

  /**
   * @define moduleToModuleParameters  name and class-path
   * $moduleToModuleInstance
   *
   * @param name  The name of the module
   * @param classPath  The class path of the module
   * @return A [[com.siigna.module.ModuleInstance]] to be read by the controller.
   */
  def Module(name : Symbol, classPath : String) = ModuleInstance(ModuleLoader.base.get, classPath, name)

  /**
   * @define moduleToModuleParameters  name, class-path and [[com.siigna.module.ModulePackage]]
   * $moduleToModuleInstance
   *
   * @param name  The name of the module
   * @param classPath  The class path of the module
   * @param pack  The package in which the module is defined
   * @return A [[com.siigna.module.ModuleInstance]] to be read by the controller.
   */
  def Module(name : Symbol, classPath : String, pack : ModulePackage) = ModuleInstance(pack, classPath, name)

  /**
   * An object that provides shortcuts to instantiate states that have a somewhat ugly syntax (especially if
   * you're not used to programming with functions).
   */
  object State {

    /**
     * Creates a state by explicitly stating that we wish to create a State to help the type-checking mechanisms in
     * Scala. States contain [[scala.PartialFunction]] which - if the implicit definitions do not catch the syntax -
     * contains some pretty heavy semantics. This can be avoided by explicitly stating which types the PartialFunction
     * has.
     *
     * @param f  The PartialFunction to apply.
     * @return  A PartialFunction accepting types of List[Event] and returning Any.
     */
    def apply(s : Symbol, f : PartialFunction[List[Event], Any]) = (s, f)

  }
}

/**
 * An object used for direct import. Import should happen through the package object in com.siigna.package.scala
 * @see http://www.scala-lang.org/docu/files/packageobjects/packageobjects.html
 */
object Modules extends Modules