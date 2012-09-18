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

package com.siigna.app.controller.remote

import com.siigna.app.controller.remote.RemoteConstants.RemoteConstant
import com.siigna.app.controller.{Controller, Session}


/**
 * A RemoteCommand capable of setting a given attribute to a given value.
 */
@SerialVersionUID(-1044323852)
sealed case class Set(name : RemoteConstant, value : Option[Any], client : Session) extends RemoteCommand

/**
 * A companion object to the Set command.
 */
object Set {

  /**
   * Creates and dispatches a Set command to the Controller.
   * @param name  The type of the value to set.
   * @param value  The value of the value (uuuh, inception!!).
   */
  def apply(name : RemoteConstant, value : Option[Any]) {
    // Dispatches the command
    Controller ! ((c : Session) => Set(name, value, c))
  }
  
}
