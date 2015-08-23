package org.venustus.samantha.speech.articles

import akka.actor.Actor
import akka.event.Logging
import org.venustus.samantha.speech.articles.ArticleExtractor.{ExtractedContent, ExtractContentFromPage}

/**
 * Created by venkat on 17/08/15.
 */
class PublisherGuidanceBasedExtractor extends Actor with ArticleExtractor {

    val log = Logging(context.system, this)

    def receive  = {
        case ExtractContentFromPage(url, rawHtml, priority) =>
            log info "Received extract command from assembler"
            sender() ! ExtractedContent(extractContent(url, rawHtml), priority)
    }

    override def extractContent(url: String, rawHtml: String) = {
        Set()
    }

}
