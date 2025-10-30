package com.major_project.multilang_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

        // 👇 Track TTS speaking state
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
                stopListening() // ✅ Fully stop STT when speaking starts
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        })
    }

    /** ✅ Prevent STT from starting while speaking */
    fun listen(onResult: (String, String) -> Unit, onError: (String) -> Unit) {
        if (isSpeaking) return

        stopListening()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val locale = UserPreferences.getLanguage(context).let {
            Locale.forLanguageTag(it.ifEmpty { "te-IN" })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrEmpty()) {
                    LanguageDetector.detectLanguage(text) { lang ->
                        onResult(text, lang.code)
                    }
                } else onError("No speech detected")
                stopListening()
            }

            override fun onError(error: Int) {
                onError("Speech error $error")
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
        val cleanText = text
            .replace(Regex("\\*\\*|\\*|_|`|~"), "")
            .replace(Regex("\\[(.*?)\\]\\((.*?)\\)"), "")
            .trim()

        val locale = Locale.forLanguageTag(lang.ifEmpty { "te-IN" })
        tts?.language = locale
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utterance-${System.currentTimeMillis()}")
    }

    fun pauseTTS() {
        tts?.stop()
        isSpeaking = false
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        stopListening()
    }
    fun resumeTTS(text: String, lang: String) {
        val locale = Locale.forLanguageTag(lang.ifEmpty { "te-IN" })
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
