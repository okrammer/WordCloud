package actors

import akka.actor.{Props, Actor}
import java.net.URL
import play.api.libs.json.Json

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
class JsonParseActor extends Actor{


  protected def receive = {
    case query =>
      val urlString = "http://search.twitter.com/search.json?q=blue%20angels&rpp=20&include_entities=true&result_type=mixed"
      val stream = new URL(urlString).openStream()

      Json.parse(steam)
  }
}
