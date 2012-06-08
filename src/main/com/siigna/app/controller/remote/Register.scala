package com.siigna.app.controller.remote

import com.siigna.app.controller.Client
import com.siigna.app.model.server.User

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
 * <p>A [[com.siigna.app.controller.remote.RemoteCommand]] that signals that the client wishes to
 * register on the server with the given user and drawing id.</p>
 * <p>If no user is given there is a possibility the user is denied access or the like.</p>
 * <p>If no drawing id is given we assume we would like to create a new drawing.</p>
 *
 * @param user  The user logged in.
 * @param drawingId  The id of the server to register. None if the remote should create an entirely new server.
 * @param client  The unique client associated with this Siigna instance. The default value is overridden by the server.
 */
@SerialVersionUID(-859328452)
case class Register(user : User, drawingId : Option[Int], client : Client = Client()) extends RemoteCommand