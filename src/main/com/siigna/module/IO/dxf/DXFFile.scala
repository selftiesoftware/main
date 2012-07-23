package com.siigna.module.IO.dxf

//import com.siigna.module.base.file.fileformats.dxf.DXFSection.apply
//import com.siigna.module.base.file.fileformats.dxf.DXFSection.apply
//import com.siigna.module.IO.dxf.{DXFSection, DXFValue}

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

class DXFFile {

  var header = "0\r\nSECTION\r\n2\r\nHEADER\r\n9\r\n$ACADVER\r\n1\r\nAC1012\r\n9\r\n$HANDSEED\r\n5\r\n128\r\n9\r\n$INSUNITS\r\n70\r\n4\r\n9\r\n$LIMMIN\r\n10\r\n0.0\r\n20\r\n-200.0\r\n9\r\n$LIMMAX\r\n10\r\n350.0\r\n20\r\n0.0\r\n0\r\nENDSEC\r\n0\r\nSECTION\r\n2\r\nTABLES\r\n0\r\nTABLE\r\n2\r\nVPORT\r\n5\r\n1\r\n100\r\nAcDbSymbolTable\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nLTYPE\r\n5\r\n2\r\n100\r\nAcDbSymbolTable\r\n0\r\nLTYPE\r\n5\r\n3\r\n100\r\nAcDbSymbolTableRecord\r\n100\r\nAcDbLinetypeTableRecord\r\n2\r\nBYBLOCK\r\n70\r\n0\r\n0\r\nLTYPE\r\n5\r\n4\r\n100\r\nAcDbSymbolTableRecord\r\n100\r\nAcDbLinetypeTableRecord\r\n2\r\nBYLAYER\r\n70\r\n0\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nLAYER\r\n5\r\n1A\r\n330\r\n0\r\n100\r\nAcDbSymbolTable\r\n0\r\nLAYER\r\n5\r\n39\r\n330\r\n1A\r\n100\r\nAcDbSymbolTableRecord\r\n100\r\nAcDbLayerTableRecord\r\n2\r\n0\r\n70\r\n0\r\n62\r\n7\r\n370\r\n-3\r\n390\r\n19\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nSTYLE\r\n5\r\n5\r\n100\r\nAcDbSymbolTable\r\n0\r\nSTYLE\r\n5\r\n6\r\n100\r\nAcDbSymbolTableRecord\r\n100\r\nAcDbTextStyleTableRecord\r\n2\r\nSTANDARD\r\n70\r\n0\r\n40\r\n0.0\r\n41\r\n1.0\r\n50\r\n0.0\r\n71\r\n0\r\n42\r\n10.0\r\n3\r\ntxt\r\n4\r\nbigfont\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nVIEW\r\n5\r\n7\r\n100\r\nAcDbSymbolTable\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nUCS\r\n5\r\n8\r\n100\r\nAcDbSymbolTable\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nAPPID\r\n5\r\n9\r\n100\r\nAcDbSymbolTable\r\n0\r\nAPPID\r\n5\r\nA\r\n100\r\nAcDbSymbolTableRecord\r\n100\r\nAcDbRegAppTableRecord\r\n2\r\nACAD\r\n70\r\n0\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nDIMSTYLE\r\n5\r\nB\r\n100\r\nAcDbSymbolTable\r\n70\r\n1\r\n100\r\nAcDbDimStyleTable\r\n71\r\n0\r\n0\r\nENDTAB\r\n0\r\nTABLE\r\n2\r\nBLOCK_RECORD\r\n5\r\nC\r\n100\r\nAcDbSymbolTable\r\n0\r\nBLOCK_RECORD\r\n5\r\nD\r\n100\r\nAcDbSymbolTableRecord\r\n100\r\nAcDbBlockTableRecord\r\n2\r\n*MODEL_SPACE\r\n0\r\nBLOCK_RECORD\r\n5\r\nE\r\n100\r\nAcDbSymbolTableRecord\r\n100\r\nAcDbBlockTableRecord\r\n2\r\n*PAPER_SPACE\r\n0\r\nENDTAB\r\n0\r\nENDSEC"
  var entities: DXFSection = DXFSection(DXFValue(0, "SECTION"), DXFValue(2, "ENTITIES"))
  var content: Seq[DXFSection] = Seq()
  var footer: DXFSection = DXFSection(DXFValue(0, "ENDSEC"), DXFValue(0, "EOF"))

  def +(section: DXFSection) = content = content.:+(section)

  def ++(sections: Seq[DXFSection]) = content = content ++ sections

  override def toString = header.toString + entities.toString + content.mkString + footer.toString

}
