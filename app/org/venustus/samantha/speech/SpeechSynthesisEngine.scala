package org.venustus.samantha.speech

import java.io.InputStream

import com.google.inject.ImplementedBy
import org.venustus.samantha.speech.ivona.IvonaTTSEngine

/**
 * Created by venkat on 26/07/15.
 */
@ImplementedBy(classOf[IvonaTTSEngine])
trait SpeechSynthesisEngine {
    def synthesizeSpeech(text: String): InputStream
}
