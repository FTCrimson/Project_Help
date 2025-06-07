package com.example.project_helper.data.api.deepseek

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Message(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
    val timestamp: Long = Date().time
) {
    val isUser: Boolean get() = role == "user"
    val isBot: Boolean get() = role == "assistant"
    val isSystem: Boolean get() = role == "system"
}
