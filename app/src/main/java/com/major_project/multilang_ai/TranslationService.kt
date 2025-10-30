package com.major_project.multilang_ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TranslationService {

    suspend fun translateText(
        text: String,
        targetLangCode: String,
        gemini: GeminiService
    ): String = withContext(Dispatchers.IO) {
        // Skip translation if Gemini already responds in same language
        if (targetLangCode.startsWith("en")) return@withContext text

        val targetLanguage = AppLanguage.values().find { it.code == targetLangCode }?.displayName
            ?: "the selected language"

        val prompt = """
            Translate this text into natural, fluent $targetLanguage script.
            Keep meaning, tone, and context accurate.
            Input: "$text"
            Output:
        """.trimIndent()

        return@withContext try {
            val translation = gemini.getResponse(prompt)
            translation.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            text // fallback if translation fails
        }
    }
}
