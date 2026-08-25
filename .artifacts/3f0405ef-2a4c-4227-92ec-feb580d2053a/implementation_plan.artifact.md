# Implementation Plan - Performance Optimization & Rate Limit Mitigation

Reduce Gemini API load by eliminating redundant calls and optimizing prompt token usage.

## Proposed Changes

### 1. Repository Layer (Optimization)

#### [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt)
- **Delete `generateAndSetSessionTitle`**: Stop using Gemini to generate conversation titles. This saves 1 API call for every new chat.
- **New Local Title Logic**: Implement `generateLocalTitle(message: String)` which takes the first 4-5 words of the user message, cleans punctuation, and capitalizes it.
- **Prompt Pruning**: Slightly condense the system instruction to reduce token overhead.

### 2. Network Layer (Retry Refinement)

#### [MODIFY] [RetryInterceptor.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/remote/RetryInterceptor.kt)
- **Reduce Backoff**: Change initial delay from 2s to 1s. The current 2-4-8 progression makes the "long wait" feel like the app is hung.
- **Max Retries**: Keep it at 3, but with the faster progression (1s, 2s, 4s).

### 3. UI Layer (UX)

#### [MODIFY] [ChatScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/ChatScreen.kt)
- **Word Tap Debounce**: Ensure that if a dictionary lookup is already in progress, tapping another word doesn't fire a new request.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure compilation.

### Manual Verification
1. **Speed**: Start a new chat and verify Buddy responds noticeably faster (due to 1 request instead of 2).
2. **Titles**: Verify the drawer shows a readable title generated from your first message.
3. **Rate Limits**: Stress-test by sending messages; verify the app handles 429s more gracefully with shorter waits.
