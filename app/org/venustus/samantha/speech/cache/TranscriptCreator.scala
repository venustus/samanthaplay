package org.venustus.samantha.speech.cache

import java.io.BufferedInputStream
import java.net.URLDecoder

import akka.actor.Actor
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.amazonaws.services.s3.model.{AmazonS3Exception, CannedAccessControlList, ObjectMetadata, PutObjectRequest}
import com.google.inject.Inject
import org.venustus.samantha.speech.SpeechSynthesisEngine
import org.venustus.samantha.speech.cache.TranscriptCreator.{CreateTranscript, Done}
import com.amazonaws.services.s3.AmazonS3Client

/**
 * Created by venkat on 13/09/15.
 */
class TranscriptCreator @Inject() (sse: SpeechSynthesisEngine) extends Actor {

    val bucketName = "samantha-transcripts"
    val amazonS3Client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider("aws/transcriptstoreadmin.properties"))

    def receive = {
        case CreateTranscript(domain, key, text) => {
            if(!isAudioStreamAlreadyAvailable(amazonS3Client, domain, key, text)) {
                val audioStream = sse synthesizeSpeech (URLDecoder decode (text, "UTF-8"))
                val om = new ObjectMetadata
                om setContentType "audio/mpeg"
                val por = new PutObjectRequest(bucketName, domain + "/" + key, new BufferedInputStream(audioStream), om)
                por setCannedAcl CannedAccessControlList.PublicRead
                (por getRequestClientOptions) setReadLimit 1000000
                amazonS3Client putObject por
                sender() ! Done
            }
        }
    }

    private def isAudioStreamAlreadyAvailable(s3: AmazonS3Client, domain: String, key: String, text: String) = {
        try {
            s3 getObjectMetadata(bucketName, domain + "/" + key)
            true
        }
        catch {
            case s3e: AmazonS3Exception => {
                if((s3e getStatusCode()) == 404) false
                else throw s3e
            }
        }
    }

}

object TranscriptCreator {
    case class CreateTranscript(domain: String, key: String, text: String)
    case object Done
    trait Factory {
        def apply(): Actor
    }
}
