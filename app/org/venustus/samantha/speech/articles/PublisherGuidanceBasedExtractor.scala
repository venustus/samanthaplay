package org.venustus.samantha.speech.articles

import akka.actor.Actor
import akka.event.Logging
import org.venustus.samantha.speech.articles.ArticleExtractor.{ExtractedContent, ExtractContentFromPage}
import org.venustus.samantha.speech.articles.components.ArticleComponent

/**
 * Created by venkat on 17/08/15.
 */
class PublisherGuidanceBasedExtractor extends Actor {

    val log = Logging(context.system, this)

    def receive  = {
        case ExtractContentFromPage(url, rawHtml, priority) =>
            log info "Received extract command from assembler"
            sender() ! ExtractedContent(extractContent(url, rawHtml), priority)
    }

    def extractContent(url: String, rawHtml: String): Set[ArticleComponent] = {
        Set()
    }

}

object PublisherGuidanceBasedExtractor {
    trait Factory {
        def apply(): Actor
    }
}
