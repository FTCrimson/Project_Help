package com.example.project_helper.data.api.deepseek

import com.example.project_helper.data.auth.api.deepseek.Message
import com.google.gson.annotations.SerializedName

// Модель для получения ответа от API чата
data class ChatCompletionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val obj: String, // Имя поля `object` изменено на `obj` из-за ключевого слова в Kotlin
    @SerializedName("created") val created: Long,
    @SerializedName("model") val model: String,
    @SerializedName("choices") val choices: List<Choice>, // Список вариантов ответов
    @SerializedName("usage") val usage: Usage? // Информация об использовании токенов (опционально)
)

// Часть ответа: один из возможных вариантов ответа модели
data class Choice(
    @SerializedName("index") val index: Int,
    @SerializedName("message") val message: Message, // Сообщение от модели (использует класс Message из другого файла)
    @SerializedName("finish_reason") val finishReason: String? // Причина завершения генерации
)

// Часть ответа: информация об использовании токенов
data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completion_tokens: Int,
    @SerializedName("total_tokens") val total_tokens: Int
)