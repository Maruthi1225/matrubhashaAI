package com.major_project.multilang_ai.voice

import android.content.Context
import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.major_project.multilang_ai.uiNav.UserPreferences

object LanguageDetector {

    private val identifier: LanguageIdentifier = LanguageIdentification.getClient()
    private var preferredLanguage: AppLanguage = AppLanguage.TELUGU // default

    fun init(context: Context) {
        val savedCode = UserPreferences.getLanguage(context)
        preferredLanguage = AppLanguage.values().find { it.code == savedCode } ?: AppLanguage.TELUGU
        Log.d("LanguageDetector", "Initialized with preferred: ${preferredLanguage.displayName}")
    }

    fun detectLanguage(text: String, onResult: (AppLanguage) -> Unit) {
        identifier.identifyLanguage(text)
            .addOnSuccessListener { langCode ->
                val detected = if (langCode == "und") preferredLanguage else mapCodeToLanguage(langCode)
                Log.d("LanguageDetector", "Detected: ${detected.displayName}")
                onResult(detected)
            }
            .addOnFailureListener {
                Log.e("LanguageDetector", "Detection failed", it)
                onResult(preferredLanguage)
            }
    }

    private fun mapCodeToLanguage(code: String): AppLanguage {
        val normalized = code.lowercase()
        // Try to match the short code (e.g., "hi") or the full code (e.g., "hi-IN")
        return AppLanguage.values().firstOrNull { 
            it.code.lowercase().startsWith(normalized) || normalized.startsWith(it.code.lowercase().take(2))
        } ?: preferredLanguage
    }
}