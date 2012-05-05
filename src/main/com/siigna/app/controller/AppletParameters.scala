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
import remote.{GetNewShapeIds, GetNewShapeId}
import com.siigna.app.model._
import com.siigna.app.controller.remote.SaveShape
object AppletParameters {

  private var drawingId: Option[Int] = None
  var drawingName: Option[String] = None
  var contributorName: Option[String] = None
  private var applet : Option[Applet] = None
  var clientReference: Option[Client] = None
  var shapeIdBank: Seq[Int] = Seq()
  var drawingIdBank: Seq[Int] = Seq()
  var drawingIdReceivedAtStartup: Boolean = false
  var drawingOwner: Option[String] = None
  var localShapeId:Int = 0
  var shapesWithLocalShapeId: Seq[Int] = Seq()
  
  def getApplet = {
    (applet.get)
  }

  /**
   * Returnerer Client, der er gemt i variablen clientReference
   * @return
   */
  def getClient = {
    (clientReference.get)
  }

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


  /**
   * Hvis der er od'er i "banken: Returnerer en shapeId fra "banken". 
   * Hvis der ikke er gives et "lokalt id", og dette føjes til listen over shapes med lokale id'er.
   * Kontrollerer, hvor mange id'er, der er tilbage i banken.
   * Hvis der er under et vist antal anmodes om "en ny portion".
   * @return
   */
  def getNewShapeId = {
    var shapeId:Int = 0
    
    if (shapeIdBank.length > 0) {
      shapeId = shapeIdBank.head
      shapeIdBank = shapeIdBank.tail
    } else {
      shapeId = localShapeId
      shapesWithLocalShapeId = shapesWithLocalShapeId :+ localShapeId
      localShapeId = localShapeId + 1
    }
    if (shapeIdBank.length<2) GetNewShapeIds(2,com.siigna.app.controller.AppletParameters.getClient)
    (shapeId)
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
   * Saves a new shapeId into the shapeIdBank variable
   * @param shapeId
   */
  def receiveNewShapeId(shapeId:Int) {
    println("Shapes with local Ids are now: " + shapesWithLocalShapeId)
    shapeIdBank = shapeIdBank :+ shapeId
    println("shapeIdBank is now: "+shapeIdBank)
    shapesWithLocalShapeId.foreach(localId => {
      if (shapeIdBank.length > 0){
        //Funktion, der opdaterer lokalt id med id fra serveren. Mangler metode...

        //Funktion, der gemmer og videresender de shapes, der nu har fået en global id
        //SaveShape(drawingId.get,shapeIdFromBank,shape,clientReference.get)
      } 
    })
    println("shapeIdBank is now, after replacement of local Ids: "+shapeIdBank)
    println("Shapes with local Ids are now: " + shapesWithLocalShapeId)
    if (shapeIdBank.length<5) GetNewShapeIds(10,com.siigna.app.controller.AppletParameters.getClient)
  }

  /**
   * Saves a sequence of new shapeIds into the shapeIdBank variable
   * @param shapeIds
   */
  def receiveNewShapeIds(shapeIds:Seq[Int]) {
    println("Shapes with local Ids are now: " + shapesWithLocalShapeId)
    shapeIdBank = shapeIdBank ++ shapeIds
    println("shapeIdBank is now: "+shapeIdBank)
    shapesWithLocalShapeId.foreach(localId => {
      if (shapeIdBank.length > 0){
        //Funktion, der opdaterer lokalt id med id fra serveren. Mangler metode...

        //Funktion, der gemmer og videresender de shapes, der nu har fået en global id
        //SaveShape(drawingId.get,shapeIdFromBank,shape,clientReference.get)
      }
    })
    println("shapeIdBank is now, after replacement of local Ids: "+shapeIdBank)
    println("Shapes with local Ids are now: " + shapesWithLocalShapeId)
    if (shapeIdBank.length<5) GetNewShapeIds(10,com.siigna.app.controller.AppletParameters.getClient)
  }

  /**
   * Saves a new name for the active drawing, ans sets the drawingNamw var to the new name.
   * @param newName
   */
  def saveNewDrawingName(newName: String) = {
    var messageReturned: Option[String] = None
    if(drawingId.isDefined && clientReference.isDefined) {
      com.siigna.app.controller.remote.SaveDrawingName(drawingId.get,newName,AppletParameters.clientReference.get)
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
      remote.RegisterWithNewDrawingId(drawingId.get,newId,clientReference.get)
    } else {
      remote.RegisterWithDrawingId(newId,clientReference.get)
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
    clientReference = newClient
  }
}