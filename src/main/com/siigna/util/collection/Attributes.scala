/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.collection

import scala.collection.immutable.Map
import java.io.{ObjectOutput, ObjectInput}

/**
 * Represents a set of immutable attributes for a shape, a group of shapes
 * and other objects that might need it.
 *
 * <p>
 * You can create some attributes directly using the constructor:
 * <pre>
 *   val attr = Attributes( "TextSize" -> 24,
 *                          "TextItalic" -> true,
 *                          "Name" -> "Subtitle" )
 * </pre>
 * Or you can use a map or any other collection:
 * <pre>
 *   val attr = Attributes( Map( "Foo" -> "Bar" ) )
 * </pre>
 * You can also create an empty set of attributes and append to it. Since
 * Attributes is an immutable case class, you need to use a <code>var</code>:
 * <pre>
 *   var attr = Attributes()
 *   attr += "TextSize" -> 12
 *   attr += "Foo" -> "Bar"
 *   attr -= "Foo"  // Removes the previously added attribute Foo.
 * </pre>
 * </p>
 *
 * @author Bjarke Walling <bjarke.walling@gmail.com>
 *
 * @param  self  a map of keys and their values.
 */
case class Attributes(self : Map[String, Any])
     extends Map[String, Any]
     with AttributesLike {

  /**
   * Creates an empty instance of attributes.
   */
  def this() = this(Map[String, Any]())

  /**
   * Creates a set of attributes using a collection of key-value.
   *
   * <p>
   * Example:
   * <pre>
   *   val attributes = "TextColor" -> Color.red :: "Italic" -> false :: Nil
   *   val realAttributes = new Attributes( attributes )
   * </pre>
   * </p>
   *
   * <p>
   * Note: Does not terminate for infinite size collections.
   * </p>
   *
   * @param  attributes  a collection of key-value tuples.
   */
  def this(attributes : Iterable[(String, Any)]) = this(attributes.toMap)

  /**
   * Creates a set of attributes using multiple arguments with a randomly
   * generated id.
   *
   * <p>
   * Example:
   * <pre>
   *   val attr = new Attributes( "TextSize" -> 24,
   *                              "TextItalic" -> true,
   *                              "Name" -> "Subtitle" )
   * </pre>
   * </p>
   *
   * @param  attributes  multiple arguments of <code>key -> value</code>.
   */
  def this(attributes : (String, Any)*) = this(attributes.toMap)

  /**
   * Intersects this set of attributes with that set of attributes.
   *
   * @see #intersect
   */
  def &(that : Attributes) = intersect(that)

  /**
   * Adds a new attribute to this set of attributes.
   *
   * <p>
   * Examples:
   * <pre>
   *   var attr = Attributes( "TextSize" -> 12 )
   *   println(attr + ( "TextColor" -> Color.red ))  // Prints both attributes.
   *   attr += "TextItalic" -> false  // Adds the attribute to attr variable.
   * </pre>
   * </p>
   *
   * @param  attribute  the new attribute written as <code>key -> value</code>.
   * @return  a set of attributes containing the new attribute.
   */
  def +[T >: Any](attribute : (String, T)) =
    Attributes(self + attribute)

  /**
   * Adds two or more attributes to this set of attributes.
   *
   * @param  attribute1  one new attribute written as <code>key -> value</code>.
   * @param  attribute2  another new attribute.
   * @param  attributes  other new attributes.
   * @return  a set of attributes containing the new attributes.
   */
  override def +[T >: Any](attribute1 : (String, T), attribute2 : (String, T), attributes : (String, T)*) =
    Attributes(self + attribute1 + attribute2 ++ attributes)

  /**
   * Merges two sets of attributes, or a set of attribute and an iterator.
   * Attributes merged in takes precedence if a key already exists in this
   * set of attributes.
   *
   * @param  attributes  an iterator of attributes you want to merge in.
   * @return  a merged set of attributes.
   */
  def ++[T >: Any](attributes : Iterator[(String, T)]) =
    Attributes(self ++ attributes)

  /**
   * Merges two sets of attributes, or a set of attribute and a collection.
   * Attributes merged in takes precedence if a key already exists in this
   * set of attributes.
   *
   * <p>
   * Example:
   * <pre>
   *   val attrList = "TextColor" -> Color.red :: "Italics" -> false :: Nil
   *   val attr = Attributes( "TextSize" -> 12 ) ++ attrList
   * </pre>
   * </p>
   *
   * @param  attributes  the other set of attributes you want to merge in.
   * @return  a merged set of attributes.
   */
  def ++[T >: Any](attributes : Iterable[(String, T)]) =
    Attributes(self ++ attributes)

  /**
   * Removes an attribute from this set of attributes. It does not fail if the
   * attribute did not exist in the first place.
   *
   * <p>
   * Example:
   * <pre>
   *   var attr = Attributes( "TextSize" -> 12,
   *                          "TextColor" -> Color.red )
   *   println(attr - "TextColor" )  // Prints only TextSize attribute.
   *   attr -= "TextSize"  // Removes TextSize from the attr variable.
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of the attribute you want to remove.
   * @return  a set of attributes without the attribute you removed.
   */
  def -(attributeName : String) = Attributes(self - attributeName)

  /**
   * Removes two or more attributes to this set of attributes.
   *
   * @param  attributeName1  the name of one attribute to remove.
   * @param  attributeName2  the name of another attribute to remove.
   * @param  attributeNames  names of other attributes to remove.
   * @return  a set of attributes without the attributes you removed.
   */
  override def -(attributeName1 : String, attributeName2 : String, attributeNames : String*) =
    Attributes(self - attributeName1 - attributeName2 -- attributeNames)

  /**
   * Removes all attributes from this set of attributes, defined by an
   * iterator.
   *
   * <p>
   * Note: Does not terminate for infinite size collections.
   * </p>
   *
   * @param  attributeNames  an iterator of names of attributes to remove.
   * @return  a set of attributes without the attributes you removed.
   */
  def --(attributeNames : Iterator[String]) =
    Attributes(self -- attributeNames)

  /**
   * Removes all attributes from this set of attributes, defined by a
   * collection of attribute name.
   *
   * <p>
   * Note: Does not terminate for infinite size collections.
   * </p>
   *
   * @param  attributeNames  a collection of names of attributes to remove.
   * @return  a set of attributes without the attributes you removed.
   */
  def --(attributeNames : Traversable[String]) =
    Attributes(self -- attributeNames)

  /**
   * Creates a new iterator for all the attributes in this set. This is
   * required by the <code>Iterable</code> trait.
   *
   * @return the new iterator
   */
  def iterator = self.iterator

  /**
   * This method removes all the attributes for which the predicate returns
   * false.
   *
   * @param  predicate  a prediacte over key-value pairs.
   * @return  an updated set of attributes.
   */
  override def filter(predicate : ((String, Any)) => Boolean) =
    Attributes(self filter(predicate))

  /**
   * Check if this set of attributes contains the given attribute and return
   * the value if it exists.
   *
   * @param  attributeName  the attribute name of interest.
   * @return  Some(value) if the attribute exists, None if it does not.
   */
  override def get(attributeName : String) = self get attributeName

  /**
   * Intersects this set of attributes with that set of attributes. The result
   * is the set of attributes that exists in both this and that set and contain
   * exactly the same value (integers and doubles can be equal).
   *
   * @param  that  attributes to intersect with.
   * @return  the attributes that are in both sets of attributes.
   */
  def intersect(that : Attributes) = Attributes(Map() ++ (Set() ++ self).&(Set() ++ that.self))

  /**
   * The number of attributes.
   *
   * @return the number of key-value pairs.
   */
  override def size = self.size

  /**
   * Defines the prefix of this object's <code>toString</code> representation.
   */
  override def stringPrefix = "Attributes"

  /**
   * This function transforms all the values of attributes contained in this set
   * of attributes with function <code>transformation</code>.
   *
   * @param  transformation  a function that for each key-value pair calculates a new value.
   * @return  an updated set of attributes.
   */
  def transform(transformation : (String, Any) => Any) =
    Attributes(self transform(transformation))

  /**
   * This method allows one to create a new set of attributes with an additional
   * attribute. If the set of attributes already contains the
   * <code>attributeName</code> key, the attribute will be overridden by this
   * function.
   *
   * <p>
   * Example:
   * <pre>
   *   var attr = new Attributes( "TextSize" -> 24,
   *                              "TextItalic" -> true,
   *                              "Name" -> "Subtitle" )
   *   attr("FontFamily") = "sans-serif"  // Adds a new attribute.
   *   attr("TextItalic") = false         // Overrides an attribute.
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of the attribute to add.
   * @param  value          the value of the attribute to add.
   * @return  an updated set of attributes.
   */
  def update[V >: Any](attributeName : String, value : V) = Attributes(self updated(attributeName, value))

  /**
   * The same set of attributes with a given default function.
   *
   * @param  default  a function that defines default values of undefined attribute names.
   * @return  a set of attributes with a default function.
   */
  override def withDefault[T >: Any](default : String => T) =
    Attributes(self withDefault(default))

  /**
   * The same set of attributes with a given default value.
   *
   * @param  defaultValue  a value that is used for undefined attribute names.
   * @return  a set of attributes with a default value.
   */
  override def withDefaultValue[T >: Any](defaultValue : T) =
    Attributes(self withDefaultValue(defaultValue))
}

