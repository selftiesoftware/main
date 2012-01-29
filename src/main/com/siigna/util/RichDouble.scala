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

/**
 * Pimp-my-library for Doubles.
 */
class RichDouble(double : Double) {

  /**
   * Round the number to the nearest integer.
   */
  def round : Double = scala.math.round(double)
  
  /**
   * Round the number with a given number of decimals.
   */
  def round(decimals : Int) : Double = {
    if (decimals <= 6) {
      val formatter = new java.text.DecimalFormat("%."+decimals+"f")
      formatter.format(double).asInstanceOf[Double]
    } else double
  }

}
