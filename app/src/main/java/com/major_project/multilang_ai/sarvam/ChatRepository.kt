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
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w("ChatRepository", "No user logged in! Chats will not be saved/fetched correctly.")
            // For development purposes, you might return a "guest" ID if not using Auth yet
            // return "guest_user" 
        }
        return uid
    }

    /**
     * Saves a chat session to Firestore under the user's ID
     */
    suspend fun saveChat(session: ChatSession) {
        val userId = getSafeUserId() ?: return
        val chatData = session.copy(userId = userId, lastTimestamp = System.currentTimeMillis())
        
        try {
            if (session.id.isEmpty()) {
                db.collection("users").document(userId)
                    .collection("chats").add(chatData).await()
            } else {
                db.collection("users").document(userId)
                    .collection("chats").document(session.id).set(chatData).await()
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error saving chat: ${e.message}", e)
        }
    }

    /**
     * Retrieves all chat history for the current user
     */
    suspend fun getChatHistory(): List<ChatSession> {
        val userId = getSafeUserId() ?: return emptyList()
        return try {
            // NOTE: This query requires a Composite Index in Firestore.
            // Check Logcat for a link to create it if it fails.
            val snapshot = db.collection("users").document(userId)
                .collection("chats")
                .orderBy("isPinned", Query.Direction.DESCENDING)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ChatSession::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching history: ${e.message}", e)
            // If the error is "FAILED_PRECONDITION", it's a missing index.
            emptyList()
        }
    }

    /**
     * Pins or unpins a chat
     */
    suspend fun togglePin(chatId: String, isPinned: Boolean) {
        val userId = getSafeUserId() ?: return
        try {
            db.collection("users").document(userId)
                .collection("chats").document(chatId)
                .update("isPinned", isPinned).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error toggling pin: ${e.message}")
        }
    }
}
