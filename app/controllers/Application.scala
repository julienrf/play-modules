package controllers

import play.api.mvc.{Result, Action, Controller}
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.cache.Cache
import play.api.Play.current
import models.Modules

object Application extends Controller {
  
  val index = Action {
      Cache.getAs[Result]("modules").getOrElse {
        Async {
          WS.url("http://raw.github.com/playframework/Play20/master/documentation/manual/Modules.md").get()
            .map { githubResult =>
              if (githubResult.status < 400) {
                val result = Ok(views.html.modules(Modules.parse(githubResult.body)))
                Cache.set("modules", result, 60 * 60 * 3) // 3 hours
                result
              } else Ok(views.html.oops())
            } recover {
              case _ => Ok(views.html.oops())
            }
        }
      }
  }
  
}