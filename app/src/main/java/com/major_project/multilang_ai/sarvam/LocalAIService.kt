package com.major_project.multilang_ai.sarvam

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.major_project.multilang_ai.voice.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class SarvamChatRequest(
    val model: String,
    val messages: List<SarvamMessage>,
    val temperature: Double = 0.1
)

data class SarvamMessage(
    val role: String = "",
    val content: String = ""
)

data class SarvamChatResponse(
    val choices: List<SarvamChoice>
)

data class SarvamChoice(
    val message: SarvamMessage
)

interface SarvamApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: SarvamChatRequest
    ): SarvamChatResponse
}

class LocalAIService(private val context: Context, private val sarvamApiKey: String) {

    private val authHeader = "Bearer $sarvamApiKey"
    var isReady = mutableStateOf(true)
    private val modelName = "sarvam-105b"

    private val chatHistory = mutableListOf<SarvamMessage>()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sarvam.ai/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val sarvamApi = retrofit.create(SarvamApi::class.java)

    private fun getLanguageName(code: String): String {
        return AppLanguage.values().find { it.code == code }?.displayName ?: "Hindi"
    }

    suspend fun getResponse(userInput: String, languageCode: String): String = withContext(Dispatchers.IO) {
        val langName = getLanguageName(languageCode)

        val systemMessage = SarvamMessage(
            role = "system",
            content = "You are Matrubhasha AI, a helpful Indian assistant. Respond strictly in $langName. Maintain context from previous turns."
        )

        chatHistory.add(SarvamMessage("user", userInput))

        val messagesToSend = listOf(systemMessage) + chatHistory.takeLast(10)

        try {
            val response = sarvamApi.getChatCompletion(
                authHeader,
                SarvamChatRequest(
                    model = modelName,
                    messages = messagesToSend
                )
            )

            val reply = response.choices.firstOrNull()?.message?.content ?: "No response."
            chatHistory.add(SarvamMessage("assistant", reply))
            reply
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            "⚠️ Sarvam Error ${e.code()}: $errorBody"
        } catch (e: Exception) {
            "⚠️ Error: ${e.localizedMessage}"
        }
    }

    fun getResponseStream(userInput: String, languageCode: String): Flow<String> = flow {
        val response = getResponse(userInput, languageCode)
        if (response.startsWith("⚠️")) {
            emit(response)
        } else {
            response.split(" ").forEach { word ->
                emit("$word ")
                delay(30)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun clearChat() {
        chatHistory.clear()
    }

    fun setChatHistory(messages: List<SarvamMessage>) {
        chatHistory.clear()
        chatHistory.addAll(messages)
    }

    fun getChatHistory(): List<SarvamMessage> = chatHistory.toList()
}
