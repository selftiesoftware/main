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

package com.siigna.util.geom

/**
 * A representation of the dimension of a geometry-object. This type ensures that the geometry stayes in
 * dimension
 */
sealed trait Dimension

/**
 * A representation of 2-dimensional objects.
 */
trait Dimension2 extends Dimension

/**
 * A representation of 3-dimensional objects.
 */
trait Dimension3 extends Dimension