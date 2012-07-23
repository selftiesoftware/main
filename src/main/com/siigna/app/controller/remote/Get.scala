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

import com.siigna.app.Siigna
import com.siigna.app.controller.{Controller, Client}

/**
 * A RemoteCommand capable of retrieving a given attribute from the remote server.
 */
@SerialVersionUID(-348100723)
case class Get(name : RemoteConstant, value : Option[Any], client : Client) extends RemoteCommand {

  // Dispatches the command
  Controller ! this
}