/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.view

import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.util.geom.Vector2D
import com.siigna.util.geom.Vector2D._
import java.awt.Color

object modulesLoader {

  val frame = Iterable         (LineShape(Vector2D(8.345,2),Vector2D(101.7,2)),
                                LineShape(Vector2D(5,15.56),Vector2D(5,5.345)),
                                LineShape(Vector2D(105,15.56),Vector2D(105,5.345)),
                                LineShape(Vector2D(8.34,18.91),Vector2D(101.7,18.91)),

                                LineShape(Vector2D(105,13.59),Vector2D(5,13.59)),

                                ArcShape(Vector2D(8.34,5.34),3.34,180,-90),
                                ArcShape(Vector2D(101.65,5.34),3.34,0,90),
                                ArcShape(Vector2D(101.65,15.56),3.34,0,-90),
                                ArcShape(Vector2D(8.34,15.56),3.34,180,90),

                                LineShape(Vector2D(55,17.84),Vector2D(53.21,14.70)),
                                LineShape(Vector2D(53.21,14.70),Vector2D(57,14.70)),
                                LineShape(Vector2D(57,14.70),Vector2D(55,17.84)))

  val frameFill = Array        (Vector2D(5,5.345),Vector2D(5,15.56),
                                Vector2D(5,15.56),Vector2D(8.34,18.91),
                                Vector2D(8.34,18.91),Vector2D(101.7,18.91),
                                Vector2D(101.7,18.91),Vector2D(105,15.56),
                                Vector2D(105,15.56),Vector2D(105,5.345),
                                Vector2D(105,5.345),Vector2D(101.7,2),
                                Vector2D(101.7,2),Vector2D(8.34,2),
                                Vector2D(8.34,2),Vector2D(5,5.345))



  def paint (g : Graphics, t : TransformationMatrix)= {

    val menuColor       = new Color(0.95f, 0.95f, 0.95f, 1.00f)
    val fillScreenX = frameFill.map(_.x.toInt).toArray
    val fillScreenY = frameFill.map(_.y.toInt).toArray

    g setColor menuColor
    g.g.fillPolygon(fillScreenX, fillScreenY, frameFill.size)

    frame.foreach(g draw)
    g draw TextShape("BASE MODULES",Vector2D(19,4.5),8)

  }
}
