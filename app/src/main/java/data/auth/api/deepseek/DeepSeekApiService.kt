package com.example.project_helper.data.api.deepseek

import com.example.project_helper.data.auth.api.deepseek.ChatCompletionRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepSeekApiService {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}