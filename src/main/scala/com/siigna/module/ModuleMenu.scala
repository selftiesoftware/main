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

package com.siigna.module

import java.awt._

import com.siigna.app.Siigna
import com.siigna.util.geom.Vector2D
import com.siigna.app.view.View
import com.siigna._
import app.model.shape.ArcShape
import app.model.shape.LineShape
import app.model.shape.PolylineShape
import app.model.shape.TextShape

/**
 * A menu for viewing and configuring [[com.siigna.module.ModulePackage]]s.
 */

object ModuleMenu {

  // Colours
  val colorFrame = new Color(0.10f, 0.10f, 0.10f, 0.30f)
  val colorLogo = new Color(0.10f, 0.10f, 0.10f, 0.50f)

  //LOGO
  val frameLogo =  Iterable(LineShape(Vector2D(8.345,2),Vector2D(101.7,2)),LineShape(Vector2D(5,25.56),Vector2D(5,5.345)),LineShape(Vector2D(105,25.56),Vector2D(105,5.345)),LineShape(Vector2D(8.34,28.91),Vector2D(101.7,28.91)),ArcShape(Vector2D(8.34,5.34),3.34,180,-90),ArcShape(Vector2D(101.65,5.34),3.34,0,90),ArcShape(Vector2D(101.65,25.56),3.34,0,-90),ArcShape(Vector2D(8.34,25.56),3.34,180,90))
  val logo = Iterable(PolylineShape(Vector2D(10.24,6.938),Vector2D(12.10,12.49),Vector2D(12.67,25),Vector2D(13.52,25),Vector2D(13.65,12.49),Vector2D(15.96,6.938),Vector2D(10.24,6.938)), PolylineShape(Vector2D(18.24,6.938),Vector2D(20.10,12.49),Vector2D(20.67,25),Vector2D(21.52,25),Vector2D(21.65,12.49),Vector2D(23.96,6.938),Vector2D(18.24,6.938)))
  val logoFill = Array(Vector2D(5,5.345),Vector2D(8.345,2),Vector2D(101.7,2),Vector2D(105,5.345),Vector2D(105,25.56),Vector2D(101.7,28.91),Vector2D(8.34,28.91),Vector2D(5,25.56))

  // Colors: Changes if siigna is online
  val expandedColor = if (Siigna.isOnline) new Color(0.75f, 0.75f, 0.75f, 0.80f) else new Color(0.75f, 0.45f, 0.45f, 0.40f)
  def menuColor = if (Siigna.isOnline) new Color(0.95f, 0.95f, 0.95f, 0.90f) else new Color(0.75f, 0.45f, 0.45f, 0.40f)

  val logoFillX = logoFill.map(_.x.toInt).toArray
  val logoFillY = logoFill.map(_.y.toInt).toArray

  def isHighlighted(m : Vector2D) : Boolean = {
    val in = m.x < 110 & m.y < 30
    if (in) Siigna.setCursor(Cursors.default) else Siigna.setCursor(Cursors.crosshair)
    in
  }

  def paint (g : com.siigna.app.view.Graphics, t : TransformationMatrix) {
    //draw modules selection menu
    if (isHighlighted(View.mousePositionScreen)) {
      // Inform the user if siigna is online
      val text = if (Siigna.isOnline) {
        "online - changes will be stored automatically"
      } else {
        "offline - changes will not be stored!"
      }

      g draw TextShape("Siigna is " + text, Vector2D(120, 10), 12)
    }

    g setColor menuColor
    g.AWTGraphics.fillPolygon(logoFillX, logoFillY, logoFill.size)
    logo.foreach(s => g.draw(s.setAttribute("Color" -> colorLogo)))
    frameLogo.foreach(s => g.draw(s.setAttribute("Color" -> colorFrame)))
    g draw TextShape("SIIGNA ", Vector2D(30,6),9)
    Siigna.string("version").foreach(s => g draw TextShape(s, Vector2D(30,19),7))
  }

}