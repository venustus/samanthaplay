package org.venustus.samantha.speech

import java.io.InputStream

/**
 * Created by venkat on 26/07/15.
 */
trait SpeechOutputHandler {
    def processSpeech(in: InputStream)
}
