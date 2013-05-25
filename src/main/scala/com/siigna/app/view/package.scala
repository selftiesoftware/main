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

package com.siigna.app

/**
 * The <i>view</i> package defines all the drawing functionality for Siigna.
 *
 * <br>
 * The package defines various traits for interacting with the graphical engine beneath Siigna. We have tried to
 * allow you to extend the functionality if you require a different setup than we provide. Both the
 * [[com.siigna.app.view.Graphics]] and [[com.siigna.app.view.Renderer]] traits provides interfaces that can be
 * implemented in different ways than we do - our take at implementing the default graphics mechanisms lies
 * in the [[com.siigna.app.view.native]] package.
 */
package object view
