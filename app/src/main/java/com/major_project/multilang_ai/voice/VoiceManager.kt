package com.major_project.multilang_ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.major_project.multilang_ai.uiNav.UserPreferences
import java.util.Locale

class VoiceManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isSpeaking = false

    fun init(languageCode: String = "te-IN", onReady: (() -> Unit)? = null) {
        val locale = Locale.forLanguageTag(languageCode)
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = locale
                onReady?.invoke()
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
                stopListening()
            }
            override fun onDone(utteranceId: String?) { isSpeaking = false }
            override fun onError(utteranceId: String?) { isSpeaking = false }
        })
    }

    fun listen(onResult: (String, String) -> Unit, onError: (String) -> Unit) {
        if (isSpeaking) return
        stopListening()

        // Switch back to standard SpeechRecognizer for online support
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val langCode = UserPreferences.getLanguage(context).ifEmpty { "te-IN" }
        val locale = Locale.forLanguageTag(langCode)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            // Removed EXTRA_PREFER_OFFLINE to allow online recognition
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrEmpty()) {
                    LanguageDetector.detectLanguage(text) { lang -> onResult(text, lang.code) }
                } else {
                    onError("No speech detected")
                }
                stopListening()
            }

            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Please try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice engine is busy."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error. Please check your internet connection."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
                    else -> "Speech Error: $error"
                }
                onError(msg)
                stopListening()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun speakInstant(text: String, lang: String) {
        if (text.isBlank()) return
        val cleanText = text.replace(Regex("\\*\\*|\\*|_|`|~"), "").trim()
        val locale = Locale.forLanguageTag(lang.ifEmpty { "te-IN" })
        tts?.language = locale
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utt-${System.currentTimeMillis()}")
    }

    fun resumeTTS(text: String, lang: String) = speakInstant(text, lang)

    fun pauseTTS() {
        tts?.stop()
        isSpeaking = false
    }

    fun release() {
        tts?.shutdown()
        stopListening()
    }
}