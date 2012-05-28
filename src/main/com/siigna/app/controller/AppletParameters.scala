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
import remote.{GetNewShapeIds}
import com.siigna.app.model._
import com.siigna.app.controller.remote.SaveShape
object AppletParameters {

  var drawingId: Option[Int] = None
  var drawingName: Option[String] = None
  var contributorName: Option[String] = None
  var applet : Option[Applet] = None
  var client: Option[Client] = None
  var drawingIdBank: Seq[Int] = Seq()
  var drawingIdReceivedAtStartup: Boolean = false
  var drawingOwner: Option[String] = None
  
  def getApplet = applet

  /**
   * Returnerer Client, der er gemt i variablen clientReference
   * @return
   */
  def getClient = client

  def getDrawingId = {
    (drawingId)
  }
  
  /**
   * Retrieves contributorName sent with param tag from html when starting applet, and sets the variable.
   * @return
   */
  def getContributorNameFromHomepage {
    contributorName = AppletParameters.getParametersString("contributorName")
  }

  /**
   * Retrieves drawing id sent with param tag from html when starting applet, and saves this value to the variable,
   * returns the variable as option, or None if it is not set.
   * @return
   */
  def getDrawingIdFromHomepage {
    drawingId = AppletParameters.getParametersInt("drawingId")
  }
  
  def getDrawingOwnerAsOption = {
    (drawingOwner)
  }

  /**
   * Fremfinder parametre, der er strenge, der er sendt ved opstart af applet fra "param"-tags fra HTML
   * @param parameterName
   * @return
   */
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

  /**
   * Fremfinder parametre, der er integers, der er sendt ved opstart af applet fra "param"-tags fra HTML
   * Hvis intet tag med det angivne navn er sat returneres Option[None]
   * @param parameterName
   * @return
   */
  def getParametersInt(parameterName: String) = {
    var parameter: Option[Int] = None
    if (applet.isDefined) {
      //Hvis der er sendt param tag fra html med det angivne navn returneres det.
      try {
        parameter = Some(applet.get.getParameter(parameterName).toInt)
      } catch {
        case _ => parameter = None
      }
    } else {
      println ("AppletParameters ved ikke hvilken applet der skla bruges - kald medoden setApplet")
    }
    (parameter)
  }


  /**
   * Returns drawingId as option
   * @return
   */
  def readDrawingIdAsOption = {
    (drawingId)
  }

  /**
   * Returns drawingName as option
   * @return
   */
  def readDrawingNameAsOption = {
    (drawingName)
  }



  /**
   * Saves a new name for the active drawing, ans sets the drawingNamw var to the new name.
   * @param newName
   */
  def saveNewDrawingName(newName: String) = {
    var messageReturned: Option[String] = None
    if(drawingId.isDefined && client.isDefined) {
      //com.siigna.app.controller.remote.SaveDrawingName(drawingId.get,newName,AppletParameters.client.get)
      drawingName = Some(newName)
      messageReturned = Some("Drawing name changed to "+drawingName.get)
    } else {
      messageReturned = Some("No drawing Id was set. Drawing name not changed")
    }
    (messageReturned)
  }

  /**
   * Registers new drawing Id with the server and sets drawingId-variable to new drawing id.
   * @param newId
   */
  def setDrawingId(newId:Int) {
    drawingId = Some(newId)
  }

  /**
   * Registers new drawing Id with the server and sets drawingId-variable to new drawing id.
   * @param newId
   */
  def setDrawingIdAndRegisterItWithTheServer(newId:Int) {
    if(drawingId.isDefined) {
      //remote.RegisterWithNewDrawingId(drawingId.get,newId,client.get)
    } else {
      //remote.RegisterWithDrawingId(newId,client.get)
    }
    drawingId = Some(newId)
  }
  
  def setDrawingName(newDrawingName:Option[String]) {
    drawingName=newDrawingName
  }
  
  def setDrawingOwner (newDrawingOwner:String) {
    drawingOwner = Some(newDrawingOwner)
  }

  /**
   * Modtager en instans af "Applet", der har modtaget informationer
   * fra "param"-tags fra HTML, når appletten startes af HTML-kode. Bruges ved opstart af applet.
   * @param newApplet
   */
  def setApplet (newApplet: Applet) {
    applet = Some(newApplet)
  }

  /**
   * Gemmer Client i variablen clientReference
   * @param newClient
   */
  def setClient(newClient:Option[Client]) {
    client = newClient
  }
}