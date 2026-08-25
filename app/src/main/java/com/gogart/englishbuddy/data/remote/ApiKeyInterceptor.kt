package com.gogart.englishbuddy.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url()
        
        val urlWithKey = originalUrl.newBuilder()
            .addQueryParameter("key", ApiKeyProvider.getApiKey())
            .build()
            
        val requestWithKey = originalRequest.newBuilder()
            .url(urlWithKey)
            .build()
            
        return chain.proceed(requestWithKey)
    }
}
