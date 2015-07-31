package org.venustus.samantha.speech.ivona

import java.io.InputStream

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.google.inject.Singleton
import com.ivona.services.tts.IvonaSpeechCloudClient
import com.ivona.services.tts.model.{Voice, Input, CreateSpeechRequest}
import org.venustus.samantha.speech.SpeechSynthesisEngine

/**
 * Created by venkat on 26/07/15.
 */
@Singleton
class IvonaTTSEngine extends SpeechSynthesisEngine {
    private val speechCloudClient = new IvonaSpeechCloudClient(
        new ClasspathPropertiesFileCredentialsProvider("ivona/IvonaCredentials.properties"))
    speechCloudClient setEndpoint ("https://tts.eu-west-1.ivonacloud.com")

    override def synthesizeSpeech(text: String): InputStream = {
        val createSpeechRequest = new CreateSpeechRequest()
        val input = new Input()
        val voice = new Voice()

        voice.setName("Salli")
        input.setData(text)

        createSpeechRequest.setInput(input)
        createSpeechRequest.setVoice(voice)

        val speech = speechCloudClient createSpeech (createSpeechRequest)

        println("\nSuccess sending request:");
        println(" content type:\t" + speech.getContentType());
        println(" request id:\t" + speech.getTtsRequestId());
        println(" request chars:\t" + speech.getTtsRequestCharacters());
        println(" request units:\t" + speech.getTtsRequestUnits());

        speech getBody()
    }
}
