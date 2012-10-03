package controllers

import play.api.mvc._
import akka.actor.{ActorRef, Props}
import actors.{Inbound, MessageProcessingActor}
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json._

object Application extends Controller {

  def queryClient = Action {
    implicit request =>
      val webSocketUrl = routes.Application.wsevents().webSocketURL(false)
      Ok(views.html.queryClient(webSocketUrl))
  }

  def echoClient = Action {
    implicit request =>
      val webSocketUrl = routes.Application.wsevents().webSocketURL(false)
      Ok(views.html.echoClient(webSocketUrl))
  }


  def wsevents = WebSocket.using[String] {
    request =>

      val out = Enumerator.imperative[String]()

      def sendOutboundMessage(msgType: String, value: JsValue) {
        val jsonMessage = toJson(Map("type" -> toJson(msgType), "data" -> value))
        val msg = Json.stringify(jsonMessage)
        println("putting:" + msg)
        out.push(msg)
      }

      val processingActor = Akka.system.actorOf(Props(new MessageProcessingActor(sendOutboundMessage)))

      val in = Iteratee.foreach[String] {
        value =>
          println("getting:" + value)
          val json = Json.parse(value)
          val data =  json \ "data"
          val msgType = (json \ "type").as[String]
          processingActor ! Inbound(msgType, data)
      }.mapDone {
        _ =>
          println("Disconnected")
      }

      (in, out)
  }

  private def processInboundMessage(json: JsValue, processingActor: ActorRef, send: (String, JsValue) => Unit) {

    // use here an actor so we can possibly address this actor over remote


  }

}