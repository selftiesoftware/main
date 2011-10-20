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

package com.siigna.util.event

import com.siigna.util.collection.DirectedGraph

/**
 * Handles events using two state diagrams: one that maps events that leed to
 * states (the stateMap) and one that maps states with actions (the stateMachine).
 * Both the states and events are described using the build-in scala symbols
 * (example: <code>'MouseDown</code>).
 *
 * @param stateMap  the state map.
 * @param stateMachine  a list of the states and their associated actions.
 */
case class EventHandler(stateMap : DirectedGraph[Symbol, Symbol], stateMachine : Map[Symbol, Function1[List[Event], Any]])