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

import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.{Rectangle, Vector}

/**
 * A priority tree containing references to the shapes in the static Model.
 *
 * A priority tree is an R-tree but with priority-nodes for fast access.
 * This implementation is an approximation to the example given by the paper authored by Arge, de Berg, Haverkort and Yi (2004)
 * <a href="http://www.win.tue.nl/~mdberg/Papers/prtree.pdf">http://www.win.tue.nl/~mdberg/Papers/prtree.pdf</a> (PDF-warning).
 *
 * @param branchFactor  The branch factor is the maximum number of elements a single leaf can contain before being split. Default (and recommended) is 8. The value is truncated to fall within the range of [2; 1000]
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
class PRTree(branchFactor : Int = 8)  {

  /**
   * The branch factor which determines how many elements can reside in a leaf. <br />
   * This value is truncated between [2; 100], so the efficiency of the PRTree isn't completely lost.
   * <br />
   * According to Arge et al. the optimal value is 8.
   */
  val this.branchFactor = if (branchFactor < 2) 2 else if (branchFactor > 100) 100 else branchFactor
}
