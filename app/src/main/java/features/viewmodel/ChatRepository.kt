package com.example.project_helper.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import data.commandchat.Chat
import data.commandchat.ChatMessage
import data.commandchat.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val chatsCollection = firestore.collection("chats")
    private val usersCollection = firestore.collection("users")

    fun getChatInfo(chatId: String): Flow<Chat?> = callbackFlow {
        val docRef = chatsCollection.document(chatId)
        val subscription = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val chat = snapshot.toObject<Chat>()?.copy(id = snapshot.id)
                trySend(chat)
            } else {
                trySend(null)
            }
        }
        awaitClose { subscription.remove() }
    }

    fun getChatMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val messagesRef = chatsCollection.document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val subscription = messagesRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<ChatMessage>()?.copy(id = doc.id)
                }
                trySend(messages)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(chatId: String, message: ChatMessage) {
        val messagesRef = chatsCollection.document(chatId).collection("messages")
        messagesRef.add(message).await()
    }

    suspend fun updateChatName(chatId: String, newName: String) {
        val chatRef = chatsCollection.document(chatId)
        chatRef.update("name", newName).await()
    }

    suspend fun addMemberToChat(chatId: String, username: String): Result<Unit> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Пользователь с таким username не найден"))
            }

            val userDoc = querySnapshot.documents[0]
            val userId = userDoc.id

            chatsCollection.document(chatId)
                .update("members", FieldValue.arrayUnion(userId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject<User>()?.copy(id = userId)
        } catch (e: Exception) {
            null
        }
    }
}

