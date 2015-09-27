package controllers

import java.net.{URL, URLDecoder}
import javax.inject.Inject

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import com.google.inject.name.Named
import org.ocpsoft.prettytime.PrettyTime
import org.venustus.samantha.speech.SpeechSynthesisEngine
import org.venustus.samantha.speech.articles.ArticleAssembler.AssembleArticleFromUrl
import org.venustus.samantha.speech.articles.{Speakable, Article}
import org.venustus.samantha.speech.cache.TranscriptStore.{AudioURL, AudioStream, Retrieve, Store}
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.Play.current

import play.api.libs.json._

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._

import javax.inject.Singleton

import scala.util.Success

@Singleton
class Application @Inject() (ws: WSClient,
                             sse: SpeechSynthesisEngine,
                             @Named("assembler-router") assembler: ActorRef,
                             @Named("store-router") store: ActorRef)
                            (implicit ec: ExecutionContext) extends Controller {


    val prettyTime: PrettyTime = new PrettyTime
    implicit val timeout = Timeout(10 seconds)
    implicit val system = play.api.libs.concurrent.Akka.system
    implicit val paragraphWrites = new Writes[Speakable] {
        def writes(p: Speakable) = Json.obj(
            "xpath" -> p.xpathStr,
            "text" -> p.text,
            "audioUrl" -> p.audioUrl.get
        )
    }
    implicit val articleWrites = new Writes[Article] {
        def writes(a: Article) = {
            var jsObj = Json.obj("speakables" -> a.speakables)
            if(a.title.isDefined) jsObj = jsObj + ("title" -> JsString(a.title.get))
            if(a.author.isDefined) jsObj = jsObj + ("author" -> JsString(a.author.get))
            if(a.publishedDate.isDefined) jsObj = jsObj + ("published" -> JsString(prettyTime format a.publishedDate.get))
            jsObj
        }
    }


    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def getDomainFromUrl(url: String) = new URL(url).getHost

    def utp(url: String, jsonp: Boolean = true) = Action.async {
        def getUpdatedArticle(articleFuture: Future[Article]) = {
            articleFuture flatMap { case article =>
                val articleWithIntroduction = {
                    var introductionText = article.title.get
                    if (article.author.isDefined) introductionText = introductionText + ". Written by " + article.author.get
                    if (article.publishedDate.isDefined) introductionText = introductionText + ". Published " + (prettyTime format article.publishedDate.get)
                    Article(article.title, article.publishedDate, article.author, Speakable("text", introductionText, "") :: article.speakables)
                }
                val updatedSpeakables: List[Future[Speakable]] = articleWithIntroduction.speakables map { case speakable =>
                    (store ? Store(getDomainFromUrl(url), speakable.text)) map {
                        case AudioURL(audioUrl) => speakable withAudioUrl audioUrl
                    }
                }
                Future.sequence(updatedSpeakables) map {
                    case speakables => Article(article.title, article.publishedDate, article.author, speakables)
                }
            }
        }
        val updatedArticle: Future[Article] = getUpdatedArticle((assembler ? AssembleArticleFromUrl(url)).mapTo[Article])
        updatedArticle map (ua =>
            Ok((if (jsonp) "BABBLE.kickStart(" else "") + Json.toJson(ua).toString + (if (jsonp) ")" else ""))
                withHeaders ("Content-Type" -> "application/javascript")
        )
    }

    def tts(text: String) = Action {
        val audioStream = sse synthesizeSpeech (URLDecoder decode (text, "UTF-8"))
        println("Got audio stream")
        Ok.chunked(Enumerator.fromStream(audioStream)).withHeaders("Content-Type" -> "audio/mpeg, audio/x-mpeg, audio/x-mpeg-3, audio/mpeg3")
    }

    def ttswid(id: String) = Action.async {
        request => {
            (store ? Retrieve(request.headers.get("Referer").get, id)) map {
                case AudioStream(in) =>
                    (Ok chunked Enumerator.fromStream(in)) withHeaders ("Content-Type" -> "audio/mpeg, audio/x-mpeg, audio/x-mpeg-3, audio/mpeg3")

            }
        }
    }
}
