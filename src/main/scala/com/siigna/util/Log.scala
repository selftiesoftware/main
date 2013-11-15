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

/**
 * A simple log object used to log messages to the console. To use it simply refer to it like so:
 * {{{
 *   Log("Some log message")
 * }}}
 * A more detailed log-level can be achieved by calling methods like <code>warning</code>, <code>error</code> and
 * <code>debug</code> or <code>success</code>, if desired.
 *
 * Log-levels are as follows:
 * <ol>
 *  <li>Error - Critical errors, preventing code from being executed.</li>
 *  <li>Warning - Unexpected behaviour causing the program to choose alternate actions.</li>
 *  <li>Info - Information about code being passed.</li>
 *  <li>Debug - Debug-information. Note: Can log quite a bit of lines.</li>
 *  <li>Success - Reports when something succeeds... Sometimes.</li>
 * </ol>
 */
object Log {

  val ERROR   = 1
  val WARNING = 2
  val INFO    = 4
  val DEBUG   = 8
  val SUCCESS = 16
  val ALL     = ERROR + WARNING + INFO + DEBUG + SUCCESS
  
  /**
   * The debug-level.
   */
  var level : Int = 1

  /**
   * The line-number.
   */
  private var lineNumber = 0

  private def format(message : Any, messageLevel : Int, refs : Seq[Any], error : Option[Throwable] = None) {
    if ((level & messageLevel) == messageLevel) {
      // Add to the line-number
      lineNumber += 1

      // Get the error-string, if defined, as the 5 latest errors
      val errorString = if (error.isDefined) " \n[StackTrace (" + error.get.getClass.getSimpleName + ")]: " + error.get.getMessage + "\n    " + error.get.getStackTrace.slice(0, 30).mkString("\n    ") else ""
      
      // Get the log-string  with references, if defined
      val text = if (refs.size > 0) {
         message.toString + " " + refs.mkString(", ") + errorString
      } else message.toString + errorString

      println(lineNumber.toString + ": " + text)
    }
  }

  /**
   * Logs messages as info by default.
   */
  def apply(message : Any) { info(message) }
  def apply(message : Any, e : Throwable) { info(message, e) }

  /**
   * Debug messages.
   */
  def debug(message : Any, refs : Any*) { format("[Debug] "+message, DEBUG, refs) }
  def debug(message : Any, e : Throwable, refs : Any*) { format("[Debug] "+message, DEBUG, refs, Some(e)) }

  def error(message : Any, refs : Any*) { format("[Error] "+message, ERROR, refs) }
  def error(message : Any, e : Throwable, refs : Any*) { format("[Error] "+message, ERROR, refs, Some(e)) }

  def info(message : Any, refs : Any*) { format("[Info] "+message, INFO, refs) }
  def info(message : Any, e : Throwable, refs : Any*) { format("[Info] "+message, INFO, refs, Some(e)) }

  def success(message : Any, refs : Any*) { format("[Success] "+message, SUCCESS, refs) }
  def success(message : Any, e : Throwable, refs : Any*) { format("[Success] "+message, SUCCESS, refs, Some(e)) }

  def warning(message : Any, refs : Any*) { format("[Warning] "+message, WARNING, refs) }
  def warning(message : Any, e : Throwable, refs : Any*) { format("[Warning] "+message, WARNING, refs, Some(e)) }

}