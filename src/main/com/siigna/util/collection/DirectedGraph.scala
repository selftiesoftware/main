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

package com.siigna.util.collection

/**
 * An immutable directed graph with named arrows. An arrow has a type of
 * ((V, A), V) and can be written as V -> A -> V.
 *
 * <p>
 * Example code for instantiating a graph:
 * <pre>
 *   val g = DirectedGraph( 'a -> 1 -> 'b,
 *                          'a -> 2 -> 'a,
 *                          'b -> 1 -> 'a )       // Using multiple arguments.
 *
 *   val g = DirectedGraph(List( 'a -> 1 -> 'b,
 *                               'a -> 2 -> 'a,
 *                               'b -> 1 -> 'a )) // You can use any Collection.
 * </pre>
 * You can also use <code>new DirectedGraph(...)</code> if you prefer that.
 * </p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Directed_graph">Directed graph on Wikipedia</a>
 *
 * @author Bjarke Walling
 *
 * @param  [V]  the type of vertices.
 * @param  [A]  the type of arrow (directed edge) names.
 * @param  _arrows  the arrows that defines this graph.
 */
case class DirectedGraph[V,A](protected val _arrows : Iterable[((V,A),V)])
     extends Iterable[((V,A),V)] {

  /**
   * Creates an instance using multiple arguments.
   *
   * @param  [V]  the type of vertices.
   * @param  [A]  the type of arrow (directed edge) names.
   * @param  arrows  multiple arguments with arrows that defines this graph.
   */
  def this(arrows : ((V,A),V)*) = this(arrows)

  /**
   * Adds an arrow to this directed graph.
   *
   * @param  arrow  an arrow, ie. <code>vertex -> edge -> vertex</code>.
   * @return  a graph that contains the new arrow.
   */
  def +(arrow : ((V,A),V)) = DirectedGraph(_arrows.toList ::: List(arrow))

  /**
   * Concatenates this graph with that graph.
   *
   * @param  that  the graph you want to concatenate with.
   * @return  a new merged directed graph.
   */
  def ++[V2 <: V, A2 <: A](that : DirectedGraph[V2,A2]) = DirectedGraph(_arrows ++ that._arrows)

  /**
   * Creates a new iterator over all arrows contained in this graph. This is
   * required by the <code>Iterable</code> trait.
   *
   * @return the new iterator
   */
  def iterator = _arrows iterator

  /**
   * Number of arrows in this graph. This is required by the
   * <code>Collection</code> trait.
   *
   * @see #arrows
   *
   * @return the size
   */
  override def size = _arrows size

  /**
   * Arrows in this directed graph. The arrows are indexed in a map, going from
   * a vertex and an edge to follow resulting in the vertex you reach (if any).
   *
   * <p>
   * Example code:
   * <pre>
   *   val g = DirectedGraph( 'a -> 1 -> 'b,
   *                          'a -> 2 -> 'a,
   *                          'b -> 1 -> 'a )
   *   println(g arrows('b -> 1))  // 'a
   * </pre>
   * </p>
   */
  val arrows = Map() ++ _arrows

  /**
   * A set of all vertices in this graph. A vertex, also called a node, is a
   * "point" connected to other vertices through arrows.
   */
  val vertices = if (_arrows isEmpty) Set[V]()
                 else Set() ++ _arrows.map(a => List(a._1._1, a._2))
                                      .reduceLeft(_++_)

  /**
   * A set of names of all arrows (directed edges) in this graph.
   */
  val arrowNames = Set() ++ _arrows.map(_._1._2)

  /**
   * Walks the graph, going from a vertex through an arrow to a new vertex. The
   * result is the vertex you reach.
   *
   * <p>
   * Here is an example graph (only one arrow):
   * <pre>
   *        1
   *   A -------> B
   * </pre>
   * By specifying <code>A -> 1</code> you will reach <code>B</code>.
   * </p>
   *
   * <p>
   * Example code:
   * <pre>
   *   val g = DirectedGraph( 'a -> 1 -> 'b,
   *                          'a -> 2 -> 'a,
   *                          'b -> 1 -> 'a )
   *   var vertex = 'a
   *   vertex = g(vertex -> 2) getOrElse('x) // vertex is still 'a.
   *   vertex = g(vertex -> 1) getOrElse('x) // vertex is now 'b.
   *   vertex = g(vertex -> 2) getOrElse('x) // vertex is 'x (edge not found).
   * </pre>
   * </p>
   *
   * <p>
   * You can use pattern matching to determine whether you reached a vertex:
   * <pre>
   *   g('a -> 1) match {
   *     case Some(vertex) => println("Reached vertex: " + vertex)
   *     case None => println("Path not found.")
   *   }
   * </pre>
   * </p>
   *
   * @param  path  a vertex and arrow to walk through, ie. <code>'a -> 1</code>.
   * @return  Some(vertex) if you reach a new vertex, None if path not found.
   */
  def apply(path : (V,A)) = arrows get(path)

  /**
   * Looks up a vertex in the graph and returns a map with arrow names to
   * resulting vertices.
   *
   * <p>
   * Here is an example graph (only one arrow):
   * <pre>
   *        1
   *   A -------> B
   * </pre>
   * By specifying <code>A</code> you will get a map with <code>1 -> B</code>.
   * </p>
   *
   * @param  vertex  a vertex of interest.
   * @return  a map with arrows going outwards from the vertex.
   */
  def apply(vertex : V) = Map() ++ (arrows filter(_._1._1 == vertex) map(a => (a._1._2, a._2)))

  /**
   * A string representation of this graph.
   *
   * @return a string representation
   */
  override def toString =
    stringPrefix + "(" +
      _arrows.map(a => a._1._1 + " -> " + a._1._2 + " -> " + a._2)
             .mkString(", ") + ")"

  /**
   * A dot format representation of this graph. The tools that use this format
   * are also called GraphViz.
   *
   * @return a dot representation.
   */
  def toDot =
    "digraph DirectedGraph {\n" +
    _arrows.map(
      arrow => niceStringName(arrow._1._1) +
      " -> " + niceStringName(arrow._2) +
      " [label=\"" + niceStringName(arrow._1._2) + "\"]"
    ).mkString("\n").replace("$", "") + "\n}"

  private def niceStringName(obj : Any) =
    obj match {
      case Symbol(name) => name
      case _ => obj.toString
    }

}

/**
 * Utility functions for <code>DirectedGraph</code>. This includes
 * instantiation, ie. using multiple arguments or using a Collection (List, Seq,
 * Array, etc.).
 *
 * @see  DirectedGraph
 *
 * @author Bjarke Walling
 */
case object DirectedGraph {

  /**
   * Creates an instance of <code>DirectedGraph</code> using multiple arguments.
   *
   * @see  DirectedGraph
   *
   * @param  [V]  the type of vertices.
   * @param  [A]  the type of arrow (directed edge) names.
   * @param  arrows  multiple arguments with arrows that defines this graph.
   */
  def apply[V,A](arrows : ((V,A),V)*) = new DirectedGraph(arrows)

}
