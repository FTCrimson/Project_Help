package com.example.project_helper.data.auth.api.deepseek

import com.example.project_helper.data.api.deepseek.Message
import com.google.gson.annotations.SerializedName

data class ChatCompletionRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("stream") val stream: Boolean = false
)