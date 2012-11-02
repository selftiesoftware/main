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

package com.siigna.module.io

import java.awt._

import com.siigna.app.model.shape._
import com.siigna.app.Siigna
import com.siigna.util.geom.TransformationMatrix
import com.siigna.util.geom.Vector2D
import com.siigna.app.view.{Interface, View, Graphics}
import com.siigna.module.{ModulePackage, ModuleLoader}
import event._
import com.siigna.util.logging.Log

object ModuleMenu {

  /**
   * A boolean flag to indicate whether the menu is active or not.
   */
  protected var highlighted = false

  //LOGO
  val frameLogo = Iterable(LineShape(Vector2D(8.345,2),Vector2D(25.56,2)),LineShape(Vector2D(5,25.56),Vector2D(5,5.345)),LineShape(Vector2D(30,25.56),Vector2D(30,5.345)),LineShape(Vector2D(8.34,28.91),Vector2D(26.7,28.91)),ArcShape(Vector2D(8.34,5.34),3.34,180,-90),ArcShape(Vector2D(26.65,5.34),3.34,0,90),ArcShape(Vector2D(26.65,25.56),3.34,0,-90),ArcShape(Vector2D(8.34,25.56),3.34,180,90))
  val logo = Iterable(PolylineShape(Vector2D(10.24,6.938),Vector2D(12.10,12.49),Vector2D(12.67,25),Vector2D(13.52,25),Vector2D(13.65,12.49),Vector2D(15.96,6.938),Vector2D(10.24,6.938)),(PolylineShape(Vector2D(18.24,6.938),Vector2D(20.10,12.49),Vector2D(20.67,25),Vector2D(21.52,25),Vector2D(21.65,12.49),Vector2D(23.96,6.938),Vector2D(18.24,6.938))))
  val logoFill = Array(Vector2D(5,5.345),Vector2D(8.345,2),Vector2D(25.56,2),Vector2D(30,5.345),Vector2D(30,25.56),Vector2D(26.7,28.91),Vector2D(8.34,28.91),Vector2D(5,25.56))

  //expanded menu - in v1.0 the current version and name of the current modules are displayed.
  //TODO: create a dynamic fly-down menu where different sets of modules can be selected
  //val frameExpanded = Iterable(LineShape(Vector2D(8.345,2),Vector2D(101.7,2)),LineShape(Vector2D(5,25.56),Vector2D(5,5.345)),LineShape(Vector2D(105,25.56),Vector2D(105,5.345)),LineShape(Vector2D(8.34,28.91),Vector2D(101.7,28.91)),LineShape(Vector2D(105,22),Vector2D(5,22)),ArcShape(Vector2D(8.34,5.34),3.34,180,-90),ArcShape(Vector2D(101.65,5.34),3.34,0,90),ArcShape(Vector2D(101.65,25.56),3.34,0,-90),ArcShape(Vector2D(8.34,25.56),3.34,180,90),LineShape(Vector2D(55,27.84),Vector2D(53.21,24.70)),LineShape(Vector2D(53.21,24.70),Vector2D(57,24.70)),LineShape(Vector2D(57,24.70),Vector2D(55,27.84)))

  //the frame without horizontal divider for flyout menu (only needed when modules changing is implemented)
  val frameExpanded = Iterable(LineShape(Vector2D(8.345,2),Vector2D(101.7,2)),LineShape(Vector2D(5,25.56),Vector2D(5,5.345)),LineShape(Vector2D(105,25.56),Vector2D(105,5.345)),LineShape(Vector2D(8.34,28.91),Vector2D(101.7,28.91)),ArcShape(Vector2D(8.34,5.34),3.34,180,-90),ArcShape(Vector2D(101.65,5.34),3.34,0,90),ArcShape(Vector2D(101.65,25.56),3.34,0,-90),ArcShape(Vector2D(8.34,25.56),3.34,180,90))

  val expandedFill = Array(Vector2D(5,5.345),Vector2D(8.345,2),Vector2D(101.7,2),Vector2D(105,5.345),Vector2D(105,25.56),Vector2D(101.7,28.91),Vector2D(8.34,28.91),Vector2D(5,25.56))


  //val frameFillTop = Array(Vector2D(5,5.345),Vector2D(5,12),Vector2D(5,12),Vector2D(105,12),Vector2D(105,12),Vector2D(105,5.345),Vector2D(105,5.345),Vector2D(101.7,2),Vector2D(101.7,2),Vector2D(8.34,2),Vector2D(8.34,2),Vector2D(5,5.345))
  //val frameFillFlyout = Array(Vector2D(5,12),Vector2D(5,25.56),Vector2D(5,25.56),Vector2D(8.34,28.91),Vector2D(8.34,28.91),Vector2D(101.7,28.91),Vector2D(101.7,28.91),Vector2D(105,25.56),Vector2D(105,25.56),Vector2D(105,22),Vector2D(105,22),Vector2D(5,22))

