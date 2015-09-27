package org.venustus.samantha.speech.articles

/**
 * Created by venkat on 16/08/15.
 */
case class Speakable(speakableType: String, text: String, xpathStr: String, audioUrl: Option[String] = None) {

    def withAudioUrl(url: String) = Speakable(speakableType, text, xpathStr, Some(url))

}
