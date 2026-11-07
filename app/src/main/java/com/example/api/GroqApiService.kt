package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GroqChatRequest(
    @Json(name = "model") val model: String = "qwen/qwen3.8-27b",
    @Json(name = "messages") val messages: List<GroqMessage>,
    @Json(name = "temperature") val temperature: Double? = 0.3,
    @Json(name = "max_tokens") val maxTokens: Int? = 512
)

@JsonClass(generateAdapter = true)
data class GroqMessage(
    @Json(name = "role") val role: String = "user",
    @Json(name = "content") val content: String? = ""
)

@JsonClass(generateAdapter = true)
data class GroqChatResponse(
    @Json(name = "choices") val choices: List<GroqChoice>? = null
)

@JsonClass(generateAdapter = true)
data class GroqChoice(
    @Json(name = "message") val message: GroqMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GroqModelItem(
    @Json(name = "id") val id: String
)

@JsonClass(generateAdapter = true)
data class GroqModelsResponse(
    @Json(name = "data") val data: List<GroqModelItem>? = null
)

interface GroqApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): GroqChatResponse

    @GET("models")
    suspend fun listModels(
        @Header("Authorization") authorization: String
    ): GroqModelsResponse
}

object GroqClient {
    private const val BASE_URL = "https://api.groq.com/openai/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
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
