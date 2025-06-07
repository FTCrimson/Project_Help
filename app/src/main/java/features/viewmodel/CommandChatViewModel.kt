package com.example.project_helper.features.commandchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project_helper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import data.auth.commandchat.Chat
import java.util.Date

class CommandChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> get() = _chats

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var chatsListener: ListenerRegistration? = null

    fun loadUserChats(userId: String) {
        chatsListener?.remove()

        chatsListener = db.collection("chats")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _error.value = "Error loading chats: ${error.message}"
                    return@addSnapshotListener
                }

                val chatList = mutableListOf<Chat>()
                snapshots?.forEach { document ->
                    val chat = document.toObject(Chat::class.java).copy(id = document.id)
                    chatList.add(chat)
                }
                _chats.value = chatList
            }
    }

    fun createChat(chat: Chat, callback: (String?) -> Unit) {
        val userId = auth.currentUser?.uid ?: ""
        if (userId.isEmpty()) {
            _error.value = "User not authenticated"
            callback(null)
            return
        }

        val newChat = chat.copy(
            creatorId = userId,
            members = listOf(userId),
            createdAt = Date()
        )

        db.collection("chats")
            .add(newChat)
            .addOnSuccessListener { documentRef ->
                callback(documentRef.id)
            }
            .addOnFailureListener { e ->
                _error.value = "Error creating chat: ${e.message}"
                callback(null)
            }
    }

    fun addMemberToChat(chatId: String, userId: String) {
        db.collection("chats").document(chatId)
            .update("members", FirebaseUtils.arrayUnion(userId))
            .addOnSuccessListener {
                // Success
            }
            .addOnFailureListener { e ->
                _error.value = "Error adding member: ${e.message}"
            }
    }

    override fun onCleared() {
        super.onCleared()
        chatsListener?.remove()
    }
}