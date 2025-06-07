package com.example.project_helper.data.api.deepseek

import com.example.project_helper.data.FirestoreApiKeyFetcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DeepSeekApiClient {

    private var deepseekApiKey: String? = null
    private const val BASE_URL = "https://openrouter.ai/api/v1/"

    private var initializedOkHttpClient: OkHttpClient? = null
    var apiService: DeepSeekApiService? = null
        private set

    suspend fun initialize() {
        if (deepseekApiKey != null) return

        val fetcher = FirestoreApiKeyFetcher()
        deepseekApiKey = fetcher.fetchDeepSeekApiKey()

        if (deepseekApiKey == null) {
            println("Не удалось получить API ключ из Firestore.")
            // Возможно, здесь нужно выбросить исключение или предпринять другие действия
            return
        }

        val authInterceptor = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $deepseekApiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(req)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        initializedOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(initializedOkHttpClient!!)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(DeepSeekApiService::class.java)
    }
}
