package org.venustus.samantha.speech.cache

import java.io.InputStream
import java.net.URLDecoder
import java.security.MessageDigest
import java.util.Base64

import akka.actor.{ActorRef, Actor}
import com.google.inject.Inject
import org.venustus.samantha.speech.SpeechSynthesisEngine
import org.venustus.samantha.speech.cache.TranscriptCreator.CreateTranscript
import org.venustus.samantha.speech.cache.TranscriptStore._
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport

import scala.collection.mutable

/**
 * This actor acts as an interface to the transcript store.
 * Given a domain and text to convert to audio stream and store, it creates a unique
 * key for that text and then sends a message to the @code{TranscriptCreator} to
 * actually create the transcript using speech synthesis engine. But it doesn't
 * wait for response. Instead, it sends back the audio URL immediately.
 */
class TranscriptStore @Inject() (configuration: Configuration,
                                 sse: SpeechSynthesisEngine,
                                 transcriptCreatorFactory: TranscriptCreator.Factory) extends Actor
        with InjectedActorSupport {

    val transcriptsEndPointPrefix = configuration getString "babble-s3-transcripts-endpoint-prefix"
    private val speakableCache = mutable.Map[String, String]()
    private val transcriptCreator: ActorRef = injectedChild(transcriptCreatorFactory(), "tc")
    private def getBase64EncodedHash(text: String) = {
        val digest = MessageDigest getInstance "SHA-256"
        digest update (text getBytes "UTF-8")
        (Base64 getUrlEncoder ()) encodeToString (digest digest())
    }

    def receive = {
        case Store(domain, text) =>
            val textKey = getBase64EncodedHash(text)
            speakableCache put (textKey, text)
            transcriptCreator ! CreateTranscript(domain, textKey, text)
            sender() ! AudioURL(transcriptsEndPointPrefix.get + "/" + domain + "/" + textKey)
        case Retrieve(domain, key) =>
            speakableCache get key match {
                case Some(text) =>
                    sender() ! AudioStream(sse synthesizeSpeech (URLDecoder decode (text, "UTF-8")))
                case None => sender() ! KeyNotFound
            }
    }

}

object TranscriptStore {
    case class Store(domain: String, text: String)
    case class Retrieve(domain: String, key: String)
    case class AudioStream(s: InputStream)
    case class AudioURL(u: String)
    case object KeyNotFound
    trait Factory {
        def apply(): Actor
    }
}
