package com.example.project_helper.data.api.deepseek

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DeepSeekApiClient { // Используем object для синглтона

    // !!! ВНИМАНИЕ !!!
    // НЕ ХРАНИТЕ API КЛЮЧИ В КОДЕ ПРОДУКТОВОГО ПРИЛОЖЕНИЯ НАПРЯМУЮ!
    // Это пример для демонстрации. Для реального приложения используйте
    // переменные окружения, BuildConfig или безопасное хранилище на сервере.
    private const val DEEPSEEK_API_KEY = "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" // <-- ЗАМЕНИТЕ НА СВОЙ API КЛЮЧ!

    private const val BASE_URL = "https://openrouter.ai/api/v1/" // Базовый URL API

    // Интерцептор для добавления заголовка авторизации и логирования
    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $DEEPSEEK_API_KEY")
            .addHeader("Content-Type", "application/json") // Обычно добавляется конвертером, но можно явно
            .build()
        chain.proceed(req)
    }

    // Интерцептор для логирования HTTP запросов и ответов (полезно для отладки)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Уровень логирования: BODY - включает заголовки и тело запроса/ответа
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Настраиваем OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // Добавляем интерцептор авторизации первым
        .addInterceptor(loggingInterceptor) // Добавляем интерцептор логирования
        .connectTimeout(30, TimeUnit.SECONDS) // Таймаут соединения
        .readTimeout(30, TimeUnit.SECONDS) // Таймаут чтения
        .writeTimeout(30, TimeUnit.SECONDS) // Таймаут записи
        .build()

    // Создаем экземпляр Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // Устанавливаем базовый URL
        .client(okHttpClient) // Подключаем настроенный OkHttpClient
        .addConverterFactory(GsonConverterFactory.create()) // Используем Gson для парсинга JSON
        .build()

    // Создаем и предоставляем экземпляр API Service
    val apiService: DeepSeekApiService = retrofit.create(DeepSeekApiService::class.java)
}