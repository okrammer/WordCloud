package actors

import akka.actor.Actor
import play.api.libs.json.{JsValue, Json}

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
class JsonParseActor extends Actor {

  protected def receive = {
    case content: String =>
      println("Parsing Json " + content.take(30))
      context.parent ! Log("PARSING -- length: " + content.length)
      val result = Json.parse(content)
      val texts: Seq[JsValue] = (result \\ "text")
      context.parent ! Tweets(texts.size)
      println("Got texts: " + texts)
      context.parent ! Log("PARSING DONE -- texts: " + texts.size)
      if (texts.size == 0) {
        context.parent ! Stop()
      } else {
        for {
          text <- texts
          t <- text.asOpt[String]
          token <- t.split("[\\s]")
          strippedToken = token.replaceAll("[#?.!,;:&()]", "").toLowerCase
          if (strippedToken.size > 3)
          if ! (JsonParseActor.blacklist contains strippedToken)
          if !strippedToken.startsWith("http")
          if !strippedToken.startsWith("@")
        } context.parent ! Histogram(Seq(strippedToken -> 1))
      }

      context.stop(self)
  }

}

object JsonParseActor {
  val blacklist = Set("line", "from", "with", "like", "your", "you", "that", "will", "amp", "this", "just", "does",
    "about", "more", "can't", "have", "every", "any", "what", "into", "there", "does", "i've", "some", "here")
}
