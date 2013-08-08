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
 * <h2>geom</h2>
 * <p>
 *   The geom package contains mathematical definitions for geometric structures in an euclidian space such as
 *   [[com.siigna.util.geom.Vector]]s, [[com.siigna.util.geom.Segment]]s (finite [[com.siigna.util.geom.Line]]s),
 *   [[com.siigna.util.geom.Rectangle]]s etc.
 * </p>
 * <h2>Conventions</h2>
 * <p>
 *   The geometry uses some mathematical conventions for consistency. We have tried to follow the same rules as
 *   those applied in the field of mathematics and physics.
 *   <dl>
 *     <dt>Angles</dt>
 *     <dd>
 *       Angles are defined as degrees (from 0 to 360) and runs <b>counter-clockwise</b>. We define <b>0 (zero) degrees
 *       to be equal to 3 o'clock (east) on a clock</b>. Some fancy ASCII-art to illustrate:
 *       {{{
 *              - 90 -
 *            /        \
 *          180         0
 *           \         /
 *             - 270 -
 *       }}}
 *     </dd>
 *   </dl>
 * </p>
 */
package object geom {

  /**
   * Normalizes a degree-value (not radians) to fit in a range of 0 - 360.
   * @param degrees  The degrees to normalize.
   * @return  A positive number between 0 and 360.
   */
  def normalizeDegrees(degrees : Double) : Double = {
    degrees % 360 match {
      case d if d < 0 => d + 360
      case d          => d
    }
  }

}
