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
  
  def getParametersString(parameterName: String) = {
    var parameter: Option[String] = None
    if (applet.isDefined) {
      //Hvis appletten ikke er startet fra hjemmesiden kan der ikke hentes brugerid herfra - 1 indsættes.
      try { (parameter = Some(applet.get.getParameter(parameterName)))
      } catch {
        case e: java.lang.NullPointerException => {
          println("No contributor logged in at Siigna.com homepage. Setting user to Anonymous. If you are connected to the internet, you may log into the Siigna applet from the menu.")
          parameter = Some ("anonymous")
        }
      }
    } else {
      println ("AppletParameters ved ikke hvilken applet der skla bruges - kald medoden setApplet")
    }
    (parameter)
  }

  def getParametersInt(parameterName: String) = {
    var parameter: Option[Int] = None
    if (applet.isDefined) {
      //Hvis appletten ikke er startet fra hjemmesiden kan der ikke hentes brugerid herfra - 1 indsættes.
      try { (parameter = Some(applet.get.getParameter(parameterName).toInt))
      } catch {
        case e: java.lang.NullPointerException => {
          println("No drawing Id provided from homepage.")
        }
      }
    } else {
      println ("AppletParameters ved ikke hvilken applet der skla bruges - kald medoden setApplet")
    }
    (parameter)
  }

}
