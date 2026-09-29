package kz.jarvis

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceSpeaker(ctx: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(ctx, this)
    private var ready = false
    private var kkSupported = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Қазақ тілін көр
            val kk = Locale("kk", "KZ")
            val r = tts.setLanguage(kk)

            if (r == TextToSpeech.LANG_MISSING_DATA ||
                r == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Қазақ жоқ → орысша резерв
                tts.language = Locale("ru", "RU")
                kkSupported = false
            } else {
                kkSupported = true
            }

            tts.setSpeechRate(0.95f)
            tts.setPitch(1.0f)
            ready = true

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {}
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {}
            })
        }
    }

    fun speak(text: String) {
        if (ready && text.isNotBlank()) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis_${System.currentTimeMillis()}"
            )
        }
    }

    /** Кезекке қосу — бірінен кейін бірі сөйлейді */
    fun speakQueue(text: String) {
        if (ready && text.isNotBlank()) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_ADD,
                null,
                "jarvis_${System.currentTimeMillis()}"
            )
        }
    }

    fun isKazakhSupported(): Boolean = kkSupported

    fun stop() {
        if (ready) tts.stop()
    }

    fun shutdown() {
        if (ready) {
            tts.stop()
            tts.shutdown()
        }
    }
}
