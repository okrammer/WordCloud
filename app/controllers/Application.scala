package controllers

import play.api.mvc._
import akka.actor.Props
import actors.{ProcessingTask, MessageProcessingActor, MessageSendingActor}
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json._

object Application extends Controller {

  def simpleClient = Action {
    implicit request =>
      val webSocketUrl = routes.Application.wsevents().webSocketURL(false)
      Ok(views.html.simpleClient(webSocketUrl))
  }

  def wsevents = WebSocket.using[String] {
    request =>

      val out = Enumerator.imperative[String]()

      def sendOutboundMessage(value: JsValue) {
        val jsonMessage = toJson(Map("type" -> toJson("message"), "data" -> value))
        val msg = Json.stringify(jsonMessage)
        println("putting:" + msg)
        out.push(msg)
      }

      val in = Iteratee.foreach[String] {
        value =>
          println("getting:" + value)
          val json = Json.parse(value) \ "data"
          processInboundMessage(json, sendOutboundMessage)
      }.mapDone {
        _ =>
          println("Disconnected")
      }

      (in, out)
  }

  private def processInboundMessage(json: JsValue, send: JsValue => Unit) {

    val string = json.asOpt[String].getOrElse("")

    def sendStringMessage(string: String){
      send(toJson(string))
    }

    // use here an actor so we can possibly address this actor over remote
    val targetActor = Akka.system.actorOf(Props(new MessageSendingActor(sendStringMessage)))

    // the actual calculation actor for long running tasks
    val processingActor = Akka.system.actorOf(Props[MessageProcessingActor])

    processingActor ! ProcessingTask(string, targetActor)
  }

}