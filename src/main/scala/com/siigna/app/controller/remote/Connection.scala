package com.siigna.app.controller.remote

import java.net.{ConnectException, HttpURLConnection, URL}
import java.io.{EOFException, ByteArrayOutputStream}


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
  def open : Either[HttpURLConnection, String] =
    try {
      val conn = destination.openConnection.asInstanceOf[HttpURLConnection]
      conn.setConnectTimeout(5000)
      // Examine return code
      val code = conn.getResponseCode
      if (code >= 200 && code < 300) {
        Left(conn)
      } else {
        Right(s"Error; code $code with message: ${conn.getResponseMessage}")
      }
    } catch {
      case e : Throwable => Right("Error when opening TCP connection: " + e.getLocalizedMessage)
    }

  def respondAndClose(con:HttpURLConnection) : Either[Array[Byte], String] = {
    try {
      if (con.getResponseCode == 200) {
        val resp = Connection.decode(con.getInputStream)
        con.disconnect()
        Left(resp)
      } else {
        val resp = Connection.decode(con.getErrorStream)
        con.disconnect()
        Left(resp)
      }
    } catch {
      case e : ConnectException => {
        Thread.sleep(2000) // Avoid spamming retries
        Right("Connection aborted")
      }
      case e : EOFException => Right("Unexpected end of file from Server")
      case e : Throwable => Right("Unknown error: " + e.getLocalizedMessage)
    }
  }

  def get : Either[Array[Byte], String] = {
    open.left.flatMap( con => {
      con.setDoInput(true)
      con.setUseCaches(false)

      respondAndClose(con)
    })
  }

  def post(message:Array[Byte]) : Either[Array[Byte], String] = send(message,"POST")

  def put(message:Array[Byte]) : Either[Array[Byte], String] = send(message,"PUT")

  def send(message:Array[Byte], method:String="POST") : Either[Array[Byte], String] = {
    open.left.flatMap( con => {
      con.setDoInput(true)
      con.setDoOutput(true)
      con.setUseCaches(false)

      con.setRequestMethod(method)

      val out = con.getOutputStream

      try {
        out.write(message)
        out.flush()

        respondAndClose(con)
      } catch {
        case e : Throwable => Right("Error when writin data to stream: " + e.getLocalizedMessage)
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
        new Array[Byte](0)
      }
    }
  }

}
