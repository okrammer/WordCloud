package actors

import akka.actor.{Props, Actor}
import play.api.libs.json.Json._
import play.api.libs.json.JsObject
import akka.util.duration._


/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 18:46
 * To change this template use File | Settings | File Templates.
 */
class TweetQueryActor(query: Query) extends Actor {

  println("TweetQueryActor: " + query)
  var latestHistogram: Option[Histogram] = None

  val tweetLoaderActor = context.actorOf(Props(new TweetLoaderActor(query)))
  tweetLoaderActor ! LoadNextPage(1)

  protected def receive = {
    case histogram: Histogram =>
      if (latestHistogram.isEmpty) {
        context.system.scheduler.scheduleOnce(500 milliseconds) {
          self ! Transmit()
        }
      }
      latestHistogram = Some(histogram)

    case Transmit() =>
      val sum = latestHistogram.get.map.map(_._2).sum
      val objects = latestHistogram.get.map.collect {
        case (word, count) => JsObject(Seq("word" -> toJson(word), "count" -> toJson(count / sum.toDouble)))
      }
      latestHistogram = None
      context.parent ! Outbound("histogram", toJson(objects))

    case Stop() =>
      println("stopping")
      context.parent ! Log("STOPPING")
      context.stop(self)

    case log: Log => context.parent ! log

    case s: Stats =>
      context.parent ! s
  }

  case class Transmit()

}
