# Implementation Plan - Fix 404 & Model Refactoring

Address the 404 error by switching to a more stable model (`gemini-3.5-flash-lite`) and refactoring the configuration into a Kotlin object to prevent future regressions.

## User Review Required

> [!IMPORTANT]
> **Model Switch**: I am switching the model to `gemini-3.5-flash-lite` as suggested by your external analysis. This model is reported to be more stable and long-lived.

> [!NOTE]
> **Configuration Refactoring**: I will move the model and API configuration from `build.gradle.kts` into a dedicated `AppConfig` Kotlin object. This provides a single source of truth that is easier to inspect and maintain without needing a full build to see what's being used.

## Proposed Changes

### 1. Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/build.gradle.kts)
- Update `GEMINI_MODEL` to `"gemini-3.5-flash-lite"`.

### 2. Code Refactoring (Config Management)

#### [NEW] [AppConfig.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/util/AppConfig.kt)
- Create an `AppConfig` object.
- Store `MODEL_NAME` (mapping to `BuildConfig.GEMINI_MODEL`).
- Add comments with model expiration dates and links to Google AI Studio documentation to serve as a "trigger" for future updates.

### 3. Repository Layer

#### [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt)
- Use `AppConfig.MODEL_NAME` instead of `BuildConfig.GEMINI_MODEL` directly.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the new model name and `AppConfig` are correctly integrated.

### Manual Verification
1. **Connectivity**: Send a message to Buddy and verify that the 404 error is gone and a valid response is received.
2. **Dictionary**: Tap a word and verify the definition still works with the new model.
3. **Logs**: Check for any successful API requests to `gemini-3.5-flash-lite`.
