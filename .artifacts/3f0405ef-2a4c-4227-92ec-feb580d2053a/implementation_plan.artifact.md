# Implementation Plan - Performance & Timeout Fix

Address the long thinking times and timeouts by switching to a more optimized model, refining the retry logic, and increasing network resilience.

## User Review Required

> [!IMPORTANT]
> **Model Switch**: I am switching from `gemini-3.5-flash-lite` to **`gemini-2.5-flash-lite`**. Based on my latest research, 3.5 might be unstable or non-existent in your current project region. `2.5-flash-lite` is designed specifically for ultra-low latency and is less likely to timeout.

> [!WARNING]
> **Retry Overload**: If you have many API keys from the *same* project, they share a "Daily Limit" (RPD). If you've reached 30/20 RPD as shown in your screenshot, ALL keys in that project will fail until tomorrow. If your keys are from different projects, the rotation will work perfectly.

## Proposed Changes

### 1. Build & Config

#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/build.gradle.kts)
- Update `GEMINI_MODEL` to `"gemini-2.5-flash-lite"`.

#### [MODIFY] [AppConfig.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/util/AppConfig.kt)
- Update the documentation comments to reflect the switch to `2.5-flash-lite`.

### 2. Network Layer (Resilience)

#### [MODIFY] [RetryInterceptor.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/remote/RetryInterceptor.kt)
- **Catch Timeouts**: Update the interceptor to catch `SocketTimeoutException` and try the next API key instead of just failing.
- **Retry on 5xx**: Add retries for `500`, `502`, and `503` errors, as these are often transient server-side hiccups.
- **Fast Fail**: If a key returns a terminal error (like 401 or 403), stop using it for the current session.

### 3. Repository Layer (Optimization)

#### [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt)
- **Simplify Schema**: Ensure the `ResponseSchema` is as lightweight as possible to reduce "reasoning" time on the server.
- **Context Pruning**: Verify history context is strictly limited to ensure the fastest possible inference.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify compilation.

### Manual Verification
1. **Speed**: Send a message and measure the "thinking" time. It should be significantly faster with the `flash-lite` model.
2. **Timeout Handling**: (Simulated) Verify that if one key is slow/timed out, the app rotates to the next one automatically.
3. **Connectivity**: Verify the 404/429/Timeout cycle is broken and Buddy responds consistently.
