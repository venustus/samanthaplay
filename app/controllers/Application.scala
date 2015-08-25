package controllers

import java.net.URLDecoder
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import com.google.inject.name.Named
import com.redis.RedisClient
import org.venustus.samantha.speech.SpeechSynthesisEngine
import org.venustus.samantha.speech.articles.ArticleAssembler.AssembleArticleFromUrl
import org.venustus.samantha.speech.articles.{Speakable, Article}
import play.api.Play
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.Play.current

import play.api.libs.json._

import scala.collection.mutable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import javax.inject.Singleton

@Singleton
class Application @Inject() (ws: WSClient,
                             sse: SpeechSynthesisEngine,
                             @Named("assembler-router") assembler: ActorRef)
                            (implicit ec: ExecutionContext) extends Controller {

    implicit val timeout = Timeout(10 seconds)
    implicit val system = play.api.libs.concurrent.Akka.system
    /*
    val cacheClientEndPoint = Play.current.configuration.getString("cache.endpoint")
    val cacheClientPort = Play.current.configuration.getInt("cache.port")
    val cacheClient = RedisClient(cacheClientEndPoint.get, cacheClientPort.get)
    */
    val speakableCache = mutable.Map[String, String]()

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def getBase64EncodedHash(text: String) = {
        val digest = MessageDigest getInstance "SHA-256"
        digest update (text getBytes "UTF-8")
        (Base64 getUrlEncoder ()) encodeToString (digest digest())
    }

    def utp(url: String, jsonp: Boolean = true) = Action.async { request =>
        implicit val paragraphWrites = new Writes[Speakable] {
            def writes(p: Speakable) = Json.obj(
                "xpath" -> p.xpathStr,
                "text" -> p.text,
                "audioUrl" -> {
                    val textKey = getBase64EncodedHash(p.text)
                    speakableCache put (textKey, p.text)
                    "//" + request.host + "/ttswid/" + textKey
                }
            )
        }
        implicit val articleWrites = new Writes[Article] {
            def writes(a: Article) = Json.obj(
                "speakables" -> a.speakables,
                "title" -> a.title
            )
        }
        (assembler ? AssembleArticleFromUrl(url)) map {
            case article: Article => Ok((if(jsonp) "BABBLE.kickStart(" else "") + Json.toJson(article).toString + (if(jsonp) ")" else "")).withHeaders("Content-Type" -> "application/javascript")
        }
    }

    def tts(text: String) = Action {
        val audioStream = sse synthesizeSpeech (URLDecoder decode (text, "UTF-8"))
        println("Got audio stream")
        Ok.chunked(Enumerator.fromStream(audioStream)).withHeaders("Content-Type" -> "audio/mpeg, audio/x-mpeg, audio/x-mpeg-3, audio/mpeg3")
    }

    def ttswid(id: String) = Action {
        speakableCache get id match {
            case Some(text) =>
                val audioStream = sse synthesizeSpeech (URLDecoder decode (text, "UTF-8"))
                (Ok chunked Enumerator.fromStream(audioStream)) withHeaders ("Content-Type" -> "audio/mpeg, audio/x-mpeg, audio/x-mpeg-3, audio/mpeg3")
            case None => NotFound
        }
    }
}
