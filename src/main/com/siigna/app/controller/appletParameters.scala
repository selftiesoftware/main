package com.siigna.app.controller

import java.applet.Applet

/**
 * Created by IntelliJ IDEA.
 * User: Niels Egholm
 * Date: 07-04-12
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */

//new AppletPreference(this )

object AppletParameters {

  private var applet : Option[Applet] = None

  def setApplet (newApplet: Applet) { 
    applet = Some(newApplet) 
  }
  
  def getParametersInt(parameterName: Option[String]) = {
    var parameter: Option[Int] = None
    if (applet.isDefined) {
    parameter = Some(applet.get.getParameter(parameterName.get).toInt)
    (parameter)
    } else {
      println ("AppletParameters ved ikke hvilken applet der skla bruges - kald medoden setApplet")
    }
    (parameter)
  }
}
