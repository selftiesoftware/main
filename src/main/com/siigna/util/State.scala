package com.siigna.util

import com.siigna.app.view.event.Event

/**
 * An object that provides shortcuts to instantiate states that have a somewhat ugly syntax (especially if
 * you're not used to programming with functions).
 */
object State {

  /**
   * Creates a state by converting a function to a partial function for use when
   * creating [[com.siigna.util.State]]s.
   *
   * <p>Example:
   * '''
   * State('Start, () => {
   *   ...
   * })
   * '''
   * Which is slightly shorter than for instance
   * '''
   * State('Start, {
   *   case _ => {
   *     ...
   *   }
   * }
   * '''
   * </p>
   *
   * @param f  The function to convert.
   * @return  A PartialFunction accepting types of List[Event] and returning Any.
   */
  def apply(s : Symbol, f : () => Any) : com.siigna.State = (s, new PartialFunction[List[Event], Any] {
    def apply(x : List[Event]) = f()
    def isDefinedAt(x : List[Event]) = true
  })

}
