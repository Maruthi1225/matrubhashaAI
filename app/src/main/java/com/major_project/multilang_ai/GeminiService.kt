package com.major_project.multilang_ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash", // fast + free tier friendly
        apiKey = apiKey
    )

    suspend fun getResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent(prompt)
            response.text ?: "No response."
        } catch (e: Exception) {
            "⚠️ Gemini Error: ${e.localizedMessage}"
        }
    }



}
