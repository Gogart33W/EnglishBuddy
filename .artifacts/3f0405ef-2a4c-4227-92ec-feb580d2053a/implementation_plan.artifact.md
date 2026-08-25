# Implementation Plan - Fix HTTP 429 (Rate Limiting)

Implement a robust retry strategy and request smoothing to handle Gemini API rate limits (HTTP 429).

## User Review Required

> [!IMPORTANT]
> **Retry Strategy**: I will implement an exponential backoff starting at 2 seconds. In the worst case (3 retries), a single request might take up to ~15 seconds to complete. The "Send" button will remain disabled during this time.

> [!NOTE]
> **Sequential Requests**: Title generation will now happen only after Buddy's first response is successfully received, preventing two simultaneous hits to the API.

## Proposed Changes

### 1. Network Layer (OkHttp)

#### [NEW] [RetryInterceptor.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/remote/RetryInterceptor.kt)
- Create a custom interceptor that:
    - Detects `HTTP 429`.
    - Reads `Retry-After` header (if available).
    - Implements exponential backoff with jitter.
    - Limits retries to 3 attempts.

#### [MODIFY] [NetworkClient.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/remote/NetworkClient.kt)
- Add the `RetryInterceptor` to the `OkHttpClient.Builder`.

### 2. Repository Layer

#### [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt)
- **Request Smoothing**: Move `generateAndSetSessionTitle` call inside the `try` block, *after* receiving the candidate response from Gemini.
- **Error Mapping**: Specifically catch `HttpException` with code 429 and return a specialized failure message: `"Buddy is a bit overwhelmed. Please wait 10-15 seconds and try again."`

### 3. ViewModel Layer

#### [MODIFY] [ChatViewModel.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/viewmodel/ChatViewModel.kt)
- Ensure the error message from the repository is correctly propagated to the `uiState.error`.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure compilation.

### Manual Verification
1. **Stress Test**: Rapidly send messages and verify the retry interceptor kicks in (can be seen in Logcat).
2. **First Message**: Start a new chat, send a message, and verify that the title is generated *after* Buddy responds, rather than simultaneously.
3. **User Message**: Verify that hitting the limit shows the friendly "Buddy is overwhelmed" message instead of a raw "HTTP 429".
