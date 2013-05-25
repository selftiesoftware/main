/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.util.collection

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
 * A trait for objects containing attributes.
 * This trait is mainly used for [[com.siigna.app.model.shape.Shape]]s and the [[com.siigna.app.model.Drawing]] and
 * similar objects that require meta-data.
 */
trait HasAttributes {

  /**
   * The returning type of the methods in this class. Defaults to a type below the HasAttributes trait. Needed when
   * we need to make sure that the return-type are the same as the type we called (so setting the attributes on a
   * [[com.siigna.app.model.shape.Shape]] results in the same [[com.siigna.app.model.shape.Shape]]-type).
   */
  type T <: HasAttributes

  /**
   * Adds an attribute to the current attributes
   * @param attribute  The attributes to add
   * @return A shape with the new attributes
   */
  def addAttribute(attribute : (String, Any)) = setAttributes(this.attributes + attribute)

  /**
   * Adds attributes to the current attributes
   * @param attributes  The attributes to add
   * @return A shape with the new attributes
   */
  def addAttributes(attributes : Attributes) = setAttributes(this.attributes ++ attributes)
  
  /**
   * Adds attributes to the current attributes
   * @param attributes  The attributes to add
   * @return A shape with the new attributes
   */
  def addAttributes(attributes : (String, Any)*) = setAttributes(this.attributes ++ attributes)
   
  /**
   * The attributes of the current object.
   */
  def attributes : Attributes

  /**
   * Removes one attribute from the set of current attributes, if it exists.
   *
   * @param attribute  The attribute to remove.
   * @return  A HasAttributes with the attribute removed.
   */
  def removeAttribute(attribute: String) = setAttributes(attributes - attribute)

  /**
   * Remotes a set of attributes from the current attributes, if they exist.
   * @param attributes  The attributes to remove
   * @return  A HasAttributes with the attributes removed.
   */
  def removeAttributes(attributes: Traversable[String]) = setAttributes(this.attributes.--(attributes))

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param attribute  the new attribute to merge in.
   * @return  a HasAttributes object with the updated attributes.
   */
  def setAttribute(attribute: (String, Any)) = setAttributes(attributes + attribute)

  /**
   * Replace the current attributes with the given set of attributes.
   *
   * @param attributes  the new attributes to replace.
   * @return  a HasAttributes object with the updated attributes.
   */
  def setAttributes(attributes: Attributes): T

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * any colliding attributes with new values.
   *
   * @param attributes  the new attributes to merge in.
   * @return  a HasAttributes object with the updated attributes.
   */
  def setAttributes(attributes: (String, Any)*): T = setAttributes(this.attributes ++ attributes)

}