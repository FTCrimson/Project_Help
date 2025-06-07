package data.auth.commandchat

import java.util.Date

data class Chat(
    val id: String = "",
    val name: String = "New Chat",
    val creatorId: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Date? = null, // Делаем Nullable на случай, если поле отсутствует
    val lastMessageTimestamp: Date? = null // Делаем Nullable
)
