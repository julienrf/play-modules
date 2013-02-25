package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.cache.Cached
import play.api.Play.current
import models.Modules

object Application extends Controller {
  
  val index = Cached(_ => "modules", duration = 60 * 60) {
    Action {
      Async {
        for {
          result <- WS.url("http://raw.github.com/playframework/Play20/master/documentation/manual/Modules.md").get()
        } yield {
          Ok(views.html.modules(Modules.parse(result.body)))
        }
      }
    }
  }
  
}