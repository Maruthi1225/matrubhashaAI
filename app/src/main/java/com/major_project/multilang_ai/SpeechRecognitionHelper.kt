package com.major_project.multilang_ai



import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast

object SpeechRecognitionHelper {
    const val REQUEST_CODE = 100

    fun startSpeechRecognition(activity: Activity) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN") // You can change this dynamically
        try {
            activity.startActivityForResult(intent, REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(activity, "Speech not supported", Toast.LENGTH_SHORT).show()
        }
    }
}
