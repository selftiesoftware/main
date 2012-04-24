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

object AppletParameters {

  private var applet : Option[Applet] = None
  var clientReference: Option[Client] = None
  var shapeIdBank: Seq[Int] = Seq()
  var drawingIdBank: Seq[Int] = Seq()

  /**
   * Returnerer Client, der er gemt i variablen clientReference
   * @return
   */
  def getClient = {
    (clientReference.get)
  }

  /**
   * Returnerer en shapeId fra "banken". Kontrollerer, hvor mange id'er, der er tilbage i banken.
   * Hvis der er under et vist antal anmodes om "en ny portion".
   * @return
   */
  def getNewShapeId = {
    val shapeId = shapeIdBank.head
    shapeIdBank = shapeIdBank.tail
    if (shapeIdBank.length<2) GetNewShapeIds(2,com.siigna.app.controller.AppletParameters.getClient)
    (shapeId)
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
   * @param parameterName
   * @return
   */
  def getParametersInt(parameterName: String) = {
    var parameter: Option[Int] = None
    if (applet.isDefined) {
      //Hvis appletten ikke er startet fra hjemmesiden kan der ikke hentes brugerid herfra - 1 indsættes.
      try { (parameter = Some(applet.get.getParameter(parameterName).toInt))
      } catch {
        case e: java.lang.NullPointerException => {
          println("No drawing Id provided from homepage. Setting drawingId to 1.")
          parameter = Some(1)
        }
      }
    } else {
      println ("AppletParameters ved ikke hvilken applet der skla bruges - kald medoden setApplet")
    }
    (parameter)
  }

  /**
   * Saves a new shapeId into the shapeIdBank variable
   * @param shapeId
   */
  def receiveNewShapeId(shapeId:Int) {
    shapeIdBank = shapeIdBank :+ shapeId
    println("shapeIdBank is now: "+shapeIdBank)
  }

  /**
   * Saves a sequence of new shapeIds into the shapeIdBank variable
   * @param shapeIds
   */
  def receiveNewShapeIds(shapeIds:Seq[Int]) {
    shapeIdBank = shapeIdBank ++ shapeIds
    println("shapeIdBank is now: "+shapeIdBank)
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