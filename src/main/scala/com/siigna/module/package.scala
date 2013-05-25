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
 * <p>
 *   The module package contains the [[com.siigna.module.Module]] definition at the root and other module-packages at
 *   the sub-levels. The standard Siigna module package is defined to be stored in the <code>base</code> sub-package.
 * </p>
 *
 * <p>
 *   Feel free to add your own sub-modules!
 * </p>
 */
package object module {

  /**
   * <p>
   *  A State is a "location" in a [[com.siigna.module.Module]] coupled with certain functionality. The name of the
   *  State functions as its unique identifier, so it can be referred to from other states that needs to forward to it.
   * </p>
   *
   * @see http://en.wikipedia.org/wiki/Finite-state_machine
   * @see http://en.wikipedia.org/wiki/State_(computer_science)
   */
  type State = (Symbol, PartialFunction[List[Event], Any])

  /**
   * <p>
   *   A StateMap is a map of the states in a
   */
  type StateMap = Map[Symbol, PartialFunction[List[Event], Any]]

}
