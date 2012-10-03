package actors

import akka.actor.{ActorRef, Props, Actor}
import akka.dispatch.Future
import java.net.URL
import java.io.InputStreamReader

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
class TweetLoaderActor(query: Query) extends Actor {

  private var currentHistogram = Map[String, Int]()
  println("Initialising TweetLoaderActor")

  private var loadNewPages = true
  private var tweetCount = 0


  protected def receive = {
    case Histogram(tuples) =>
      currentHistogram = tuples.foldRight(currentHistogram) {
        (e, m) =>
          val (word, count) = e
          if (word contains query.query.toLowerCase){
            m
          }else{
            m.updated(word, m.get(word).getOrElse(0) + count)
          }
      }

      context.parent ! Histogram(currentHistogram.toList.filter(_._2 > 10).sortBy(_._2).reverse.take(30))

    case LoadNextPage(page) =>
      if (loadNewPages) loadPage(page)

    case Stop() =>
      loadNewPages = false
      context.parent ! Log("STOPPING LOAD PAGES")

    case ParseJson(text) =>
      jsonParserActor ! text

    case log: Log => context.parent ! log

    case Tweets(count) =>
      tweetCount += count
      context.parent ! Stats("tweets", tweetCount.toString)

    case s: Stats =>
      context.parent ! s
  }

  def jsonParserActor = context.actorOf(Props[JsonParseActor])

  def loadPage(page: Int) {
    context.parent ! Stats("page", page.toString())
    val url: String = "http://search.twitter.com/search.json?q=%s&rpp=%s&include_entities=true&result_type=mixed&page=%s".format(query.query, query.resultsPerPage, page)
    implicit val executionContext = context.dispatcher
    Future[String] {
      println("Loading: " + url)
      try {
        context.parent ! Log("LOADING - " + url, null)
        val connection = new URL(url).openConnection()
        val stream = new InputStreamReader(connection.getInputStream, "UTF-8")
        try {
          val stringBuilder = Stream.continually(stream.read()).takeWhile(_ != -1).foldLeft(new StringBuilder()) {
            (buffer, ch) =>
              buffer.append(ch.toChar)
          }
          val data = stringBuilder.toString()
          println("Got result '" + data.take(20) + "' from " + url)
          context.parent ! Log("DONE LOADING - " + url, null)
          data
        } finally {
          stream.close()
        }
      } catch {
        case e => e.printStackTrace()
        null
      }
    }.foreach {
      text =>
      self ! ParseJson(text)
      self ! LoadNextPage(page + 1)
    }
  }

}
