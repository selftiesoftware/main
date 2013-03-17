package com.siigna.util.http

import org.scalatest.{GivenWhenThen, BeforeAndAfter, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import com.siigna.app.controller.remote.{RemoteConstants, Get, Session, RemoteCommand}
import com.siigna.app.model.server.User
import com.siigna.util.io

class ClientSpec extends FunSpec with BeforeAndAfter with ShouldMatchers with GivenWhenThen{


  val user = new User(0,"anon","dsfdsf")

  var session : Session = Session(0L, user)

  describe("Siigna Http Client"){

    it("Can open a connection to a server and send a message"){
      Given("A connection")
      val conn = new Connection("http://localhost:7788")

      Given("Some siigna data")
      val data = Get(RemoteConstants.Drawing, null, session)
      val marshalled = io.Marshal(data)

      println(new String(marshalled))
      val resp = conn.send(marshalled)

      println(new String(decode(resp)))
    }

  }

  def decode(stream: java.io.InputStream):Array[Byte] = {

    var in = Array[Byte]()

    var data = stream.read(in).asInstanceOf[Byte]

    while( data != -1){
      in = data +: in
      data = stream.read().asInstanceOf[Byte]
    }

    return in.reverse
  }

}
