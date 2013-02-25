package models

import play.api.templates.Html
import org.pegdown.PegDownProcessor

object Modules {

  /**
   * @param text Markdown page containing the list of modules
   * @return A list of HTML representation of each module
   */
  def parse(text: String): List[String] =
    for (m <- ModuleStart.split(text).drop(1).to[List])
    yield (new PegDownProcessor).markdownToHtml("## " + m)

  private val ModuleStart = "## ".r
}
