package com.example.project_helper.data.api.deepseek

import com.example.project_helper.data.auth.api.deepseek.ChatCompletionRequest
import retrofit2.http.Body
import retrofit2.http.POST

// Интерфейс для взаимодействия с DeepSeek API
interface DeepSeekApiService {

    // Отправка запроса на завершение чата (получение ответа нейросети)
    // Используем suspend функцию для работы с корутинами
    @POST("chat/completions") // Путь к эндпоинту чата
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest // Тело запроса с моделью и сообщениями
    ): ChatCompletionResponse // Ожидаемый тип ответа
}