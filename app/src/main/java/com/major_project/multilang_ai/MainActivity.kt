package com.major_project.multilang_ai


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost



import com.major_project.multilang_ai.ui.VoiceOnlyChatScreen

import kotlinx.coroutines.launch


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