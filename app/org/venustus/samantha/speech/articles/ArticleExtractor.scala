package org.venustus.samantha.speech.articles

import akka.actor.Actor
import org.venustus.samantha.speech.articles.components.ArticleComponent

/**
 * Created by venkat on 31/07/15.
 */
object ArticleExtractor {
    case class ExtractContentFromPage(url: String, rawHtml: String, priority: Int)
    case class ExtractedContent(components: Set[ArticleComponent], priority: Int)
}
