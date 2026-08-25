package com.gogart.englishbuddy.data.remote

import com.gogart.englishbuddy.BuildConfig
import java.util.concurrent.atomic.AtomicInteger

object ApiKeyProvider {
    private val keys = BuildConfig.GEMINI_API_KEYS.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    
    private val currentIndex = AtomicInteger(0)

    fun getApiKey(): String {
        if (keys.isEmpty()) return ""
        return keys[currentIndex.get() % keys.size]
    }

    fun nextKey(): String {
        if (keys.isEmpty()) return ""
        val next = currentIndex.incrementAndGet()
        return keys[next % keys.size]
    }
    
    fun getKeyCount(): Int = keys.size
}
