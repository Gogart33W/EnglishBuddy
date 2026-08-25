# Implementation Plan - stabilization, Gamification & Learning Enhancements

Comprehensive upgrade focusing on network stability, gamification (streaks/activity), learning tools (mistake validation), and UI/UX optimization.

## User Review Required

> [!IMPORTANT]
> **Git Soft Reset**: I will perform a `git reset --soft 07a4f37`. This will keep all your current file changes in the staging area but will rewrite the commit history as requested to clean up intermediate commits.

> [!WARNING]
> **Network Timeouts**: Increasing timeouts to 60 seconds may cause the UI to appear "stuck" longer if the Gemini API is unresponsive, but it will prevent frequent socket drops on slow connections.

## Proposed Changes

### 1. Stabilization & Network Layer
- **[MODIFY] `NetworkClient.kt`**: Configure `OkHttpClient` with 60-second timeouts (`connect`, `read`, `write`).
- **[MODIFY] `GeminiApiService.kt`**: Update `@POST` to use dynamic `{model}` path parameter.
- **[MODIFY] `app/build.gradle.kts`**: Ensure `GEMINI_MODEL` is passed correctly in `BuildConfig`.

### 2. Gamification & Activity Tracking
- **[NEW] `DailyActivityEntity.kt`**: Store practice minutes, messages sent, and completed lessons per day.
- **[NEW] `ActivityTracker.kt`**: Lifecycle-aware component to measure active study time.
- **[MODIFY] `UserProfileEntity.kt`**: Add `currentStreak` and `longestStreak` fields.
- **[NEW] `CalendarScreen.kt`**: A monthly grid UI showing activity intensity and streak flame 🔥.

### 3. Learning Tools & AI Persona
- **[MODIFY] `ChatRepositoryImpl.kt`**:
    - **Strict Ukrainian**: Enforce "strictly UKRAINIAN" translations in brackets via the system prompt.
    - **Smart Titles**: Implement logic to generate a 2-4 word human title from the first message of a new session.
- **[MODIFY] `MistakesScreen.kt`**:
    - Add Delete (Trash) action.
    - Add "Check" validation logic for corrections with success/error feedback.
- **[NEW] `PlacementTestScreen.kt`**: 10-question multiple-choice quiz to determine CEFR level.

### 4. UI/UX Optimization
- **[MODIFY] `ChatScreen.kt`**:
    - Disable Send button during `isLoading`.
    - Apply `Modifier.imePadding()` to ensure the input field isn't clipped by the keyboard.
- **[MODIFY] `ChatViewModel.kt`**: Manage `lastActiveSessionId` persistence via DataStore.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify KSP and build integrity.

### Manual Verification
1. **Network**: Verify Gemini calls no longer timeout prematurely on large prompts.
2. **Activity**: Stay on the Chat screen for 60s and verify `activeMinutes` increments in the DB.
3. **Streak**: Check the Calendar for activity markers.
4. **Mistakes**: Successfully "solve" a mistake and verify it is removed from the list.
5. **Titles**: Verify a new chat gets a meaningful title (e.g., "Space travel") instead of a timestamp.
