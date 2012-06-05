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

import com.siigna.util.geom.{Rectangle, Vector}
import java.awt.Color
import com.siigna.app.model.Model

//import com.siigna.app.model.Selection

/**
 * Contains several implicit definitions which are collected in a single object
 * to easen the implementation.
 * See <a href="http://scala.sygneca.com/patterns/pimp-my-library" title="Pimp my library">Pimp my library</a>
 * on <a href="http://scala.sygneca.com/" title="Scala wiki">Scala Wiki</a>.
 */
abstract class Implicits {

  /**
   * Implicitly adds a toHtmlString method on AWT Color objects. To use this
   * <code>import RichColor.awtColor2RichColor</code> in your code.
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
   * Implicitly adds a color method on String objects. To use this
   * <code>import RichColor.stringToColor</code> in your code.
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
 * An object used for direct import. Import should happen through com.siigna.package.scala.
 */
object Implicits extends Implicits
