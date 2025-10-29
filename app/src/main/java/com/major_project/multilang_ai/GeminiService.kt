package com.major_project.multilang_ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash", // fast + free tier friendly
        apiKey = apiKey
    )

    suspend fun getResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent(prompt)
            response.text ?: "⚠️ No response from Gemini."
        } catch (e: Exception) {
            "❌ Error: ${e.localizedMessage}"
        }
    }

    suspend fun detectLanguage(text: String): String {
        val prompt = "Detect the language of this text and reply only the language name: \"$text\""
        return getResponse(prompt).lowercase()
    }

}
