package org.venustus.samantha.speech.articles

import org.venustus.samantha.speech.articles.components.ArticleComponent

/**
 * Created by venkat on 31/07/15.
 */
trait ArticleExtractor {

    def extractContent(url: String, rawHtml: String): Set[ArticleComponent]

}

object ArticleExtractor {
    case class ExtractContentFromPage(url: String, rawHtml: String, priority: Int)
    case class ExtractedContent(components: Set[ArticleComponent], priority: Int)
}
