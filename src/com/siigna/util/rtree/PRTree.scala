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

package com.siigna.util.rtree

/**
 * A priority tree containing references to the shapes in the static Model.
 *
 * A priority tree is an R-tree but with priority-nodes for fast access.
 * This implementation is an approximation to the example given by the paper authored by Arge, de Berg, Haverkort and Yi (2004)
 * <a href="http://www.win.tue.nl/~mdberg/Papers/prtree.pdf">http://www.win.tue.nl/~mdberg/Papers/prtree.pdf</a> (PDF-warning).
 * 
 * @param T  The type of the elements to save
 * @param branchFactor  The branch factor is the maximum number of elements a single leaf can contain before being split.
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
class PRTree[T](branchFactor : Int) extends Branch[T](branchFactor, 0)