package com.major_project.multilang_ai


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.major_project.multilang_ai.google.GeminiService
import com.major_project.multilang_ai.google.Secrets


import com.major_project.multilang_ai.uiNav.MainApp
import com.major_project.multilang_ai.uiNav.UserPreferences
import com.major_project.multilang_ai.voice.LanguageDetector
import com.major_project.multilang_ai.voice.VoiceManager


class MainActivity : ComponentActivity() {
    private lateinit var gemini: GeminiService
    private lateinit var voice: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gemini = GeminiService(Secrets.GEMINI_API_KEY)
        LanguageDetector.init(this)

        val savedLang = UserPreferences.getLanguage(this)
        val firstLaunch = savedLang.isEmpty()

        voice = VoiceManager(this)
        voice.init(savedLang.ifEmpty { "te-IN" }) // Default Telugu

        setContent {

            MainApp(gemini, voice)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voice.release()
    }
}