/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model.shape

import collection.BitSet

/**
 * A ShapeSelector is a part of a shape represented in various ways. This class exists so we can 
 * access small parts of one single shape instead of creating new sub-instances of the shape.
 * Using ShapeSelectors it can thus be identified which parts of the shape are selected and which 
 * are not. 
 * 
 * <br /><b>Note: The specific implementation varies for each shape</b>, but the standard is to 
 * use a number of booleans equal to the number of segments in the shape to indicate which part has
 * been selected (true) or not selected (false).
 */
trait ShapeSelector extends Serializable

/**
 * An EmptySelector is a ShapeSelector with no information and thus represents an empty Shape subset.
 */
case object EmptySelector extends ShapeSelector

/**
 * A FullSelector signals that the ShapeSelector contains the entire shape. No sub-selection magic is needed.
 */
case object FullSelector extends ShapeSelector