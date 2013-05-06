package actors

import akka.actor.{Props, Actor}
import scala.concurrent.Future
import helper.HttpLoader
import java.util.Date
import java.text.SimpleDateFormat
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
class TweetLoaderActor(query: Query) extends Actor {

  val maxWait = 60
  val waitBetweenLogs = 5

  private var currentHistogram = Map[String, Int]()
  println("Initialising TweetLoaderActor")

  private var loadNewPages = true
  private var tweetCount = 0

  private var maxDate = new Date(0)
  private var minDate = new Date(Long.MaxValue)


  def receive = {
    case Histogram(tuples) =>
      currentHistogram = tuples.foldRight(currentHistogram) {
        (e, m) =>
          val (word, count) = e
          if (word contains query.query.toLowerCase) {
            m
          } else {
            m.updated(word, m.get(word).getOrElse(0) + count)
          }
      }

      context.parent ! Histogram(currentHistogram.toList.sortBy(_._2).reverse.take(20))

    case date: Date =>
      if (date.getTime > maxDate.getTime) {
        maxDate = date
        context.parent ! Stats("maxTimespan", dateFormat.format(date))
      }
      if (date.getTime < minDate.getTime) {
        minDate = date
        context.parent ! Stats("minTimespan", dateFormat.format(date))
      }

    case LoadNextPage(page) =>
      if (loadNewPages) loadPage(page)

    case Stop() =>
      loadNewPages = false
      context.parent ! Log("STOPPING LOAD PAGES")

    case ParseJson(text) =>
      context.actorOf(Props[JsonParseActor]) ! text

    case log: Log => context.parent ! log

    case Tweets(count) =>
      tweetCount += count
      context.parent ! Stats("tweets", tweetCount.toString)

    case s: Stats =>
      context.parent ! s

    case WaitingAfter403(waitTime, nextPage) if waitTime <= 0 =>
      self ! Log("TRY AGAIN ...")
      self ! LoadNextPage(nextPage)

    case WaitingAfter403(waitTime, nextPage) =>
      self ! Log("WAITING -- %ss".format(waitTime))
      context.system.scheduler.scheduleOnce(waitBetweenLogs seconds) {
        self ! WaitingAfter403(waitTime - waitBetweenLogs, nextPage)
      }

  }

  def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")

  def loadPage(page: Int) {
    context.parent ! Stats("page", page.toString())
    val url: String = "http://search.twitter.com/search.json?q=%s&rpp=%s&include_entities=true&result_type=mixed&page=%s".format(query.query, query.resultsPerPage, page)
    implicit val executionContext = context.dispatcher

    val httpLoader = new HttpLoader(url)
    val future = Future[String] {
      val beginTime = System.currentTimeMillis()
      context.parent ! Log("LOADING - " + url)
      val contentString = httpLoader.load()
      context.parent ! Log("DONE LOADING - %sms".format(System.currentTimeMillis() - beginTime))
      contentString
    }
    future.onFailure {
      case e =>
        if (httpLoader.statusCode == 403) {
          self ! Log("Got 403 from Twitter wating, to try again ...")
          self ! WaitingAfter403(maxWait, page + 1)
        } else {
          self ! Log("Error occured: " + e.getMessage + "; ResponseMessage: " + httpLoader.responseMessage)
        }

    }
    future.onSuccess {
      case text =>
        self ! LoadNextPage(page + 1)
        self ! ParseJson(text)

    }
  }

  case class WaitingAfter403(seconds: Int, nextPageToLoad: Int)


}
