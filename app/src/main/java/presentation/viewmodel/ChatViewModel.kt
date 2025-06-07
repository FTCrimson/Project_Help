package com.example.project_helper.presentation.viewmodel.commandchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_helper.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import data.commandchat_api.Chat
import data.commandchat_api.ChatMessage
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Date

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _chatInfo = MutableLiveData<Chat?>()
    val chatInfo: LiveData<Chat?> = _chatInfo

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _addMemberResult = MutableLiveData<Result<Unit>?>()
    val addMemberResult: LiveData<Result<Unit>?> = _addMemberResult

    private val _updateChatNameResult = MutableLiveData<Result<Unit>?>()
    val updateChatNameResult: LiveData<Result<Unit>?> = _updateChatNameResult

    private var chatId: String = ""

    fun setChatId(id: String) {
        if (this.chatId != id) {
            this.chatId = id
            if (id.isNotEmpty()) {
                loadChatMessages(id)
                loadChatInfo(id)
            }
        }
    }

    private fun loadChatMessages(chatId: String) {
        if (currentUserId.isEmpty()) {
            _error.value = "Пользователь не авторизован"
            return
        }
        viewModelScope.launch {
            chatRepository.getChatMessages(chatId)
                .distinctUntilChanged()
                .catch { e -> handleFirestoreError(e as Exception) }
                .collectLatest { messagesList ->
                    _messages.value = messagesList
                }
        }
    }

    private fun loadChatInfo(chatId: String) {
        if (currentUserId.isEmpty()) {
            _error.value = "Пользователь не авторизован"
            return
        }
        viewModelScope.launch {
            chatRepository.getChatInfo(chatId)
                .distinctUntilChanged()
                .catch { e -> handleFirestoreError(e as Exception) }
                .collectLatest { chat ->
                    _chatInfo.value = chat
                    if (chat == null) {
                        _error.value = "Чат не найден"
                    } else if (!chat.members.contains(currentUserId)) {
                        _error.value = "Нет доступа к чату"
                    } else {
                        _error.value = null
                    }
                }
        }
    }

    fun sendMessage(text: String) {
        if (currentUserId.isEmpty() || chatId.isEmpty()) {
            _error.value = "Не удалось отправить сообщение: Пользователь не авторизован или ID чата не установлен."
            return
        }

        val currentChat = _chatInfo.value
        if (currentChat == null || !currentChat.members.contains(currentUserId)) {
            _error.value = "Вы не участник этого чата. Невозможно отправить сообщение."
            return
        }

        val message = ChatMessage(
            chatId = chatId,
            senderId = currentUserId,
            text = text,
            timestamp = Date()
        )

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(chatId, message)
                // Optional: Update last message timestamp
                // chatRepository.updateChatLastMessageTimestamp(chatId, Date())
            } catch (e: Exception) {
                handleFirestoreError(e)
            }
        }
    }

    fun addMemberToChat(username: String) {
        if (currentUserId.isEmpty() || chatId.isEmpty()) {
            _addMemberResult.value = Result.failure(Exception("Пользователь не авторизован или ID чата не установлен"))
            return
        }
        val currentChat = _chatInfo.value
        if (currentChat == null || currentChat.creatorId != currentUserId) {
            _addMemberResult.value = Result.failure(Exception("Только создатель чата может добавлять участников"))
            return
        }

        viewModelScope.launch {
            val result = chatRepository.addMemberToChat(chatId, username)
            _addMemberResult.value = result
        }
    }

    fun updateChatName(newName: String) {
        if (currentUserId.isEmpty() || chatId.isEmpty()) {
            _updateChatNameResult.value = Result.failure(Exception("Пользователь не авторизован или ID чата не установлен"))
            return
        }
        val currentChat = _chatInfo.value
        if (currentChat == null || currentChat.creatorId != currentUserId) {
            _updateChatNameResult.value = Result.failure(Exception("Только создатель чата может изменять название"))
            return
        }

        viewModelScope.launch {
            try {
                chatRepository.updateChatName(chatId, newName)
                _updateChatNameResult.value = Result.success(Unit)
            } catch (e: Exception) {
                handleFirestoreError(e)
                _updateChatNameResult.value = Result.failure(e)
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

    fun resetUpdateChatNameResult() {
        _updateChatNameResult.value = null
    }

    fun resetAddMemberResult() {
        _addMemberResult.value = null
    }

    fun canEditChat(chat: Chat?): Boolean {
        return chat != null && chat.creatorId == currentUserId
    }

    fun isCurrentUserMember(chat: Chat?): Boolean {
        return chat != null && chat.members.contains(currentUserId)
    }
}

