package com.gogart.englishbuddy.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.random.Random

class RetryInterceptor : Interceptor {
    private val maxRetries = 3
    private val initialDelayMillis = 2000L

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0

        while (response.code() == 429 && tryCount < maxRetries) {
            tryCount++
            
            // Respect Retry-After header if present
            val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 0L
            val delay = if (retryAfter > 0) {
                retryAfter * 1000
            } else {
                // Exponential backoff with jitter
                val backoff = initialDelayMillis * 2.0.pow(tryCount.toDouble()).toLong()
                val jitter = Random.nextLong(0, 1000)
                backoff + jitter
            }

            response.close()
            
            try {
                TimeUnit.MILLISECONDS.sleep(delay)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return response
            }

            response = chain.proceed(request)
        }

        return response
    }
}
