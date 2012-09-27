package com.siigna.module.io

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

import java.awt.{Color, FileDialog, Frame}
import com.siigna._
import java.io.{FileInputStream, File}

/**
 * Imports data from files into Siigna using [[com.siigna.module.io.Importer]] objects.
 */
object Import extends Module {

  lazy val anthracite = new Color(0.25f, 0.25f, 0.25f, 1.00f)

  val color = "Color" -> "#AAAAAA".color

  val frame = new Frame
  var frameIsLoaded = false

  var fileLength: Int = 0

  // A map of file extensions to a given importer
  protected var importers : Map[String, Importer] = Map()

  //graphics to show the loading progress
  def loadBar(point: Int): Shape = PolylineShape(Rectangle2D(Vector2D(103, 297), Vector2D(point + 103, 303))).setAttribute("raster" -> anthracite)

  def loadFrame: Shape = PolylineShape(Rectangle2D(Vector2D(100, 294), Vector2D(500, 306))).setAttribute(color)

  private var startTime: Option[Long] = None

  def stateMap : StateMap = Map(
    'Start -> {
      case _ => {
        if (frameIsLoaded == false) {
          try {
            //opens a file dialog
            val dialog = new FileDialog(frame)
            dialog.setVisible(true)

            val fileName = dialog.getFile
            val fileDir = dialog.getDirectory
            val file = new File(fileDir + fileName)

            // Can we import the file-type?
            val extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase

            if (importers.contains(extension)) {
              startTime = Some(System.currentTimeMillis().toLong)

              //TODO: find the correct scaling factor to make loading bar fit large DXF files.
              fileLength = file.length().toInt * 4

              Siigna display "Loading file... Please wait."

              // Retrieve the streams for the file
              val fileStream = new FileInputStream(file)

              // Import!
              importers(extension)(fileStream)

              // Close the stream
              fileStream.close()

              Siigna display "Loading completed."
              frameIsLoaded = true
            } else {
              Log.error("Import: Unable to import file of the specified type: " + extension)
            }
          } catch {
            case e => {
              Siigna display "Import cancelled."
            }
          }
        }
        'End
      }
    },
    // Dispose of the frame so the thread can close down.
    'End -> {
      case _ => {
        fileLength = 0
        frameIsLoaded = false
        frame.dispose()
        startTime = None
        'End
      }
    }
  )

  /**
   * Adds a hook that uses the file-extension in the given Importer to match known file types, so they
   * can be imported using the apply-method in the Importer itself. Overrides any hooks defined on that
   * given extension.
   * @param importer  The importer containing the file extension and the algorithms to import a given file-type.
   */
  def addImporter(importer : Importer) {
    importers = importers + (importer.extension.toLowerCase -> importer)
  }

  //draw a loading bar
  override def paint(g: Graphics, t: TransformationMatrix) {
    g draw loadFrame
    if (fileLength > 0 && ((System.currentTimeMillis() - startTime.get) / (fileLength / 30000)) < 394) {
      g draw loadBar(((System.currentTimeMillis() - startTime.get) / (fileLength / 30000)).toInt)
    } else if (fileLength > 0 && ((System.currentTimeMillis() - startTime.get) / (fileLength / 30000)) > 394)
      g draw loadBar(390)

  }
}