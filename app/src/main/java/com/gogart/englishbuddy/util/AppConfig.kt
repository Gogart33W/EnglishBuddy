package com.gogart.englishbuddy.util

import com.gogart.englishbuddy.BuildConfig

/**
 * Central configuration for EnglishBuddy AI.
 * 
 * Model Lifecycle Reference:
 * - gemini-1.5-flash: DEPRECATED (404)
 * - gemini-3.5-flash-lite: UNSTABLE in some regions (Timeouts)
 * - gemini-2.5-flash: Primary stable model for current API keys.
 * 
 * To update keys, use `local.properties`: GEMINI_API_KEYS=key1,key2...
 */
object AppConfig {
    const val MODEL_NAME = BuildConfig.GEMINI_MODEL
    
    // For manual debugging if needed
    const val DEFAULT_FALLBACK_MODEL = "gemini-1.5-flash"
}
