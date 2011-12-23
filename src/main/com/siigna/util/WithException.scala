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

package com.siigna.util

trait WithException

/**
 * Used to fetch an attribute with the possibility of an exception being thrown.
 *
 * <p>
 * Here is an example:
 * <pre>
 *   val attr = Attributes("TextSize" -> 12, "TextColor" -> "#3399FF")
 *   println(attr int("TextSize", WithException)) // 12
 *   println(attr int("TextColor", WithException)) // NumberFormatException
 *   println(attr string("Italic", WithException)) // NoSuchElementException
 * </pre>
 * </p>
 *
 * @see com.siigna.util.collection.Attributes
 */
object WithException extends WithException
