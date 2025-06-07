package data.auth.commandchat

import java.util.Date

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Date? = null
)
