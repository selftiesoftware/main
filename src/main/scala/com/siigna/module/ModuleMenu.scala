/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

/*
package com.siigna.module

import java.awt._

import com.siigna.app.Siigna
import com.siigna.util.geom.Vector2D
import com.siigna.app.view.{Interface, View}
import com.siigna._
import app.model.shape.ArcShape
import app.model.shape.LineShape
import app.model.shape.PolylineShape
import app.model.shape.TextShape

/**
 * A menu for viewing and configuring [[com.siigna.module.ModulePackage]]s.
 */

object ModuleMenu {
  /**
   * A boolean flag to indicate whether the menu is active or not.
   */
  protected var highlighted = false
  def modules = ModuleLoader.packages.toList

  // Colours
  val colorActive = new Color(0.10f, 0.10f, 0.10f, 1f)
  val colorFrame = new Color(0.10f, 0.10f, 0.10f, 0.30f)
  val colorInactive = new Color(0.10f, 0.10f, 0.10f, 0.90f)
  val colorLogo = new Color(0.10f, 0.10f, 0.10f, 0.50f)

  //LOGO
  val frameLogo =  Iterable(LineShape(Vector2D(8.345,2),Vector2D(101.7,2)),LineShape(Vector2D(5,25.56),Vector2D(5,5.345)),LineShape(Vector2D(105,25.56),Vector2D(105,5.345)),LineShape(Vector2D(8.34,28.91),Vector2D(101.7,28.91)),ArcShape(Vector2D(8.34,5.34),3.34,180,-90),ArcShape(Vector2D(101.65,5.34),3.34,0,90),ArcShape(Vector2D(101.65,25.56),3.34,0,-90),ArcShape(Vector2D(8.34,25.56),3.34,180,90))
  val logo = Iterable(PolylineShape(Vector2D(10.24,6.938),Vector2D(12.10,12.49),Vector2D(12.67,25),Vector2D(13.52,25),Vector2D(13.65,12.49),Vector2D(15.96,6.938),Vector2D(10.24,6.938)),(PolylineShape(Vector2D(18.24,6.938),Vector2D(20.10,12.49),Vector2D(20.67,25),Vector2D(21.52,25),Vector2D(21.65,12.49),Vector2D(23.96,6.938),Vector2D(18.24,6.938))))
  val logoFill = Array(Vector2D(5,5.345),Vector2D(8.345,2),Vector2D(101.7,2),Vector2D(105,5.345),Vector2D(105,25.56),Vector2D(101.7,28.91),Vector2D(8.34,28.91),Vector2D(5,25.56))

  //the frame without horizontal divider for flyout menu (only needed when modules changing is implemented)
  val frameExpanded = Iterable(LineShape(Vector2D(8.345,2),Vector2D(101.7,2)),LineShape(Vector2D(5,225.56),Vector2D(5,5.345)),LineShape(Vector2D(105,225.56),Vector2D(105,5.345)),LineShape(Vector2D(8.34,228.91),Vector2D(101.7,228.91)),ArcShape(Vector2D(8.34,5.34),3.34,180,-90),ArcShape(Vector2D(101.65,5.34),3.34,0,90),ArcShape(Vector2D(101.65,225.56),3.34,0,-90),ArcShape(Vector2D(8.34,225.56),3.34,180,90))

  val expandedFill = Array(Vector2D(5,5.345),Vector2D(8.345,2),Vector2D(101.7,2),Vector2D(105,5.345),Vector2D(105,225.56),Vector2D(101.7,228.91),Vector2D(8.34,228.91),Vector2D(5,225.56))

  val expandedColor = new Color(0.75f, 0.75f, 0.75f, 0.80f)
  val menuColor = new Color(0.95f, 0.95f, 0.95f, 0.90f)

  val expandedX = expandedFill.map(_.x.toInt).toArray
  val expandedY = expandedFill.map(_.y.toInt).toArray

  val logoFillX = logoFill.map(_.x.toInt).toArray
  val logoFillY = logoFill.map(_.y.toInt).toArray

  var isOpen = false

  // Load the package
  //ModuleLoader load ModulePackage(Symbol(name), dir, file, true)

  def isHighlighted(m : Vector2D) : Boolean = {
    if(m.x < 110 & m.y < 30) {
      View.setCursor(Interface.Cursors.hand)
      highlighted = true
      isOpen = true
    }
    else if(m.x > 110 & m.y < 350) {
      View.setCursor(Interface.Cursors.crosshair)
      highlighted = false
      isOpen = false
    }
    isOpen
  }

  /**
   * Triggered when a mouse down event is detected above the module menu.
   * @param point  The point over the module menu.
   */
  def onMouseDown(point : Vector2D) {
    Log.info("ModuleMenu got point " + point)
  }
   /*
  def paint (g : com.siigna.app.view.Graphics, t : TransformationMatrix) {
    val highlight = isHighlighted(View.mousePosition)

    //draw modules selection menu
    if(highlight == true) {

      // Retrieves a text shape with some text in the given position with the given color
      def moduleString(s : String, pos : Int, color : Color) = {
        TextShape(s,Vector2D(10,pos),10).setAttribute("Color" -> color)
      }

      // Draw frame & background
      g setColor expandedColor
      g.g.fillPolygon(expandedX, expandedY, expandedFill.size)
      frameExpanded.foreach(s => g.draw(s.setAttribute("Color" -> colorFrame)))
      g draw TextShape("SIIGNA ", Vector2D(11,6),9)
      g draw TextShape(Siigna.version, Vector2D(11,19),7)

      // spacer
      g draw LineShape(Vector2D(10,30),Vector2D(100,30))

      // the loaded modules
      for (i <- 0 until modules.size) {
        g draw moduleString(modules(i).name, i * 20 + 40, colorActive)
      }

    } else {
      g setColor menuColor
      g.g.fillPolygon(logoFillX, logoFillY, logoFill.size)
      logo.foreach(s => g.draw(s.setAttribute("Color" -> colorLogo)))
      frameLogo.foreach(s => g.draw(s.setAttribute("Color" -> colorFrame)))
    }

    // Draw the currently active module package
    modules.headOption.foreach(p => g draw TextShape(p.name,Vector2D(56,6),8).setAttribute("Color" -> colorActive))
  }
  */
}
*/