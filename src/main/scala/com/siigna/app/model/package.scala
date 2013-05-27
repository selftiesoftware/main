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

import com.siigna.app.model.shape.Shape
import scala.collection.immutable.MapProxy

/**
 * The model package contains the model-structure in the model-view-controller pattern. It is here all the information
 * about [[com.siigna.app.model.shape.Shape]]s, [[com.siigna.app.model.action.Action]]s,
 * [[com.siigna.app.model.selection.Selection]]s and the active [[com.siigna.app.model.Drawing]] itself is stored.
 */
package object model {

  /**
   * A reference to the currently active model. Defaults to [[com.siigna.app.model.Drawing]]. This can be used
   * to override defaults behaviour in for instance the helper-objects in the [[com.siigna.app.model.selection]]
   * package.
   * @return  The [[com.siigna.app.model.Drawing]] object, representing the active drawing.
   */
  implicit def drawing : Drawing = Drawing

}
