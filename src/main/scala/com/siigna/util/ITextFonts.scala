package com.siigna.util

import com.itextpdf.text.pdf.BaseFont

/**
 * A helper object to load ITextFonts.
 */
object ITextFonts {

  val helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED)
  //getClass.getClassLoader.getResourceAsStream("com/itextpdf/text/pdf/fonts/Helvetica.afm")

}
