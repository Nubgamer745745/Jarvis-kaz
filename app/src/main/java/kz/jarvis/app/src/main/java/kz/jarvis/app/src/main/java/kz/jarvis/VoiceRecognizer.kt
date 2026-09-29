package kz.jarvis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class VoiceRecognizer(private val ctx: Context) {

    suspend fun listen(): String = suspendCancellableCoroutine { cont ->
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) {
            cont.resume("")
            return@suspendCancellableCoroutine
        }

        val rec = SpeechRecognizer.createSpeechRecognizer(ctx)

        rec.setRecognitionListener(object : RecognitionListener {
            override fun onResults(r: Bundle?) {
                val text = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""
                if (cont.isActive) cont.resume(text)
                rec.destroy()
            }

            override fun onError(e: Int) {
                if (cont.isActive) cont.resume("")
                rec.destroy()
            }

            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(r: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(i: Int, p: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "kk-KZ")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "kk-KZ")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "kk-KZ")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }

        rec.startListening(intent)
        cont.invokeOnCancellation { rec.destroy() }
    }
}
