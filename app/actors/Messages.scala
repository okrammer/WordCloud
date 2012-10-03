package actors

import play.api.libs.json.JsValue

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */

object LogLevel extends Enumeration{
  type LogLevel = Value
  val ERROR, INFO, WARN, DEBUG = Value
}

case class Query(query: String, resultsPerPage: Int)
case class Histogram(map: Seq[(String, Int)])
case class Stop()
case class LoadNextPage(page: Int)
case class ParseJson(string: String)
case class Outbound(resultType: String, value: JsValue)
case class Log(message: String, level: LogLevel.LogLevel = LogLevel.INFO)
case class Stats(name: String, value: String)
case class Tweets(count: Int)

