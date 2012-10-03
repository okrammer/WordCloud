package actors

import play.api.libs.json.JsValue

/**
 * Created with IntelliJ IDEA.
 * User: okrammer
 * Date: 02/10/2012
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
case class Query(query: String)
case class Histogram(map: Seq[(String, Int)])
case class Stop()
case class LoadNextPage(page: Int)
case class ParseJson(string: String)
case class Outbound(resultType: String, value: JsValue)
