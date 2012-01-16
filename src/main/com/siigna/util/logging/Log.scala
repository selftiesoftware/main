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

package com.siigna.util.logging

/**
 * A simple log object used to log messages to the console.
 */
object Log extends scala.util.logging.ConsoleLogger {
  
  /**
   * The debug-level. Levels are following:
   * <ol>
   *  <li>Error - Critical errors, preventing code from being executed.</li>
   *  <li>Warning - Unexpected behaviour causing the program to choose alternate actions.</li>
   *  <li>Info - Information about code being passed.</li>
   *  <li>Debug - Debug-information. Note: Can log quite a bit of lines.</li>
   *  <li>Success - Reports when something succeeds... Sometimes.</li>
   * </ol>
   *
   * TODO: Use twitter's util-logging instead of inventing the wheel damnit!
   */
  var level : Int = 0

  /**
   * The line-number.
   */
  private var lineNumber = 0

  val ERROR   = 1
  val WARNING = 2
  val INFO    = 4
  val DEBUG   = 8
  val SUCCESS = 16

  private def format(message : Any, messageLevel : Int, refs : Seq[Any], error : Option[Throwable] = None) {
    if ((level & messageLevel) == messageLevel) { // TODO: Is this working?
      // Add to the line-number
      lineNumber += 1

      // Get the error-string, if defined
      val errorString = if (error.isDefined) " - " + error.get.getMessage else ""

      // Get the log-string  with references, if defined
      val text = if (refs.size > 0) {
         message.toString + " " + refs.mkString(", ") + errorString
      } else message.toString + errorString

      log(lineNumber.toString + ": " + text)
    }
  }

  /**
   * Logs messages as info by default.
   */
  def apply(message : Any, refs : Any*) : Unit = info(message, refs)
  def apply(message : Any, e : Throwable, refs : Any*) : Unit = info(message, e, refs)

  /**
   * Debug messages.
   */
  def debug(message : Any, refs : Any*) : Unit = format("[Debug] "+message, DEBUG, refs)
  def debug(message : Any, e : Throwable, refs : Any*) : Unit = format(message, DEBUG, refs, Some(e))

  def error(message : Any, refs : Any*) : Unit = format("[Error] "+message, ERROR, refs)
  def error(message : Any, e : Throwable, refs : Any*) : Unit = format(message, ERROR, refs, Some(e))

  def info(message : Any, refs : Any*) : Unit = format("[Info] "+message, INFO, refs)
  def info(message : Any, e : Throwable, refs : Any*) : Unit = format(message, INFO, refs, Some(e))

  def success(message : Any, refs : Any*) : Unit = format("[Success] "+message, SUCCESS, refs)
  def success(message : Any, e : Throwable, refs : Any*) : Unit = format(message, SUCCESS, refs, Some(e))

  def warning(message : Any, refs : Any*) : Unit = format("[Warning] "+message, WARNING, refs)
  def warning(message : Any, e : Throwable, refs : Any*) : Unit = format(message, WARNING, refs, Some(e))

}