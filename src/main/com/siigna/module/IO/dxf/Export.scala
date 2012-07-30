package com.siigna.module.IO.dxf

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

/* 2010 (C) Copyright by Siigna, all rights reserved. */

import com.siigna.module.IO.dxf._
import java.awt.{FileDialog, Frame}
import java.io.{BufferedWriter, FileWriter}
import com.siigna._
import java.security._

object Export extends Module {

  lazy val eventHandler = EventHandler(stateMap, stateMachine)
  private var frameIsLoaded: Boolean = false
  lazy val stateMap = DirectedGraph('Start -> 'KeyEscape -> 'End)

  lazy val stateMachine = Map(
    'Start -> ((events: List[Event]) => {
      //a hack to prevent the dialog from opening twice
      if (frameIsLoaded == false) {
        try {
          frameIsLoaded = true
          val frame = new Frame()
          val dialog = new FileDialog(frame, "Export to file", FileDialog.SAVE)
          dialog.setVisible(true)

          val directory = dialog.getDirectory
          val filename = dialog.getFile
          //val filetype = "dxf"
          val filetype = filename.substring(filename.lastIndexOf('.') + 1, filename.length());

          filetype match {
            case "dxf" => {
              println("in export case")
              println("filename: " + filename)
              println("dir: " + directory)
              exportToDXF(filename, directory)
              Siigna display "export complete"
            }
            case "" => {
              // TODO: Change default?
              //TODO: file extension does not show in the dialog???
              exportToDXF(filename + ".dxf", directory)
              Siigna display "No fileextension found. Exporting DXF as default to " + filename + ".dxf."
            }
            case _ => Siigna display "Please type the filename WITH extension. Eg. Export.dxf"
          }

          dialog.dispose
          frame.dispose

        } catch {
          case e => Siigna display "Export cancelled."
        }
      }
      Goto('End)
      None
    }),
    'End -> ((events: List[Event]) => {
      frameIsLoaded = false
    })
  )

  //AccessController.doPrivileged(new PrivilegedAction() {
  def exportToDXF(filename: String, directory: String) = {
    val dxf = new DXFFile

    println("dir :" + directory)
    println("name: " + filename)

    //problems here... maybe PrintWriter is better???
    val writer = new FileWriter(directory + filename)
    //should yield java.io.FileWriter@27bbf6b4. Is not evaluated???
    println("writer: " + writer)

    val file = new BufferedWriter(writer)

    dxf ++ Drawing.map(t => DXFSection.toDXF(t._2)).toSeq

    file.write(dxf.toString)
    file.flush
    file.close
  }

  //})
}