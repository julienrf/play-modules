package controllers

import play.api.mvc.{RequestHeader, Result, Action, Controller}
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.cache.Cache
import play.api.Play.current
import models.Modules

object Application extends Controller {
  
  val index = Action { implicit request =>
    Cached("modules", 60 * 60 * 3) { // 3 hours
      Async {
        {
          WS.url("http://raw.github.com/playframework/Play20/master/documentation/manual/Modules.md").get()
        } collect {
          case response if response.status < 300 => Ok(views.html.modules(Modules.parse(response.body)))
        } recover {
          case _ => InternalServerError(views.html.oops())
        }
      }
    }
  }

  def Cached(key: String, duration: Int)(result: => Result)(implicit request: RequestHeader): Result = {

    val resultEtag = s"${key}_etag"
    val resultKey = s"${key}_result"

    val notModified = for {
      requestEtag <- request.headers.get(IF_NONE_MATCH)
      etag <- Cache.getAs[String](resultEtag)
      if etag == requestEtag
    } yield NotModified

    notModified.getOrElse {
      Cache.getAs[Result](resultKey).getOrElse {
        val etag = System.currentTimeMillis().toString
        val taggedResult = result.withHeaders(ETAG -> etag)
        Cache.set(resultKey, taggedResult, duration)
        Cache.set(resultEtag, etag)
        taggedResult
      }
    }
  }
}