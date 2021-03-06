package org.venustus.samantha.speech.articles

import java.util.Date

import akka.actor.Actor
import akka.event.Logging
import com.gravity.goose.{Article => GooseArticle, Configuration, Goose}
import org.venustus.samantha.speech.articles.ArticleExtractor.{ExtractedContent, ExtractContentFromPage}
import org.venustus.samantha.speech.articles.components._

/**
 * Created by venkat on 31/07/15.
 */
class GooseArticleExtractor extends Actor {

    val log = Logging(context.system, this)

    val configuration = new Configuration
    configuration setEnableImageFetching false
    val goose = new Goose(configuration)

    def receive = {
        case ExtractContentFromPage(url, rawHtml, priority) =>
            log info "Received extract command from assembler"
            sender() ! ExtractedContent(extractContent(url, rawHtml), priority)
    }

    def extractContent(url: String, rawHtml: String): Set[ArticleComponent] = {
        val gooseArticle: GooseArticle = goose extractContent (url, rawHtml)
        Set(Title(gooseArticle title), Speakables(((gooseArticle paragraphs) map {
            case paragraph => Speakable("text", paragraph.text, paragraph.xpath)
        }).toList))
    }
}

object GooseArticleExtractor {
    trait Factory {
        def apply(): Actor
    }
}

