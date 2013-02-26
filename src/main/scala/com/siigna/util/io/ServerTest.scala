package com.siigna.util.io

/**
 * Created with IntelliJ IDEA.
 * User: JensEgholm
 * Date: 26-02-13
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
object ServerTest {

  import java.net._
  import java.io._

  def main(args : Array[String]) {
    val a = Marshal("Hello World!")
    val url = new URL("http://localhost:7788")
    val c = url.openConnection().asInstanceOf[HttpURLConnection]
    c.setRequestMethod("GET")
    c.setRequestProperty("Test", "true")
    c.setDoInput(true)
    c.setDoOutput(true)
    val out = c.getOutputStream
    val wr = new ObjectOutputStream(out)
    wr.writeUTF(new String(a, "UTF-8"))
    wr.flush()
    wr.close()

    val in = c.getInputStream

  }

}
