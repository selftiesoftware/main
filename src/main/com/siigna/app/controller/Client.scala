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

import com.siigna.app.model.server.Drawing

/**
 * A client represented by a unique identifier.
 * @param id  The unique identifier of the client, given by the server. Zero if no id has been given.
 * @param drawing  A unique identifier of the drawing, the client is currently drawing upon. Zero if no drawing is active.
 */
@SerialVersionUID(-1673622368)
case class Client(id : Long = 0L, drawing : Drawing = Drawing())