object Attributes {

  /**
   * Creates a set of attributes using a collection of key-value tuples.
   *
   * <p>
   * Example:
   * <pre>
   *   val attributes = "TextColor" -> Color.red :: "Italic" -> false :: Nil
   *   val realAttributes = Attributes( attributes )
   * </pre>
   * </p>
   *
   * <p>
   * Note: Does not terminate for infinite size collections.
   * </p>
   *
   * @param attributes  a collection of key-value tuples.
   * @return a new Attributes instance.
   */
  def apply(attributes : Iterable[(String, Any)]) : Attributes = new Attributes(attributes)

  /**
   * Creates a set of attributes using multiple arguments.
   *
   * <p>
   * Example:
   * <pre>
   *   val attr = Attributes( "TextSize" -> 24,
   *                          "TextColor" -> Color.red,
   *                          "TextItalic" -> true,
   *                          "Name" -> "Subtitle" )
   * </pre>
   * </p>
   *
   * @param  attributes  multiple arguments of <code>key -> value</code>.
   * @return  a new Attributes instance.
   */
  def apply(attributes : (String, Any)*) : Attributes = new Attributes(attributes)

  /**
   * An empty set of attributes.
   */
  val empty : Attributes = Attributes()

  /**
   * Creates a new set of attributes, only containing an five-letter identifier under the key "id".
   */
  def withId = Attributes("id" -> java.util.UUID.randomUUID.toString)

}