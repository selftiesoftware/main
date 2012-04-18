package com.siigna.server

import com.siigna.server.RemoteCommand

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

/**
 * A [[com.siigna.app.controller.command.remote.RemoteCommand]] that signals that the client wishes to
 * register any actions received in the drawing with the given id. If no id is given, we assume the client
 * wishes to register a new drawing.
 *
 * @param drawingId  The id of the drawing to register. None if the server should create an entirely new drawing.
 */
case class Register(userId: Option[Int], drawingId: Option[Int]) extends RemoteCommand