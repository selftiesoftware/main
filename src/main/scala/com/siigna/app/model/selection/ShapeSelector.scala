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

package com.siigna.app.model.selection

import com.siigna.app.model.shape.Shape

/**
 * A ShapeSelector is a description of how a shape can be selected. This class exists so we can
 * describe how a shape have been selected in a manor that does not contain any logic, and so we
 * can access small parts of one single shape instead of creating new sub-instances of the shape.
 *
 * The ShapeSelector is different form the [[com.siigna.app.model.selection.ShapePart]], because
 * the ShapePart controls the relation and logic between the Selector and the
 * [[com.siigna.app.model.shape.Shape]]. That is, logic regarding additions, subtractions,
 * transformations and so forth.
 * 
 * <br /><b>Note: The specific implementation of the selectors varies for each shape</b>, but the standard is to
 * use a number of booleans equal to the number of segments in the shape to indicate which parts
 * (usually points) have been selected (true) or not selected (false).
 * @tparam  T  The type of the shape the ShapePart is a part of. In other words the 'parent' shape to this ShapePart.
 * @see [[com.siigna.app.model.selection.ShapePart]]
 */
trait ShapeSelector[+T <: Shape]

/**
 * An EmptyShapeSelector is a [[com.siigna.app.model.selection.ShapeSelector]] with no information and thus represents
 * an empty Shape subset.
 */
case object EmptyShapeSelector extends ShapeSelector[Nothing]

/**
 * A FullShapeSelector is a [[com.siigna.app.model.selection.ShapeSelector]] containing the entire shape. No
 * sub-selection magic is needed.
 */
case object FullShapeSelector extends ShapeSelector[Nothing]

