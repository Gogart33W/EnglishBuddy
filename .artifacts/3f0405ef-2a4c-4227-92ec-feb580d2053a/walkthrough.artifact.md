# Walkthrough - Fix HTTP 429 (Rate Limiting)

I have implemented a robust solution to handle Gemini API rate limits (HTTP 429) through strategic retries and request smoothing.

## Key Changes

### 1. Robust Retry Mechanism
- **[NEW] `RetryInterceptor.kt`**: Implemented a custom OkHttp interceptor that automatically handles `HTTP 429` errors.
    - **Exponential Backoff**: If Buddy is busy, the app waits with increasing delays (2s, 4s, 8s) before retrying.
    - **Jitter**: Added a random delay component to prevent synchronized retry spikes.
    - **Header Respect**: The interceptor respects the `Retry-After` server header if provided.
- **`NetworkClient.kt`**: Registered the interceptor in the `OkHttpClient` pipeline.

### 2. Request Smoothing (Sequential Logic)
- **`ChatRepositoryImpl.kt`**: Previously, the app sent two simultaneous requests (Buddy's response + Chat Title generation) when a new conversation started. This often triggered the rate limit.
- **Sequentialization**: Updated `sendMessage` to only trigger title generation **after** Buddy's conversational response is successfully received.

### 3. User-Friendly Error Mapping
- **Friendly Feedback**: Updated the repository to catch 429 errors and map them to a helpful message: *"Buddy is a bit overwhelmed. Please wait 10-15 seconds and try again."* instead of a technical error code.

## Verification Results

### Automated Tests
- `./gradlew assembleDebug` passed successfully.

### Manual Verification (Expected behavior)
1. **New Chat**: Sending the first message now results in Buddy responding first, then the title updating in the sidebar a second later (no more simultaneous hits).
2. **Rate Limit**: If the limit is hit, the app will silently retry up to 3 times. If it still fails, the user gets a friendly "overwhelmed" notification.

> [!TIP]
> The exponential backoff ensures that we don't spam the API when the quota is reached, increasing the chances of a successful retry.
