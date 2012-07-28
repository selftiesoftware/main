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

package com.siigna.module.io.dxf

import com.siigna.module.io.Importer
import com.siigna.util.logging.Log
import com.siigna.app.model.shape.Shape
import com.siigna.app.model.action.Create
import java.io.InputStream
import io.Source

/**
 * An Importer that can import .dxf files.
 */
object DXFImporter extends Importer {

  def apply(input : InputStream) {
    // Read the input stream as a regular file.
    val lines = Source.fromInputStream(input).getLines()

    var dxfValues = DXFSection(List())
    var dxfSections = List[DXFSection]()

    while (lines.hasNext) {
      // Get the values
      try {
        val id = lines.next().trim
        val value = lines.next().trim

        // Add the DXF-value
        try {
          dxfValues += DXFValue(id.toInt, value)
        } catch {
          case e => Log.error("DXFImport: Failed to convert id '" + id + "' to Integer in (" + id + " -> " + value + "). Line skipped.")
        }
      } catch {
        case e => Log.error("DXFImport: Unspecified error encountered. Line skipped.")
      }
    }

    var index : Int = 0
    var sectionStartIndex : Option[Int] = None
    dxfValues.values.par.foreach((value: DXFValue) => {
      if (value.a == 0 && sectionStartIndex.isDefined && value.b != "VERTEX") {
        // Exclude the Rhino vertex, since we need it in the Polyline section.
        dxfSections = dxfSections :+ DXFSection(dxfValues.values.slice(sectionStartIndex.get, index))
        sectionStartIndex = Some(index)
      } else if (value.a == 0 && value.b != "VERTEX") {
        sectionStartIndex = Some(index)
      }
      index += 1
    })

    // Retrieve the shapes
    val shapes : Seq[Shape] = dxfSections.map(_.toShape).collect{case Some(shape : Shape) => shape}

    // Create the shapes
    Create(shapes)
  }

  def extension = "dxf"
}
