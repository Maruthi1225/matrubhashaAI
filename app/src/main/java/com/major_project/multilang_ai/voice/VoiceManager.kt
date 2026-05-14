package com.major_project.multilang_ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private var currentSessionId = 0
    private var currentListeningStateCallback: ((Boolean) -> Unit)? = null
    private var currentRmsCallback: ((Float) -> Unit)? = null

    // Safety watchdog
    private val watchdog = Runnable {
        stopListening()
    }

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
                mainHandler.post { stopListening() }
            }
            override fun onDone(utteranceId: String?) { isSpeaking = false }
            override fun onError(utteranceId: String?) { isSpeaking = false }
        })
    }

    private fun updateListeningState(listening: Boolean) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            currentListeningStateCallback?.invoke(listening)
        } else {
            mainHandler.post {
                currentListeningStateCallback?.invoke(listening)
            }
        }
    }

    fun listen(
        onResult: (String, String) -> Unit, 
        onError: (String) -> Unit,
        onListeningStateChanged: (Boolean) -> Unit,
        onRmsChanged: ((Float) -> Unit)? = null
    ) {
        if (isSpeaking) {
            onListeningStateChanged(false)
            return
        }
        
        val sessionId = ++currentSessionId
        this.currentListeningStateCallback = onListeningStateChanged
        this.currentRmsCallback = onRmsChanged
        
        mainHandler.post {
            stopListeningInternal(false)
            
            mainHandler.removeCallbacks(watchdog)
            mainHandler.postDelayed(watchdog, 15000L)

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

            val langCode = UserPreferences.getLanguage(context).ifEmpty { "te-IN" }
            val locale = Locale.forLanguageTag(langCode)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                private fun isCurrent() = sessionId == currentSessionId

                override fun onReadyForSpeech(params: Bundle?) {
                    if (isCurrent()) updateListeningState(true)
                }
                
                override fun onBeginningOfSpeech() {
                    if (isCurrent()) {
                        updateListeningState(true)
                        mainHandler.removeCallbacks(watchdog)
                        mainHandler.postDelayed(watchdog, 20000L)
                    }
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    if (isCurrent()) {
                        mainHandler.post { currentRmsCallback?.invoke(rmsdB) }
                    }
                }

                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    if (isCurrent()) updateListeningState(false)
                }

                override fun onError(error: Int) {
                    if (!isCurrent()) return
                    
                    updateListeningState(false)
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Mic is busy."
                        SpeechRecognizer.ERROR_NETWORK -> "Network issue."
                        else -> null
                    }
                    
                    if (msg != null) onError(msg)
                    stopListening()
                }

                override fun onResults(results: Bundle?) {
                    if (!isCurrent()) return
                    
                    updateListeningState(false)
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrEmpty()) {
                        LanguageDetector.detectLanguage(text) { lang -> onResult(text, lang.code) }
                    }
                    stopListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                updateListeningState(false)
                onError("Failed to start mic")
            }
        }
    }

    fun stopListening() {
        currentSessionId++
        stopListeningInternal(true)
    }

    private fun stopListeningInternal(updateUI: Boolean) {
        mainHandler.removeCallbacks(watchdog)
        if (updateUI) updateListeningState(false)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) { }
        speechRecognizer = null
        currentRmsCallback = null
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
        mainHandler.removeCallbacks(watchdog)
        tts?.shutdown()
        stopListening()
    }
}
