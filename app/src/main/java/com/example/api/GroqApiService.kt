package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GroqChatRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>,
    val temperature: Double? = 0.5,
    @param:Json(name = "max_tokens") val maxTokens: Int? = 512
)

@JsonClass(generateAdapter = true)
data class GroqMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class GroqChatResponse(
    val choices: List<GroqChoice>? = null
)

@JsonClass(generateAdapter = true)
data class GroqChoice(
    val message: GroqMessage? = null,
    @param:Json(name = "finish_reason") val finishReason: String? = null
)

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): GroqChatResponse
}

object GroqClient {
    private const val BASE_URL = "https://api.groq.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }
}
