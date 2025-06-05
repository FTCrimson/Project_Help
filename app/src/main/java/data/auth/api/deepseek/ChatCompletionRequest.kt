package com.example.project_helper.data.auth.api.deepseek

import com.google.gson.annotations.SerializedName

// Модель для отправки запроса к API чата
data class ChatCompletionRequest(
    @SerializedName("model") val model: String, // Модель, например "deepseek-reasoner"
    @SerializedName("messages") val messages: List<Message>, // Список сообщений для контекста беседы
    @SerializedName("stream") val stream: Boolean = false // Включать ли потоковую передачу (false для простого ответа)
)