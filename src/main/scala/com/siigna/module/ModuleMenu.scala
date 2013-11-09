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
  //val frameLogo =   Iterable( LineShape(Vector2D(10,2),Vector2D(300,2)),LineShape(Vector2D(5,24),Vector2D(5,7)),LineShape(Vector2D(10,29),Vector2D(300,29)),LineShape(Vector2D(305,24),Vector2D(305,7)),ArcShape(Vector2D(305,24),Vector2D(303.5,27.54),Vector2D(300,29)),ArcShape(Vector2D(300,2),Vector2D(303.5,3.46),Vector2D(305,7)),ArcShape(Vector2D(5,7),Vector2D(6.464,3.464),Vector2D(10,2)),ArcShape(Vector2D(5,24),Vector2D(6.464,27.54),Vector2D(10,29)))
  val frameLogo =   Iterable( LineShape(Vector2D(10,2),Vector2D(300,2)),LineShape(Vector2D(5,24),Vector2D(5,7)),LineShape(Vector2D(10,29),Vector2D(300,29)),LineShape(Vector2D(305,24),Vector2D(305,7)),LineShape(Vector2D(305,24),Vector2D(300,29)),LineShape(Vector2D(300,2),Vector2D(305,7)),LineShape(Vector2D(5,7),Vector2D(10,2)),LineShape(Vector2D(5,24),Vector2D(10,29)))

  val logo = Iterable(PolylineShape(Vector2D(10.24,6.938),Vector2D(12.10,12.49),Vector2D(12.67,25),Vector2D(13.52,25),Vector2D(13.65,12.49),Vector2D(15.96,6.938),Vector2D(10.24,6.938)), PolylineShape(Vector2D(18.24,6.938),Vector2D(20.10,12.49),Vector2D(20.67,25),Vector2D(21.52,25),Vector2D(21.65,12.49),Vector2D(23.96,6.938),Vector2D(18.24,6.938)))
  val logoFill = Array(Vector2D(10,2),Vector2D(6.464,3.464),Vector2D(5,7),Vector2D(5,24),Vector2D(6.464,27.54),Vector2D(10,29),Vector2D(300,29),Vector2D(303.5,27.54),Vector2D(305,24),Vector2D(305,7),Vector2D(303.5,3.464),Vector2D(300,2),Vector2D(10,2))

  // Colors: Changes if siigna is online
  val expandedColor = if (Siigna.isOnline) new Color(0.75f, 0.75f, 0.75f, 0.80f) else new Color(0.75f, 0.45f, 0.45f, 0.40f)
  def menuColor = if (Siigna.isOnline) new Color(0.95f, 0.95f, 0.95f, 0.90f) else new Color(0.75f, 0.45f, 0.45f, 0.40f)

  val logoFillX = logoFill.map(_.x.toInt).toArray
  val logoFillY = logoFill.map(_.y.toInt).toArray

  val isSyncing : TextShape = TextShape("saving changes to server...",Vector2D(120,9),11).addAttribute("Color" -> new Color(0.95f, 0.12f, 0.30f, 1.00f))
  val isSynced : TextShape = TextShape("all changes saved",Vector2D(130,9),12).addAttribute(("Color" -> new Color(0.10f, 0.95f, 0.10f, 1.00f)))
  val isOffline : TextShape = TextShape("offline - changes will not be stored!", Vector2D(130, 9), 10)

  def isHighlighted(m : Vector2D) : Boolean = {
    val in = m.x < 290 & m.y < 30
    //if (in) Siigna.setCursor(Cursors.default) else Siigna.setCursor(Cursors.crosshair)
    in
  }

  def paint (g : com.siigna.app.view.Graphics, t : TransformationMatrix) {


    g setColor menuColor
    g.AWTGraphics.fillPolygon(logoFillX, logoFillY, logoFill.size)

    //draw online and sync feedback
    if(Siigna.isOnline) if(Siigna.isSyncronizing) g draw isSyncing else g draw isSynced
    if (!Siigna.isOnline) g draw isOffline


    logo.foreach(s => g.draw(s.setAttribute("Color" -> colorLogo)))
    frameLogo.foreach(s => g.draw(s.setAttribute("Color" -> colorFrame)))
    g draw TextShape("SIIGNA ", Vector2D(30,6),9)
    Siigna.string("version").foreach(s => g draw TextShape(s, Vector2D(30,19),7))
  }

}