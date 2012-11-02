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
package com.siigna.util.event

import com.siigna.module.{Module, ModuleLoader, ModulePackage, ModuleInstance}

/**
 * Events the modules can use to signal to each other.
 */
trait ModuleEvent extends Event

/**
 * Messages that can be sent to and from modules and states, containing a message of a given type.
 *
 * @param message  any object that the module wishes to forward.
 */
case class Message[T](message : T) extends ModuleEvent { val symbol = 'Message }

/**
 * An event that signals a [[com.siigna.module.Module]] to end with a given message.
 * @param message  The message to pass to the 'parent' module.
 * @tparam T  The type of the message.
 */
case class End[T](message: T) extends ModuleEvent { val symbol = 'ModuleEnd }

/**
 * An event that signals a [[com.siigna.module.Module]] to end.
 */
case object End extends End[Unit]()

/**
 * An event that signals that a [[com.siigna.module.Module]] wishes to initialize and forward to the given module.
 * This can be used effectively by splitting functionality into different modules and invoking them when needed.
 * @param module  The module to initialize and forward to.
 */
case class Start(module : ModuleInstance) extends ModuleEvent { val symbol = 'ModuleStart }

/**
 * Companion object for case class [[com.siigna.util.event.Start]]
 */
object Start {

  /**
   * @define moduleToModuleParameters  name and class-path
   * Creates a [[com.siigna.module.ModuleInstance]] of a module with the given name and class-path and
   * returns it, so the controller can load and the new module. This is useful when modules needs to
   * wrap the underlying understanding of [[com.siigna.module.ModuleInstance]]s and
   * [[com.siigna.module.ModulePackage]]s away and maintain the simple module semantic.
   *
   * @param name  The name of the module
   * @param classPath  The class path of the module
   * @return A [[com.siigna.module.ModuleInstance]] to be read by the controller.
   */
  def apply(name : Symbol, classPath : String) : Start = Start(Module(name, classPath))

}