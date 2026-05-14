package com.major_project.multilang_ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.security.ProviderInstaller
import com.major_project.multilang_ai.sarvam.LocalAIService
import com.major_project.multilang_ai.uiNav.MainApp
import com.major_project.multilang_ai.uiNav.UserPreferences
import com.major_project.multilang_ai.voice.LanguageDetector
import com.major_project.multilang_ai.voice.VoiceManager

class MainActivity : ComponentActivity() {
    private lateinit var localAI: LocalAIService
    private lateinit var voice: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        
        super.onCreate(savedInstanceState)

        // Initialize Google Play Services Security Provider
        ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstalled() {
                Log.d("MainActivity", "Security provider installed successfully")
            }

            override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: android.content.Intent?) {
                Log.e("MainActivity", "Security provider installation failed: $errorCode")
            }
        })

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        val sarvamApiKey = "sk_dudmpyme_AZ4EPFdXKLxRAYr1ukLFwdex"
        localAI = LocalAIService(this, sarvamApiKey)

        LanguageDetector.init(this)
        val savedLang = UserPreferences.getLanguage(this)
        voice = VoiceManager(this)
        voice.init(savedLang.ifEmpty { "te-IN" })

        setContent {
            MainApp(localAI, voice)
        }
    }
}
