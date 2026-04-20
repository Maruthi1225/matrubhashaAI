package com.major_project.multilang_ai.sarvam

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ChatSession(
    val id: String = "",
    val userId: String = "",
    val title: String = "New Chat",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val messages: List<SarvamMessage> = emptyList()
)

class ChatRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getSafeUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun saveChat(session: ChatSession): String? {
        val userId = getSafeUserId() ?: return null
        val chatData = session.copy(userId = userId, lastTimestamp = System.currentTimeMillis())
        
        return try {
            if (session.id.isEmpty()) {
                val docRef = db.collection("users").document(userId)
                    .collection("chats").add(chatData).await()
                docRef.id
            } else {
                db.collection("users").document(userId)
                    .collection("chats").document(session.id).set(chatData).await()
                session.id
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error saving chat: ${e.message}", e)
            null
        }
    }

    suspend fun getChatHistory(): List<ChatSession> {
        val userId = getSafeUserId() ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("chats")
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ChatSession::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching history: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getChat(chatId: String): ChatSession? {
        val userId = getSafeUserId() ?: return null
        return try {
            val doc = db.collection("users").document(userId)
                .collection("chats").document(chatId).get().await()
            doc.toObject(ChatSession::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching chat: ${e.message}")
            null
        }
    }

    suspend fun deleteChat(chatId: String) {
        val userId = getSafeUserId() ?: return
        try {
            db.collection("users").document(userId)
                .collection("chats").document(chatId).delete().await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting chat: ${e.message}")
        }
    }
}
