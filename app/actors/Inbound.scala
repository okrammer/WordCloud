package actors

import akka.actor.ActorRef
import play.api.libs.json.JsValue

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
case class Inbound(msgType: String, data: JsValue)
