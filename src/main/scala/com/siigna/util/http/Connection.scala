package com.siigna.util.http

import java.net.{HttpURLConnection, URL}

//

/**
 * Class used for communicating with the siigna HTTP server
 */
class Connection(val url: String) {

  val destination = new URL(url)

  def send(message:Array[Byte]) = {
    val con = destination.openConnection.asInstanceOf[HttpURLConnection]

    con.setDoInput(true)
    con.setDoOutput(true)
    con.setUseCaches(false)

    val out = con.getOutputStream

    try{
      out.write(message)
      out.flush
    } finally {
      out.close
    }

    if (con.getResponseCode == 200)
      con.getInputStream
    else
      con.getErrorStream
  }


  def decode(stream: java.io.InputStream):Array[Byte] = {

    var in = Array[Byte]()

    var data = stream.read(in).asInstanceOf[Byte]

    while( data != -1){
      in = data +: in
      data = stream.read().asInstanceOf[Byte]
    }

    return in
  }

}