  val expandedColor = new Color(0.75f, 0.75f, 0.75f, 0.80f)
  val menuColor = new Color(0.95f, 0.95f, 0.95f, 0.90f)

  val expandedX = expandedFill.map(_.x.toInt).toArray
  val expandedY = expandedFill.map(_.y.toInt).toArray

  val logoFillX = logoFill.map(_.x.toInt).toArray
  val logoFillY = logoFill.map(_.y.toInt).toArray

  def init() {
    val f = new Frame("Siigna Module Menu")
    val l = new GridLayout()
    l.setHgap(2)
    l.setVgap(2)
    l.setColumns(3)
    l.setRows(2)
    f.setLayout(l)

    f.setLocation(View.center.toPoint)
    f.setPreferredSize(new Dimension(300, 240))
    f.setVisible(true)
    f.requestFocus()
    f.revalidate()
    f.setAlwaysOnTop(true)
    f.pack()

    f.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) {
        f.dispose()
      }
    })

    // List of module packages
    val list = new List()
    ModuleLoader.packages.foreach(p => list.add(p.name.name))
    f.add(new Label("Loaded packages"))
    f.add(list)

    // Remove module package
    val buttonRemove = new Button("Remove")
    buttonRemove.setEnabled(false)
    f.add(buttonRemove)

    list.addFocusListener(new FocusListener {
      def focusGained(e: FocusEvent) {
        val item = list.getSelectedItem
        if (item != null) {
          buttonRemove.setEnabled(true)
        }
      }

      def focusLost(e: FocusEvent) {
        buttonRemove.setEnabled(false)
      }
    })
    buttonRemove.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        val item = list.getSelectedItem
        if (item != null) {
          ModuleLoader.packages.find(_.name.name == item) match {
            case Some(pack) => {
              ModuleLoader.unload(pack)
              list.remove(item)
              Log.success("Module: Successfully removed package " + item)
            }
            case _ => Log.error("Module: Failed to remove package " + item)
          }
        }
      }
    })

    // Add module packages
    val buttonAddLocal  = new Button("Add Local Package")
    val buttonAddRemote = new Button("Add Remote Package")
    f.add(buttonAddLocal)
    f.add(buttonAddRemote)
    buttonAddRemote.setEnabled(false)

    // Add module package local
    buttonAddLocal.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        val dialog = new FileDialog(f, "Add package")
        dialog.setVisible(true)

        val dir  = dialog.getDirectory
        val file = dialog.getFile
        try {
          val name = file.replace(".jar", "")

          ModulePackage(Symbol(name), dir, file, true)
          list.remove(name)
          list.add(name)
          Log.success("Module: Successfully imported module package " + file)
        } catch {
          case _ => Log.error("Module: Import of module package " + file + "failed")
        }

        // Close dialog
        dialog.dispose()
      }
    })
  }

  def isHighlighted(m : Vector2D) : Boolean = {
    if(m.x < 30 & m.y < 30) {
      if (!highlighted) View.setCursor(Interface.Cursors.hand)
      highlighted = true
      true
    }
    else {
      if (highlighted) View.setCursor(Interface.Cursors.crosshair)
      highlighted = false
      false
    }
  }

  def paint (g : Graphics, t : TransformationMatrix)= {
    val highlight = isHighlighted(View.mousePosition)

    if(highlight == true) {
      g setColor expandedColor
      g.g.fillPolygon(expandedX, expandedY, expandedFill.size)

      frameExpanded.foreach(s => g.draw(s.setAttributes("Color" -> new Color(0.10f, 0.10f, 0.10f, 0.30f))))
      //g draw TextShape("BASE MODULES",Vector2D(16,8),9)
      g draw TextShape("SIIGNA "+Siigna.version, Vector2D(11,6),7)
    } else {
      g setColor menuColor
      g.g.fillPolygon(logoFillX, logoFillY, logoFill.size)
      logo.foreach(s => g.draw(s.setAttributes("Color" -> new Color(0.10f, 0.10f, 0.10f, 0.50f))))
      frameLogo.foreach(s => g.draw(s.setAttributes("Color" -> new Color(0.10f, 0.10f, 0.10f, 0.30f))))

    }
  }
}
