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

package com.siigna.app.controller

import java.applet.Applet

object AppletParameters {

  private var applet : Option[Applet] = None
  var clientReference: Option[Client] = None

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

  def getClient = {
      (clientReference.get)
  }

  def setClient(newClient:Option[Client]) {
    clientReference = newClient
  }
}