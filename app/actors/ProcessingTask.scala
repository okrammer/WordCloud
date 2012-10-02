package actors

import akka.actor.ActorRef

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
case class ProcessingTask(string: String, resultHandler: ActorRef)
