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

package com.siigna.app.controller

import com.siigna.util.logging.Log
import command.{Preload, ForwardTo, Command}
import remote._

/**
 * An object whose sole responsibility is to handle incoming and outgoing
 * [[com.siigna.app.controller.remote.RemoteCommand]]s.
 *
 * $controlHierarchy
 */
trait CommandController extends EventController {

  /**
   * Examines the input command and handles it appropriately.
   * @param command  The RemoteCommand to process.
   */
  protected def apply(command : Command) {
    try {
      command match {

        // Handle ordinary commands (from the modules):
        case command : Command => {
          // Match on the command
          command match {
            // Forward to another module
            case ForwardTo(symbol, continue) => {

            }

            // Preload a module
            case Preload(name, classPath, filePath) => {
              moduleBank.preload(name, classPath, filePath)
            }
            // Match unknown commands
            case unknown => Log.warning("Controller received unknown command: "+unknown)
          }
        }
      }
    }
  }

  /**
   * Examines if the function is defined for the given input. Defaults to true for all RemoteCommands.
   * @param x  The input to examine.
   * @return  True, since the incoming type is always type RemoteCommand.
   */
  def isDefinedAt(x : RemoteCommand) = true

}
