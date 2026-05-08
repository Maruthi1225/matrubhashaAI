package com.major_project.multilang_ai.sarvam

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ChatSession(
    var id: String = "",
    var userId: String = "",
    var title: String = "New Chat",
    var lastTimestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("isPinned")
    @set:PropertyName("isPinned")
    var isPinned: Boolean = false,
    var messages: List<SarvamMessage> = emptyList()
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

    suspend fun updateChatTitle(chatId: String, newTitle: String) {
        val userId = getSafeUserId() ?: return
        try {
            db.collection("users").document(userId)
                .collection("chats").document(chatId)
                .update("title", newTitle).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error updating title: ${e.message}")
        }
    }

    suspend fun togglePin(chatId: String, pinned: Boolean) {
        val userId = getSafeUserId() ?: return
        try {
            db.collection("users").document(userId)
                .collection("chats").document(chatId)
                .update("isPinned", pinned).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error toggling pin: ${e.message}")
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
                try {
                    val session = doc.toObject(ChatSession::class.java)
                    val pinnedStatus = doc.getBoolean("isPinned") ?: doc.getBoolean("pinned") ?: false
                    session?.copy(id = doc.id, isPinned = pinnedStatus)
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error mapping document ${doc.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.isPinned }
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
            val session = doc.toObject(ChatSession::class.java)
            val pinnedStatus = doc.getBoolean("isPinned") ?: doc.getBoolean("pinned") ?: false
            session?.copy(id = doc.id, isPinned = pinnedStatus)
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
