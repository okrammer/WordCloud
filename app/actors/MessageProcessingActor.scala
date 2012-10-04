package actors

import akka.actor.{ActorRef, Props, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.util.duration._
import play.api.libs.json.{JsValue, JsObject}
import play.api.libs.json.Json._

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 09:24
 * To change this template use File | Settings | File Templates.
 */
class MessageProcessingActor(sender: (String, JsValue) => Unit) extends Actor {

  var queryActors = Seq[ActorRef]()

  protected def receive = {
    case Inbound("echo", data: JsValue) =>
      val string = data.as[String]

      self ! Outbound("echo", toJson("--> %s ...".format(string)))

      Akka.system.scheduler.scheduleOnce(2 seconds) {
        self ! Outbound("echo", toJson("<-- %s".format(string.reverse)))
      }

    case Inbound("query", data: JsValue) =>
      val query = Query((data \ "query").as[String], (data \ "resultsPerPage").as[Int])
      val newActor = context.actorOf(Props(new TweetQueryActor(query)))
      queryActors = (queryActors :+ newActor)

    case Inbound("stop", data: JsValue) =>
      println("sending stop")
      queryActors.foreach(_ ! Stop())
      queryActors = Seq()

    case Outbound(resultType, msg) =>
      sender(resultType, msg)

    case log: Log =>
      self ! Outbound("log", toJson(log.message))

    case Stats(name, value) =>
      self ! Outbound("stats", JsObject(Seq("name" -> toJson(name), "value" -> toJson(value))))

    case MessageProcessingStop =>
      context.stop(self)
  }

}
