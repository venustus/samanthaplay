package org.venustus.samantha.speech.articles

import java.util.Date

import akka.pattern.pipe
import akka.actor.Actor
import akka.event.Logging
import com.google.inject.Inject
import org.venustus.samantha.speech.articles.ArticleExtractor.{ExtractedContent, ExtractContentFromPage}
import org.venustus.samantha.speech.articles.components.{PublishedDate, Author, ArticleComponent}
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.ws.{WS, WSClient}
import play.api.Play.current

import scala.concurrent.Future

/**
 * Created by venkat on 28/08/15.
 */
class EmbedlyArticleExtractor @Inject() (ws: WSClient, configuration: Configuration) extends Actor {
    val embedlyEndPoint = configuration getString "embedly.endpoint"
    val embedlyKey = configuration getString "embedly.key"
    val log = Logging(context.system, this)
    implicit val ec = context.dispatcher
    implicit val authorReads: Reads[Author] = (
        (JsPath \ "name").read[String] and (JsPath \ "url").read[String]
    )(Author.apply _)

    def receive = {
        case ExtractContentFromPage(url, rawHtml, priority) =>
            log info "Received extract command from assembler"

            extractContent(url, rawHtml) map {
                case s: Set[ArticleComponent] => ExtractedContent(s, priority)
            } pipeTo sender()
            //sender() ! ExtractedContent(Set(), priority)
    }

    def extractContent(url: String, rawHtml: String): Future[Set[ArticleComponent]] = {
        val embedlyFullUrl = embedlyEndPoint.get + "?url=" + url + "&key=" + embedlyKey.get
        val wsRequestHolder = WS url embedlyFullUrl
        implicit val ec = context.dispatcher
        (wsRequestHolder get()) map {
            response => Set((response.json \ "authors")(0).as[Author],
                PublishedDate(new Date((response.json \ "published").as[Long])))
        }
        (wsRequestHolder get()) map {
            response =>
                Set((response.json \ "authors")(0).asOpt[String] map { case au => Author(au, "") },
                    (response.json \ "published").asOpt[Long] map { case dp => PublishedDate(new Date(dp)) })
                    .filter(_.isDefined).map(_.get)
        }
    }

}

object EmbedlyArticleExtractor {
    case class Author(name:String, url: String)
    trait Factory {
        def apply(): Actor
    }
}