package com.siigna.app.model.server

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
 * A Drawing is basically an identifier with an id and a name along with several pieces of
 * information relevant for the server when we need to store the server.
 */
trait Drawing {

  /**
   * The id of the drawing.
   * @return The unique number of this drawing or None if the drawing does not yet have one.
   */
  def id : Option[Int]

  /**
   * The owner of this drawing.
   * @return The user owning this drawing or None if no user is set.
   */
  def owner : Option[User]

  /**
   * The title of this drawing.
   * @return A string or None if the title is not yet set.
   */
  def title : Option[String]

}

/**
 * A companion object to Drawing which creates drawing instances.
 */
object Drawing {

  /**
   * Creates a drawing instance with no information attached.
   * @return  An empty IllegalDrawing.
   */
  def apply() = new IllegalDrawing(None, None, None)

  /**
   * Creates a drawing instance with an id attached.
   * @param id  The unique id of the drawing.
   * @return An IllegalDrawing.
   */
  def apply(id : Int) = new IllegalDrawing(Some(id), None, None)

  /**
   * Creates a drawing instance with a owner attached.
   * @param owner  The user that owns the drawing.
   * @return An IllegalDrawing.
   */
  def apply(owner : User) = new IllegalDrawing(None, Some(owner), None)

  /**
   * Creates a drawing instance with a owner attached.
   * @param title  The title of the drawing.
   * @return An IllegalDrawing.
   */
  def apply(title : String) = new IllegalDrawing(None, None, Some(title))

  /**
   * Creates a drawing instance with an id, owner and title attached.
   * @param id  The unique id of the drawing.
   * @param owner  The user that owns the drawing.
   * @param title  The title of the drawing.
   * @return A LegalDrawing.
   */
  def apply(id : Int, owner : User, title : String) = new LegalDrawing(Some(id), Some(owner), Some(title))

}

/**
 * A drawing which does not satisfy the demands set by the server to have an id, title and owner.
 */
sealed case class IllegalDrawing(id : Option[Int], owner : Option[User], title : Option[String]) extends Drawing

/**
 * A drawing which satisfies the demands set by the server to shave an id, title and owner.
 */
sealed case class LegalDrawing(id : Some[Int], owner : Some[User], title : Some[String]) extends Drawing
