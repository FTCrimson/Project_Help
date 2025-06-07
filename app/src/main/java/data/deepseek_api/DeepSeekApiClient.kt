package com.example.project_helper.data.api.deepseek

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DeepSeekApiClient {

    private const val DEEPSEEK_API_KEY = "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" // <-- ЗАМЕНИТЕ НА СВОЙ API КЛЮЧ!

    private const val BASE_URL = "https://openrouter.ai/api/v1/"

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $DEEPSEEK_API_KEY")
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(req)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: DeepSeekApiService = retrofit.create(DeepSeekApiService::class.java)
}