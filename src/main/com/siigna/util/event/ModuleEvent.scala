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

import com.siigna.module.{Module, ModuleInstance}

/**
 * Events the modules can use to signal to each other.
 */
sealed trait ModuleEvent extends Event

/**
 * Messages that can be sent to and from states, containing a message of a given type.
 * <b>Note:</b> Messages are treated like normal events and are <i>not</i> given to children and/or
 * parent modules like [[com.siigna.util.event.Start]] and [[com.siigna.util.event.End]].
 *
 * @param message  any object that the module wishes to pass on to other states.
 */
final case class Message[T](message : T) extends ModuleEvent { val symbol = 'Message }

/**
 * An event that signals a [[com.siigna.module.Module]] to end with a given message.
 * @param message  The message to pass to the 'parent' module.
 * @tparam T  The type of the message.
 */
final case class End[T](message: T) extends ModuleEvent { val symbol = 'ModuleEnd }

/**
 * An event that signals a [[com.siigna.module.Module]] to end.
 */
case object End extends ModuleEvent { val symbol = 'ModuleEnd }

/**
 * An event that signals that a [[com.siigna.module.Module]] wishes to initialize and forward to the given module.
 * This can be used effectively by splitting functionality into different modules and invoking them when needed.
 * @param module  The module to initialize and forward to.
 * @param message  An optional message to the new module.
 */
final case class Start[T](module : ModuleInstance, message : T) extends ModuleEvent { val symbol = 'ModuleStart }

/**
 * Companion object for case class [[com.siigna.util.event.Start]]
 */
object Start {

  /**
   * Starts a [[com.siigna.module.ModuleInstance]] with the given module path and
   * returns it, so the controller can load and the new module.
   *
   * @param module The [[com.siigna.module.ModuleInstance]] to create
   * @return A [[com.siigna.module.ModuleInstance]]
   */
  def apply(module : ModuleInstance) : Start[Unit] = new Start[Unit](module, Unit)

  /**
   * Starts a [[com.siigna.module.ModuleInstance]] with the given name and class-path and
   * returns it, so the controller can load and the new module.
   *
   * @param name  The name of the module
   * @param classPath  The class path of the module
   * @return A [[com.siigna.module.ModuleInstance]]
   */
  def apply(name : Symbol, classPath : String) : Start[_] = new Start(Module(name, classPath), Unit)

  /**
   * Starts a [[com.siigna.module.ModuleInstance]] with the given name and class-path and
   * returns it, so the controller can load and the new module with the given message.
   *
   * @param name  The name of the module
   * @param classPath  The class path of the module
   * @return A [[com.siigna.module.ModuleInstance]]
   */
  def apply[T](name : Symbol, classPath : String, message : T) : Start[T] = new Start[T](Module(name, classPath), message)

}