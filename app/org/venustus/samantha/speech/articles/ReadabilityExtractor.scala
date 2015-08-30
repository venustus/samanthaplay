package org.venustus.samantha.speech.articles

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.Actor
import akka.pattern.pipe
import akka.event.Logging
import com.google.inject.Inject
import org.venustus.samantha.speech.articles.ArticleExtractor.{ExtractedContent, ExtractContentFromPage}
import org.venustus.samantha.speech.articles.components._
import play.api.Configuration
import play.api.libs.ws.{WS, WSClient}

import play.api.Play.current
import play.api.libs.json._

import scala.concurrent.Future

/**
 * Created by venkat on 24/08/15.
 */
class ReadabilityExtractor @Inject() (ws: WSClient, configuration: Configuration) extends Actor {

    val readabilityUrl = configuration.getString("readability.endpoint")
    val readabilityToken = configuration.getString("readability.token")
    val log = Logging(context.system, this)
    implicit val ec = context.dispatcher
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS")

    def receive = {
        case ExtractContentFromPage(url, rawHtml, priority) =>
            log info "Received extract command from assembler"
            extractContent(url, rawHtml) map {
                case s: Set[ArticleComponent] => ExtractedContent(s, priority)
            } pipeTo sender()
            //sender() ! ExtractedContent(Set(), priority)
    }

    def extractContent(url: String, rawHtml: String): Future[Set[ArticleComponent]] = {
        val readabilityFullUrl = readabilityUrl.get + "?url=" + url + "&token=" + readabilityToken.get
        val wsRequestHolder = WS url readabilityFullUrl
        implicit val ec = context.dispatcher
        (wsRequestHolder get()) map {
            response =>
                Set((response.json \ "author").asOpt[String] map { case au => Author(au, "") },
                    (response.json \ "date_published").asOpt[String] map { case dp => PublishedDate(sdf parse dp)})
                    .filter(_.isDefined).map(_.get)
        }
    }

}

object ReadabilityExtractor {
    trait Factory {
        def apply(): Actor
    }
}
