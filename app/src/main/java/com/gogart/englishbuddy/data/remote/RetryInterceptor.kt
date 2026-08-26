package com.gogart.englishbuddy.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.random.Random

class RetryInterceptor : Interceptor {
    private val maxRetries = 5
    private val initialDelayMillis = 1000L

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var tryCount = 0
        var lastResponse: Response? = null

        while (tryCount < maxRetries) {
            try {
                val response = chain.proceed(request)
                
                // If success or non-retryable error, return
                if (response.isSuccessful || !isRetryable(response.code())) {
                    return response
                }
                
                // If we reach here, it's 429 or 5xx
                lastResponse = response
                Log.w("RetryInterceptor", "Retryable error ${response.code()} (try ${tryCount + 1}). Rotating key...")
                response.close()
                
            } catch (e: Exception) {
                if (e is SocketTimeoutException || e is java.io.IOException) {
                    Log.w("RetryInterceptor", "Network error/timeout: ${e.message} (try ${tryCount + 1}). Rotating key...")
                } else {
                    throw e
                }
            }

            tryCount++
            
            // Rotate Key and Rebuild Request
            ApiKeyProvider.nextKey()
            val newUrl = request.url().newBuilder()
                .setQueryParameter("key", ApiKeyProvider.getApiKey())
                .build()
            request = request.newBuilder().url(newUrl).build()

            // Backoff
            val delay = if (tryCount < ApiKeyProvider.getKeyCount()) {
                300L // Quick switch if we have more keys
            } else {
                val backoff = initialDelayMillis * 2.0.pow((tryCount - ApiKeyProvider.getKeyCount()).coerceAtLeast(0).toDouble()).toLong()
                backoff + Random.nextLong(0, 1000)
            }

            try {
                TimeUnit.MILLISECONDS.sleep(delay)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }

        return lastResponse ?: chain.proceed(chain.request())
    }

    private fun isRetryable(code: Int): Boolean {
        return code == 429 || code >= 500
    }
}
