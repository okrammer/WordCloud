package actors

import akka.actor.Actor

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 09:24
 * To change this template use File | Settings | File Templates.
 */
class MessageSendingActor(sender: (String) => Unit) extends Actor{
  protected def receive = {
    case msg: String => sender(msg)
  }
}
