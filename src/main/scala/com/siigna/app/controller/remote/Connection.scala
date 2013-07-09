package com.siigna.app.controller.remote

import java.net.{HttpURLConnection, URL}
import java.io.ByteArrayOutputStream
import javax.xml.bind.DatatypeConverter
import com.siigna.util.Log


/**
 * Class used for communicating with the siigna HTTP server
 */
class Connection(val url: String) {

  val destination = new URL(url)

  def respondAndClose(con:HttpURLConnection) = {
    if (con.getResponseCode == 200) {
      val resp = Connection.decode(con.getInputStream)
      con.disconnect()
      resp
    }else {
      val resp = Connection.decode(con.getErrorStream)
      con.disconnect()
      resp
    }
  }

  def get = {
    val con = destination.openConnection.asInstanceOf[HttpURLConnection]
    con.setDoInput(true)
    con.setUseCaches(false)

    respondAndClose(con)

  }

  def post(message:Array[Byte]) = {
    send(message,"POST")
  }

  def send(message:Array[Byte],method:String="POST") = {
    val con = destination.openConnection.asInstanceOf[HttpURLConnection]

    con.setDoInput(true)
    con.setDoOutput(true)
    con.setUseCaches(false)

    con.setRequestMethod(method)

    val out = con.getOutputStream

    try{
      out.write(message)
      out.flush

      respondAndClose(con)

    } catch {
      case e : Throwable => Array[Byte]()
    } finally {
      out.close()
    }
  }

}

object Connection{

  def decode(stream: java.io.InputStream):Array[Byte] = {

    try {

      //val reader = new BufferedReader(new InputStreamReader(stream))
      //val request = reader.readLine() // Read the request line
      //request.getBytes("UTF-8")

      // val token = request.substring(6, request.size) // Cut off the initial 6 characters (do they mean anyting??)
      //      raw      parsed   unmarshalled

      val bos = new ByteArrayOutputStream()
      val buffer:Array[Byte] = new Array[Byte](4096)
      var len= 0
      while (len != -1) {
        len = stream.read(buffer)
        if (len != -1)
          bos.write(buffer, 0, len)
      }

      bos.toByteArray
    } catch {
      case e : Throwable => {
        Log.error("Fail: " + e)
        new Array[Byte](0)
      }
    }
  }

  /**
   * Mathces user and password against a base64 basic auth string
   * @param user
   * @param pass
   * @param hash A base64 encoded string of the format user:password
   */
  def auth(user: String, pass: String, hash: String):Boolean = {
    DatatypeConverter.parseBase64Binary(user+":"+pass).equals(new String(DatatypeConverter.printBase64Binary(hash.getBytes)))
  }

  /**
   * Simple function to return a map of parameters from a query string
   * @param query
   */
  def extractParameters(query: String) = {
    query.split("=")
  }

}
