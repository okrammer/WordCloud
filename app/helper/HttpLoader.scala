package helper

import java.net.{HttpURLConnection, URL}
import java.io.InputStreamReader

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 04/10/2012
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
class HttpLoader(url: String) {
  var responseMessage: String = _
  var statusCode: Int = _
  def load(): String = {
    println("Loading: " + url)
    val connection = new URL(url).openConnection()
    connection match {
      case http: HttpURLConnection =>
        responseMessage = http.getResponseMessage
        statusCode = http.getResponseCode
    }
    val stream = new InputStreamReader(connection.getInputStream, "UTF-8")
    try {
      val stringBuilder = Stream.continually(stream.read()).takeWhile(_ != -1).foldLeft(new StringBuilder()) {
        (buffer, ch) =>
          buffer.append(ch.toChar)
      }
      val data = stringBuilder.toString()
      println("Got result '" + data.take(15) + "' from " + url)
      data
    } finally {
      stream.close()
    }
  }



}
