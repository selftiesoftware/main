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

package com.siigna.util.collection

import java.awt.Color
import com.siigna.util.geom.{Vector2D, TransformationMatrix}

/**
 * Represents a set of attributes that can be retrieved as different types, but without a concrete
 * implementation, allowing it to be used in immutable or mutable contexts.
 *
 * @author Bjarke Walling <bjarke.walling@gmail.com>
 */
trait AttributesLike {

  /**
   * The attributes itself, stored in a map of strings to any.
   */
  protected def self : collection.Map[String, Any]

  /**
   * Gets an attribute value as an optional boolean. This means no exceptions
   * are thrown. You can either use pattern matching or the getOrElse feature
   * to provide a default value.
   *
   * <p>
   * Examples:
   * <pre>
   *   // Using pattern matching.
   *   attributes.boolean("TextItalic") match {
   *     case Some(italic) => println("Italic text: " + italic)
   *     case None         => println("Italic text: maybe")
   *   }
   *
   *   // Using default value.
   *   println( attributes boolean "TextItalic" getOrElse false )
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of attribute to look up.
   * @return  an optional attribute boolean value.
   */
  def boolean(attributeName : String) = getAsType(attributeName, _.asInstanceOf[Boolean])

  /**
   * Gets an attribute value as an optional color. This means no exceptions
   * are thrown. You can either use pattern matching or the getOrElse feature
   * to provide a default value.
   *
   * <p>
   * Examples:
   * <pre>
   *   // Using pattern matching.
   *   attributes.color("Background") match {
   *     case Some(background) => println("Bg color: " + background)
   *     case None             => println("Transparent background.")
   *   }
   *
   *   // Using default value.
   *   println( attributes color "Background" getOrElse Color.black )
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of attribute to look up.
   * @return  an optional attribute color value.
   */
  def color(attributeName : String) = getAsType(attributeName, _.asInstanceOf[Color])

  /**
   * Gets an attribute value as an optional double. This means no exceptions
   * are thrown. You can either use pattern matching or the getOrElse feature
   * to provide a default value.
   *
   * <p>
   * Examples:
   * <pre>
   *   // Using pattern matching.
   *   attributes.double("Size") match {
   *     case Some(size) => println("Size: " + size)
   *     case None       => println("Unknown size.")
   *   }
   *
   *   // Using default value.
   *   println( attributes double "Size" getOrElse 12.0 )
   * </pre>
   * </p>
   *
   * TODO: This method should accept values stored as Int (and other numeric types).
   *
   * @param  attributeName  the name of attribute to look up.
   * @return  an optional attribute double value.
   */
  def double(attributeName : String) = {
    // Try double
    val double = getAsType(attributeName, _.asInstanceOf[Double])
    if (double.isDefined) double
      
    // Try int
    else {
      val int : Option[Int] = getAsType(attributeName, _.asInstanceOf[Int])
      if (int.isDefined) Some(int.get.toDouble)
        
      // Try float 
      else {
        val float : Option[Float] = getAsType(attributeName, _.asInstanceOf[Float])
        if (float.isDefined) Some(float.get.toDouble)
          
        // Try long
        else {
          val long : Option[Long] = getAsType(attributeName, _.asInstanceOf[Long])
          if (long.isDefined) Some(long.get.toDouble)
          else None // No luck...
        }
      }
    }
  }
  
  /**
   * Get the value of an attribute if it exists and can be converted to a
   * certain type.
   *
   * @param  attributeName   the attribute name of interest.
   * @param  typeConversion  a function that converts the value to a certain type.
   * @return  Some(value) if attribute found and typeConversion succeeds, None otherwise.
   */
  protected def getAsType[T](attributeName : String, typeConversion : Any => T) : Option[T] =
      try {
        Some(typeConversion(self(attributeName)))
      } catch {
        case _ => None
      }

  /**
   * Gets an attribute value as an optional integer. This means no exceptions
   * are thrown. You can either use pattern matching or the getOrElse feature
   * to provide a default value.
   *
   * <p>
   * Examples:
   * <pre>
   *   // Using pattern matching.
   *   attributes.int("Count") match {
   *     case Some(count) => println("Number of items: " + count)
   *     case None        => println("Unknown number of items.")
   *   }
   *
   *   // Using default value.
   *   println( attributes int "Count" getOrElse 0 )
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of attribute to look up.
   * @return  an optional attribute integer value.
   */
  def int(attributeName : String) = getAsType(attributeName, _.asInstanceOf[Int])

  /**
   * Gets an attribute value as an optional string. This means no exceptions
   * are thrown. You can either use pattern matching or the getOrElse feature
   * to provide a default value.
   *
   * <p>
   * Examples:
   * <pre>
   *   // Using pattern matching.
   *   attributes.string("Name") match {
   *     case Some(name) => println("The name is: " + name)
   *     case None       => println("Unknown name.")
   *   }
   *
   *   // Using default value.
   *   println( attributes string "Name" getOrElse "Unknown" )
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of attribute to look up.
   * @return  an optional attribute string value.
   */
  def string(attributeName : String) = getAsType(attributeName, _.asInstanceOf[String])

  /**
   * Gets an attribute value as an optional TransformationMatrix. This means no exceptions
   * are thrown. You can either use pattern matching or the getOrElse feature
   * to provide a default value.
   *
   * <p>
   * Examples:
   * <pre>
   *   // Using pattern matching.
   *   attributes.transformationMatrix("Transform") match {
   *     case Some(matrix) => println("I'm transformed by: " + matrix)
   *     case None       => println("Unknown matrix.")
   *   }
   *
   *   // Using default value.
   *   println( attributes string "Name" getOrElse "TransformationMatrix" )
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of attribute to look up.
   * @return  an optional attribute string value.
   */
  def transformationMatrix(attributeName : String) = getAsType(attributeName, _.asInstanceOf[TransformationMatrix])
  
  /**
   * Gets an attribute value as an optional Vector2D. This means no exceptions
   * are thrown. You can either use pattern matching or the getOrElse feature
   * to provide a default value.
   *
   * <p>
   * Examples:
   * <pre>
   *   // Using pattern matching.
   *   attributes.vector("Direction") match {
   *     case Some(direction) => println("Direction: " + direction)
   *     case None            => println("Unknown direction.")
   *   }
   *
   *   // Using default value.
   *   println( attributes vector "Direction" getOrElse Vector2D(0, 0) )
   * </pre>
   * </p>
   *
   * @param  attributeName  the name of attribute to look up.
   * @return  an optional attribute vector value.
   */
  def vector2D(attributeName : String) = getAsType(attributeName, _.asInstanceOf[Vector2D])
  
}
