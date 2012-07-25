package com.siigna.app.controller.remote

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

import com.siigna.app.controller.command.Command
import com.siigna.app.controller.Client

/**
 * A [[com.siigna.app.controller.command.Command]] that can be sent over the network to the Siigna Universe,
 * and thus provides semantics for instructing the Server and informing the Client.
 *
 * Every remote command going <b>from</b> the client <b>to</b> the server are simply the native type. So for
 * instance a registration of the client will be performed via the Register command.
 */
trait RemoteCommand extends Command with Serializable {

  /**
   * The client who are sending this command.
   * @return The client associated with the command.
   */
  def client : Client
  
}

