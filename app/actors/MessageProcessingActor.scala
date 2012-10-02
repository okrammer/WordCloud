package actors

import akka.actor.{ActorRef, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.util.duration._

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 09:24
 * To change this template use File | Settings | File Templates.
 */
class MessageProcessingActor extends Actor{
  protected def receive = {
    case ProcessingTask(msg, resultHandler) =>

      resultHandler ! "Processing %s ...".format(msg)

      Akka.system.scheduler.scheduleOnce(2 seconds){
        resultHandler ! "-- Result %s".format(msg.reverse)
      }
  }
}
