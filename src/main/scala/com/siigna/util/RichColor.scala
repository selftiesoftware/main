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

package com.siigna.util

import java.awt.Color

class RichColor(color : Color) {

  /**
   * Returns a string representation of a color, e.g. "#3399FF", without the
   * alpha value.
   *
   * @return  a HTML-friendly string representation.
   */
  def toHtmlString =
    "#" + Integer.toHexString((color.getRGB & 0xffffff) | 0x1000000).substring(1)
}

class RichColorString(string : String) {

  /**
   * Returns a color object from a HTML string representation.
   *
   * @return  a AWT Color object.
   */
  def color =
  {
    val value = string.trim
    if (value.startsWith("#") && value.size == 7) {
      try {
        new Color(Integer parseInt(value substring(1), 16))
      } catch {
        case ex : Throwable => throw new IllegalArgumentException("Expected a color, but got: " + value, ex)
      }
    } else
      throw new IllegalArgumentException("Expected a color, but got: " + value)
  }

}