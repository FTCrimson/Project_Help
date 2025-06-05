package com.example.project_helper.data.auth.api.deepseek

import com.google.gson.annotations.SerializedName

// Представляет одно сообщение в беседе (как в запросе, так и в ответе)
data class Message(
    @SerializedName("role") val role: String, // Роль: "user", "system", "assistant"
    @SerializedName("content") val content: String // Текст сообщения
) {
    // Для удобства в адаптере, чтобы различать отправителя
    val isUser: Boolean get() = role == "user"
    val isBot: Boolean get() = role == "assistant"
}