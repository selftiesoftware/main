package com.siigna.app.controller.remote

import java.net.{ConnectException, HttpURLConnection, URL}
import java.io.ByteArrayOutputStream
import com.siigna.util.Log


/**
 * Class used for communicating with the siigna HTTP server
 */
class Connection(val url: String) {

  /**
   * The destination of the requests.
   */
  val destination = new URL(url)

  /**
   * Attempts to open a URL connection
   * @return Some[HttpURLConnection] if successful, None otherwise
   */
  def open : Option[HttpURLConnection] =
    try {
      val conn = destination.openConnection.asInstanceOf[HttpURLConnection]
      conn.setConnectTimeout(5000)
      Some(conn)
    } catch {
      case e : Throwable => None
    }

  def respondAndClose(con:HttpURLConnection) : Option[Array[Byte]] = {
    try {
      if (con.getResponseCode == 200) {
        val resp = Connection.decode(con.getInputStream)
        con.disconnect()
        Some(resp)
      } else {
        val resp = Connection.decode(con.getErrorStream)
        con.disconnect()
        Some(resp)
      }
    } catch {
      case e : ConnectException => {
        Thread.sleep(2000) // Avoid spamming retries
        None
      }
      case e : Throwable => None
    }
  }

  def get : Option[Array[Byte]] = {
    open.flatMap( con => {
      con.setDoInput(true)
      con.setUseCaches(false)

      respondAndClose(con)
    })
  }

  def post(message:Array[Byte]) : Option[Array[Byte]] = send(message,"POST")

  def put(message:Array[Byte]) : Option[Array[Byte]] = send(message,"PUT")

  def send(message:Array[Byte], method:String="POST") : Option[Array[Byte]] = {
    open.flatMap( con => {
      con.setDoInput(true)
      con.setDoOutput(true)
      con.setUseCaches(false)

      con.setRequestMethod(method)

      val out = con.getOutputStream

      try {
        out.write(message)
        out.flush

        respondAndClose(con)

      } catch {
        case e : Throwable => None
      } finally {
        out.close()
      }
    })
  }

}

object Connection{

  def decode(stream: java.io.InputStream):Array[Byte] = {

    try {
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

}
