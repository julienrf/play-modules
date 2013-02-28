package controllers

import play.api.mvc.{Result, Action, Controller}
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.cache.Cache
import play.api.Play.current
import models.Modules

object Application extends Controller {
  
  val index = Action { implicit request =>
    val notModified = for {
      requestEtag <- request.headers.get(IF_NONE_MATCH)
      etag <- Cache.getAs[String]("etag")
      if etag == requestEtag
    } yield NotModified
    notModified.getOrElse {
      Cache.getAs[Result]("modules").getOrElse {
        Async {
          WS.url("http://raw.github.com/playframework/Play20/master/documentation/manual/Modules.md").get()
            .map { githubResult =>
              if (githubResult.status < 400) {
                val etag = System.currentTimeMillis().toString
                val result = Ok(views.html.modules(Modules.parse(githubResult.body))).withHeaders(ETAG -> etag)
                Cache.set("modules", result, 60 * 60 * 3) // 3 hours
                Cache.set("etag", etag)
                result
              } else InternalServerError(views.html.oops())
            } recover {
              case _ => InternalServerError(views.html.oops())
            }
        }
      }
    }
  }
  
}