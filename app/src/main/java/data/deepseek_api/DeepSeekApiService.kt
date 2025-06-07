package com.example.project_helper.data.api.deepseek

import retrofit2.http.Body
import retrofit2.http.POST

interface DeepSeekApiService {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}