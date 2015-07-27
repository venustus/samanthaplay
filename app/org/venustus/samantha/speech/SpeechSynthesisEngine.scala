package org.venustus.samantha.speech

import java.io.{InputStream, OutputStream}

/**
 * Created by venkat on 26/07/15.
 */
trait SpeechSynthesisEngine {
    def synthesizeSpeech(text: String): InputStream
}
