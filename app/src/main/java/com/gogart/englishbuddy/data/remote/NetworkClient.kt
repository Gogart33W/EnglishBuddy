package com.gogart.englishbuddy.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
        .build()

    val geminiApiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}
