package controllers

import java.net.URLDecoder
import javax.inject.Inject

import com.gravity.goose.{Article, Paragraph}
import org.venustus.samantha.speech.SpeechSynthesisEngine
import org.venustus.samantha.speech.articles.ArticleExtractor
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.utils.UriEncoding


import scala.concurrent.Future

class Application @Inject() (ws: WSClient, sse: SpeechSynthesisEngine, ae: ArticleExtractor) extends Controller {

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def utp(url: String) = Action.async { request =>
        implicit val paragraphWrites = new Writes[Paragraph] {
            def writes(p: Paragraph) = Json.obj(
                "xpath" -> p.xpath,
                "text" -> p.text,
                "audioUrl" -> ("//" + request.host + "/tts/" + UriEncoding.encodePathSegment(p.text, "UTF-8"))
            )
        }
        implicit val articleWrites = new Writes[Article] {
            def writes(a: Article) = Json.obj(
                "paragraphs" -> a.paragraphs,
                "title" -> a.title
            )
        }
        Future { ae extractContent (url) } map {
            case article => Ok("BABBLE.kickStart(" + Json.toJson(article).toString + ")").withHeaders("Content-Type" -> "application/javascript")
        }
    }

    def tts(text: String) = Action {
        val audioStream = sse synthesizeSpeech (URLDecoder.decode(text, "UTF-8"))
        println("Got audio stream")
        Ok.chunked(Enumerator.fromStream(audioStream)).withHeaders("Content-Type" -> "audio/mpeg, audio/x-mpeg, audio/x-mpeg-3, audio/mpeg3")
    }

    def uts(url: String) = Action.async {
        Future { ae extractContent (url) } map {
            case article => (Ok chunked (Enumerator fromStream ((sse synthesizeSpeech (article.title + ". " + article.cleanedArticleText)))))
        }
    }

}
