package com.gogart.englishbuddy.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.random.Random

class RetryInterceptor : Interceptor {
    private val maxRetries = 5 // Increased for multi-key support
    private val initialDelayMillis = 1000L

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0

        while (response.code() == 429 && tryCount < maxRetries) {
            tryCount++
            Log.w("RetryInterceptor", "HTTP 429 detected (try $tryCount/$maxRetries). Rotating API key...")

            // Rotate Key
            ApiKeyProvider.nextKey()
            
            // Re-build request with new key (the ApiKeyInterceptor will pick up the new key from Provider)
            // But we need to make sure the "key" query param is updated.
            // Actually, proceeding with the same request object is fine if the ApiKeyInterceptor 
            // is placed AFTER this one in the chain, OR if we rebuild it here.
            // Let's assume ApiKeyInterceptor is at the end.
            
            val newUrl = request.url().newBuilder()
                .setQueryParameter("key", ApiKeyProvider.getApiKey())
                .build()
            
            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

            response.close()
            
            // If we have more than 1 key, we can retry faster. 
            // If we only have 1 key or we've rotated through all, wait longer.
            val delay = if (tryCount < ApiKeyProvider.getKeyCount()) {
                500L // Fast switch if we have backup keys
            } else {
                val backoff = initialDelayMillis * 2.0.pow((tryCount - ApiKeyProvider.getKeyCount()).coerceAtLeast(0).toDouble()).toLong()
                backoff + Random.nextLong(0, 1000)
            }

            try {
                TimeUnit.MILLISECONDS.sleep(delay)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return response
            }

            request = newRequest
            response = chain.proceed(request)
        }

        return response
    }
}
