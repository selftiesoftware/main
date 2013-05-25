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

package com.siigna

/**
 * The <i>util</i> package contains auxiliary functionality to Siigna.
 * <ul>
 *   <li>
 *     The collection package contains classes regarding attributes and other collection-based implementations.
 *   </li>
 *   <li>
 *     The event package contains classes representing user-events and event-handling such as the
 *     [[com.siigna.util.event.EventParser]] and [[com.siigna.util.event.Snap]] and [[com.siigna.util.event.Track]].
 *   </li>
 *   <li>
 *     The geom package contains geometries and methods to calculate on and manipulate geometric objects. The
 *     persistence package is used to marshal and un-marshal objects and write to or read from a file. In restricted
 *     environments (such as an applet) it is necessary to use these utility methods, because third-party code
 *     (such as modules) are not allowed to perform any I/O operations.
 *   </li>
 * </ul>
 */
package object util
