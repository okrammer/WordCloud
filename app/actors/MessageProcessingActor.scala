package actors

import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.util.duration._
import play.api.libs.json.JsString
import play.api.libs.json.Json._

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 09:24
 * To change this template use File | Settings | File Templates.
 */
class MessageProcessingActor extends Actor {
  protected def receive = {
    case ProcessingTask(JsString(string), resultHandler) =>
      resultHandler ! toJson("--> %s ...".format(string))

      Akka.system.scheduler.scheduleOnce(2 seconds) {
        resultHandler ! toJson("<-- %s".format(string.reverse))
      }

  }
}
