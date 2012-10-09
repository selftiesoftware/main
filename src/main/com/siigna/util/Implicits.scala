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

package com.siigna.util

import java.awt.Color
import com.siigna.app.view.event.Event

/**
 * Contains several implicit definitions which are collected in a single object
 * to easen the implementation.
 * See <a href="http://scala.sygneca.com/patterns/pimp-my-library" title="Pimp my library">Pimp my library</a>
 * on <a href="http://scala.sygneca.com/" title="Scala wiki">Scala Wiki</a>.
 */
trait Implicits {

  /**
   * Implicitly adds a toHtmlString method on AWT Color objects. To use this
   * <code>import Implicits.awtColor2RichColor</code> in your code.
   *
   * <p>
   * Example:
   * <pre>
   *   import com.siigna.util.Implicits.awtColorToRichColor
   *   import java.awt.Color
   *   println(Color yellow toHtmlString)  // Prints #ffff00.
   * </pre>
   * </p>
   */
  implicit def awtColorToRichColor(color : Color) = new RichColor(color)

  /**
   * Implicitly convert a Function0[Any] to a PartialFunction[List[Event], Any], used in the
   * [[com.siigna.module.Module]]s stateMap. This conversion is useful if you would like to write states like:
   * {{{
   *   State('Start -> () => ( ... ))
   * }}}
   * @param f  The function to convert to a partial function
   * @return A PartialFunction
   */
  implicit def funToPartialFun(f : () => Any) = new PartialFunction[List[Event], Any] {
    def isDefinedAt(x : List[Event]) = true
    def apply(x : List[Event]) = f()
  }

  /**
   * Implicitly convert a Function1[List[Event, Any]] to a PartialFunction[List[Event], Any], used in the
   * [[com.siigna.module.Module]]s stateMap. This conversion is useful if you would like to write states like:
   * {{{
   *   State('Start -> (events : List[Event]) => ( ... ))
   * }}}
   * @param f  The function to convert to a partial function
   * @return A PartialFunction
   */
  implicit def funToPartialFun(f : (List[Event]) => Any) = new PartialFunction[List[Event], Any] {
    def isDefinedAt(x : List[Event]) = true
    def apply(x : List[Event]) = f(x)
  }

  /**
   * Implicitly adds a color method on String objects. To use this
   * <code>import Implicits.stringToColor</code> in your code.
   *
   * <p>
   * Example:
   * <pre>
   *   import com.siigna.util.Implicits.stringToColor
   *   import java.awt.Color
   *   println("#FFFFFF".color)  // Prints Color white.
   * </pre>
   * </p>
   */
  implicit def stringToColor(string : String) = new RichColorString(string)

}

/**
 * An object used for direct import. Import should happen through the package object in com.siigna.package.scala
  * @see http://www.scala-lang.org/docu/files/packageobjects/packageobjects.html
 */
object Implicits extends Implicits
