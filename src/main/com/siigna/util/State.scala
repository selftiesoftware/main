package com.siigna.util

import com.siigna.app.view.event.Event

/**
 * An object that provides shortcuts to instantiate states that have a somewhat ugly syntax (especially if
 * you're not used to programming with functions).
 */
object State {

  /**
   * Creates a state by explicitly stating that we wish to create a State to help the type-checking mechanisms in
   * Scala. States contain [[scala.PartialFunction]] which - if the implicit definitions do not catch the syntax -
   * contains some pretty heavy semantics. This can be avoided by explicitly stating which types the PartialFunction
   * has.
   *
   * @param f  The PartialFunction to apply.
   * @return  A PartialFunction accepting types of List[Event] and returning Any.
   */
  def apply(s : Symbol, f : PartialFunction[List[Event], Any]) = (s, f)

}
