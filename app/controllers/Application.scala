package controllers

import java.net.URLDecoder
import javax.inject.Inject

import org.venustus.samantha.speech.ivona.IvonaTTSEngine
import play.api.Play
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

class Application @Inject() (ws: WSClient) extends Controller {

    val ivonaEngine = new IvonaTTSEngine()
    val readabilityUrl = Play.current.configuration

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def tts(text: String) = Action {
        val audioStream = ivonaEngine synthesizeSpeech (URLDecoder.decode(text, "UTF-8"))
        println("Got audio stream")
        Ok.chunked(Enumerator.fromStream(audioStream)).withHeaders("Content-Type" -> "audio/mpeg, audio/x-mpeg, audio/x-mpeg-3, audio/mpeg3")
    }

    def uts(url: String) = Action.async {
        val readabilityUrl = Play.current.configuration.getString("readability.endpoint")
        val readabilityToken = Play.current.configuration.getString("readability.token")
        val readabilityRequest = ws.url(readabilityUrl.get).withQueryString(
            "url" -> URLDecoder.decode(url, "UTF-8"),
            "token" -> readabilityToken.get
        )
        for {
            wsResponse <- readabilityRequest get ()
            x = println(wsResponse.status)
            y = println(wsResponse.statusText)
            z = println(wsResponse.body)
            textToRead = (wsResponse.json \ "title").as[String] + " by " + (wsResponse.json \ "author").as[String]
            audioStream = ivonaEngine synthesizeSpeech (textToRead)
        } yield (Ok chunked (Enumerator fromStream (audioStream)))
    }

}
