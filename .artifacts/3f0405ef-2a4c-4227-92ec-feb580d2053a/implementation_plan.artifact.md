# Implementation Plan - API Key Rotation & Stabilization

Implement a load-balancing system that rotates multiple Gemini API keys to bypass individual rate limits and support concurrent users.

## User Review Required

> [!IMPORTANT]
> **API Keys Update**: You will need to update your `local.properties` file. Instead of one key, provide multiple keys separated by commas:
> `GEMINI_API_KEYS=key1,key2,key3`

> [!NOTE]
> **Smart Failover**: If one key returns a `429 (Too Many Requests)`, the app will automatically switch to the next key and retry the request immediately, without making the user wait.

## Proposed Changes

### 1. Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/build.gradle.kts)
- Change `GEMINI_API_KEY` (String) to `GEMINI_API_KEYS` (String representing a comma-separated list).
- Change `GEMINI_MODEL` to `gemini-1.5-flash` (or keep 2.5 if it's actually working in your environment, but 1.5 is standard stable).

### 2. Network Layer (Rotation Logic)

#### [NEW] [ApiKeyProvider.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/remote/ApiKeyProvider.kt)
- A thread-safe singleton that manages the list of keys.
- Methods: `getApiKey()` (returns current), `nextKey()` (rotates to the next one).

#### [MODIFY] [RetryInterceptor.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/remote/RetryInterceptor.kt)
- Update logic: When a `429` is detected:
    1. Call `ApiKeyProvider.nextKey()`.
    2. Retry the request with the new key **immediately** (no delay if keys are available).
    3. If all keys are exhausted/rate-limited, then fallback to exponential backoff.

#### [MODIFY] [NetworkClient.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/remote/NetworkClient.kt)
- Remove the `key` query parameter from the Retrofit interface or handle it via a new `ApiKeyInterceptor`.

### 3. Repository Layer

#### [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt)
- Remove `BuildConfig.GEMINI_API_KEY` from direct calls. The interceptor will now handle key injection.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure compilation.

### Manual Verification
1. **Load Balancing**: Monitor logs to see different API keys being used for consecutive requests.
2. **Failover**: Simulate a 429 (or use a key that is already limited) and verify the app silently switches keys and succeeds.
3. **Capacity**: Verify the app handles multiple rapid requests (simulating 10 users) across the pool of keys.
