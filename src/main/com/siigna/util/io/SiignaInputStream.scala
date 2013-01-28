package com.siigna.util.io

import java.io.InputStream
import org.ubjson.io.UBJInputStream
import com.siigna.app.controller.remote.RemoteCommand

/**
 * An input stream capable of reading objects familiar to the Siigna domain. Uses the [[org.ubjson.io.UBJInputStream]]
 * to use the UBJSON functionality.
 * @param in  The InputStream from which to read data.
 * @see [[http://ubjson.org]] Universal Binary JSON
 */
class SiignaInputStream(in : InputStream) extends UBJInputStream(in) {

  /**
   * Attempts to read a RemoteCommand from the input stream as a given object.
   * @return  Some[RemoteCommand] if the command could be successfully read and parsed, None otherwise.
   */
  def readRemoteCommand : Option[RemoteCommand] = {
    None
  }

}
