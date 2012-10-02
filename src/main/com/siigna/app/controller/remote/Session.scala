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

import com.siigna.app.model.server.User

/**
 * A client session represented by an id of the drawing the current instance of Siigna is associated with
 * and a token given by the server to authenticate the client.
 *
 * @param drawing The unique identifier of the drawing the client is drawing on.
 * @param user  The user associated with the session.
 */
@SerialVersionUID(-160078897)
case class Session(drawing : Long, user : User)