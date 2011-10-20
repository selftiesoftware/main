/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.dxf

import scala.actors.Debug

case class DXFValue(a : Int, b : Any) {
  def toDouble : Option[Double] = try {
    b match {
      case string : String => Some(java.lang.Double.parseDouble(string))
      case int : Int       => Some(int.toDouble)
      case double : Double => Some(double)
      case e => {
        Debug.info("DXFValue: Could not parse "+e+" into Double. Unknown element.")
        None
      }
    }
  } catch {
    case e => {
      Debug.warning("DXFValue: Could not parse "+b+" into a Double. Returning None.")
      None
    }
  }
  
  override def toString : String = "\n" + a.toString+ "\n" + b.toString
}