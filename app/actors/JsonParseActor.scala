package actors

import akka.actor.Actor
import play.api.libs.json.{JsValue, Json}
import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Locale, Date}

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
class JsonParseActor extends Actor {

  def receive = {
    case content: String =>
      println("Parsing Json " + content.take(30))
      val beginTime = System.currentTimeMillis()
      context.parent ! Log("PARSING -- length: " + content.length)
      val result = Json.parse(content)
      val texts: Seq[JsValue] = (result \\ "text")
      val dates: Seq[JsValue] = (result \\ "created_at")

      val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.UK)
      val dateAsLongList = for{
        date <- dates
        dateString <- date.asOpt[String]
        d = format.parse(dateString)
        l = d.getTime
      } yield l

      if(dateAsLongList.nonEmpty){
        context.parent ! new Date(dateAsLongList.max)
        context.parent ! new Date(dateAsLongList.min)
      }


      context.parent ! Tweets(texts.size)

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
      context.parent ! Log("PARSING DONE -- %sms".format(System.currentTimeMillis() - beginTime))

      context.stop(self)
  }

}

object JsonParseActor {
  val blacklist = Set("line", "from", "with", "like", "your", "you", "that", "will", "amp", "this", "just", "does",
    "about", "more", "can't", "have", "every", "any", "what", "into", "there", "does", "i've", "some", "here")
}
