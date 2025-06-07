package com.example.project_helper.features.commandchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import data.auth.commandchat.Chat
import data.auth.commandchat.ChatMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ChatViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _chatInfo = MutableLiveData<Chat>()
    val chatInfo: LiveData<Chat> = _chatInfo

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _addMemberResult = MutableLiveData<Result<Unit>?>()
    val addMemberResult: LiveData<Result<Unit>?> = _addMemberResult

    fun loadChatMessages(chatId: String) {
        if (auth.currentUser == null) {
            _error.value = "Пользователь не авторизован"
            return
        }

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    return@addSnapshotListener
                }

                val messagesList = mutableListOf<ChatMessage>()
                snapshot?.documents?.forEach { document ->
                    val message = document.toObject(ChatMessage::class.java)
                    message?.let { messagesList.add(it) }
                }
                _messages.value = messagesList
            }
    }

    fun loadChatInfo(chatId: String) {
        db.collection("chats")
            .document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    return@addSnapshotListener
                }

                snapshot?.toObject(Chat::class.java)?.let {
                    _chatInfo.value = it
                }
            }
    }

    fun sendMessage(chatId: String, senderId: String, text: String) {
        if (auth.currentUser == null) {
            _error.value = "Пользователь не авторизован"
            return
        }

        db.collection("chats").document(chatId).get()
            .addOnSuccessListener { chatDoc ->
                val chat = chatDoc.toObject(Chat::class.java)
                if (chat == null || !chat.members.contains(senderId)) {
                    _error.value = "Вы не участник этого чата"
                    return@addOnSuccessListener
                }

                val message = ChatMessage(
                    chatId = chatId,
                    senderId = senderId,
                    text = text,
                    timestamp = Date()
                )

                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        db.collection("chats").document(chatId)
                            .update("lastMessageTimestamp", FieldValue.serverTimestamp())
                    }
                    .addOnFailureListener { e ->
                        handleFirestoreError(e)
                    }
            }
            .addOnFailureListener { e ->
                handleFirestoreError(e)
            }
    }

    fun addMemberToChat(chatId: String, username: String) {
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _addMemberResult.value = Result.failure(Exception("Пользователь не найден"))
                    return@launch
                }

                val userDoc = querySnapshot.documents[0]
                val userId = userDoc.id

                val chatDoc = db.collection("chats").document(chatId).get().await()
                val chat = chatDoc.toObject(Chat::class.java) ?: throw Exception("Чат не найден")

                if (auth.currentUser?.uid != chat.creatorId) {
                    _addMemberResult.value = Result.failure(Exception("Только создатель чата может добавлять участников"))
                    return@launch
                }

                db.collection("chats").document(chatId)
                    .update("members", FieldValue.arrayUnion(userId))
                    .await()

                _addMemberResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _addMemberResult.value = Result.failure(e)
            }
        }
    }

    private fun handleFirestoreError(exception: Exception) {
        when (exception) {
            is FirebaseFirestoreException -> {
                if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    _error.value = "Нет доступа к чату"
                } else {
                    _error.value = "Ошибка Firestore: ${exception.message}"
                }
            }
            else -> {
                _error.value = "Ошибка: ${exception.message}"
            }
        }
    }

    fun resetAddMemberResult() {
        _addMemberResult.value = null
    }
}