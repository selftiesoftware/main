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

package com.siigna.util.dxf

class DXFFile {

  var header = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1015\n9\n$HANDSEED\n5\n46\n9\n$INSUNITS\n70\n4\n9\n$LIMMIN\n10\n0.0\n20\n-200.0\n9\n$LIMMAX\n10\n350.0\n20\n0.0\n0\nENDSEC\n0\nSECTION\n2\nTABLES\n0\nTABLE\n2\nVPORT\n5\n1\n100\nAcDbSymbolTable\n0\nENDTAB\n0\nTABLE\n2\nLTYPE\n5\n2\n100\nAcDbSymbolTable\n0\nLTYPE\n5\n3\n100\nAcDbSymbolTableRecord\n100\nAcDbLinetypeTableRecord\n2\nBYBLOCK\n70\n0\n0\nLTYPE\n5\n4\n100\nAcDbSymbolTableRecord\n100\nAcDbLinetypeTableRecord\n2\nBYLAYER\n70\n0\n0\nENDTAB\n0\nTABLE\n2\nLAYER\n5\n1A\n330\n0\n100\nAcDbSymbolTable\n0\nLAYER\n5\n39\n330\n1A\n100\nAcDbSymbolTableRecord\n100\nAcDbLayerTableRecord\n2\n0\n70\n0\n62\n7\n370\n-3\n390\n19\n0\nENDTAB\n0\nTABLE\n2\nSTYLE\n5\n5\n100\nAcDbSymbolTable\n0\nSTYLE\n5\n6\n100\nAcDbSymbolTableRecord\n100\nAcDbTextStyleTableRecord\n2\nSTANDARD\n70\n0\n40\n0.0\n41\n1.0\n50\n0.0\n71\n0\n42\n10.0\n3\ntxt\n4\nbigfont\n0\nENDTAB\n0\nTABLE\n2\nVIEW\n5\n7\n100\nAcDbSymbolTable\n0\nENDTAB\n0\nTABLE\n2\nUCS\n5\n8\n100\nAcDbSymbolTable\n0\nENDTAB\n0\nTABLE\n2\nAPPID\n5\n9\n100\nAcDbSymbolTable\n0\nAPPID\n5\nA\n100\nAcDbSymbolTableRecord\n100\nAcDbRegAppTableRecord\n2\nACAD\n70\n0\n0\nENDTAB\n0\nTABLE\n2\nDIMSTYLE\n5\nB\n100\nAcDbSymbolTable\n70\n1\n100\nAcDbDimStyleTable\n71\n0\n0\nENDTAB\n0\nTABLE\n2\nBLOCK_RECORD\n5\nC\n100\nAcDbSymbolTable\n0\nBLOCK_RECORD\n5\nD\n100\nAcDbSymbolTableRecord\n100\nAcDbBlockTableRecord\n2\n*MODEL_SPACE\n0\nBLOCK_RECORD\n5\nE\n100\nAcDbSymbolTableRecord\n100\nAcDbBlockTableRecord\n2\n*PAPER_SPACE\n0\nENDTAB\n0\nENDSEC"
  var entities  : DXFSection = DXFSection(DXFValue(0, "SECTION"), DXFValue(2, "ENTITIES"))
  var content : Seq[DXFSection] = Seq()
  var footer  : DXFSection = DXFSection(DXFValue(0, "ENDSEC"), DXFValue(0, "EOF"))

  def + (section : DXFSection) = content = content.:+(section)

  def ++ (sections : Seq[DXFSection]) = content = content ++ sections

  override def toString = header.toString + entities.toString + content.mkString + footer.toString

}
