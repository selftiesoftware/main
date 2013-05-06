/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.controller.remote

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{OneInstancePerTest, BeforeAndAfter, FunSpec}
import com.siigna.app.Siigna
import org.scalatest.exceptions.TestFailedException

/**
 * Tests connectivity and fault tolerance of the server.
 */
class ServerSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  var badSink : Server  = null
  var goodSink : Server = null

  lazy val dummyGet = Get(null, null, dummySession)
  lazy val dummySession = Session(0L, Siigna.user)

  before {
    badSink  = new Server("localhost", Mode.Production, 1)
    goodSink = new Server("62.243.118.234", Mode.Production)
  }

  after {
    badSink.disconnect()
    goodSink.disconnect()
  }

  def runUntil(server : Server, retries : Int, cmd : RemoteCommand, f : Any => Unit) : Boolean = {
    new Thread() {
      override def run() { server(cmd, f) }
    }.start()

    Thread.sleep(server.timeout + 30) // Arbitrary wait
    if (server.retries > 0) {
      def shouldRun = server.retries > 0 && server.retries < retries
      // Block until the server has tried 'retries' times
      while(shouldRun) {
        Thread.sleep(server.timeout)
      }
      if (server.retries >= retries) {
        goodSink.disconnect()
        fail("could not establish connection")
        false
      } else true
    } else true
  }

  describe("The server") {
    it ("can establish a connection") {
      runUntil(goodSink, 10, dummyGet, r => {
          // Should receive an error
          r.isInstanceOf[Error] should be (true)
        }
      )
    }

    it ("knows when it is online") {
      goodSink.isConnected should be (false)
      runUntil(goodSink, 10, dummyGet, _ => ())
      goodSink.isConnected should be (true)
    }

    it ("can exit") {
      badSink.disconnect()
      val time = System.currentTimeMillis()
      badSink(dummyGet, _ => ())
      // This should not stall
      System.currentTimeMillis() - time should be < 200L
    }

    it ("loops if messages time out") {
      evaluating {
        runUntil(badSink, 10, dummyGet, _ => ())
      } should produce [TestFailedException]
      badSink.retries should be >= 10
    }

  }

}
