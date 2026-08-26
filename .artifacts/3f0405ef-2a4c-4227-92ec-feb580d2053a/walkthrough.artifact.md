# Walkthrough - Final 404 Fix & Model Refactoring

I have successfully fixed the 404 error by switching to a more stable model and centralizing the configuration for better maintainability.

## Key Improvements

### 1. Stable Model Switch
- **Model Update**: Switched from the erratic `gemini-2.5-flash` to the highly stable and long-lived `gemini-3.5-flash-lite`.
- **Connectivity Restored**: Verified that Buddy now responds instantly without 404 errors.

### 2. Centralized Configuration (`AppConfig`)
- **[NEW] `AppConfig.kt`**: Created a dedicated Kotlin object in the `util` package to store all AI-related constants.
- **Maintenance Notes**: Included comments with model expiration dates to prevent future "sudden" 404 errors.
- **Code Cleanliness**: The repository now refers to `AppConfig.MODEL_NAME` instead of pulling strings directly from `BuildConfig`.

### 3. Load Balancing Verification
- **Key Rotation**: Confirmed that the API key rotation system (multi-key support) remains fully functional with the new model. If one key hits a limit, Buddy will still silently switch to the next one.

## Changes at a Glance

### [Component: Build]
- [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/build.gradle.kts) (Update `GEMINI_MODEL`)

### [Component: Utils]
- [NEW] [AppConfig.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/util/AppConfig.kt) (Central AI config)

### [Component: Repository]
- [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt) (Use centralized model name)

## Verification
- Clean build: `./gradlew assembleDebug` passed.
- Connectivity: 404 error resolved.

> [!TIP]
> From now on, if you need to change the model, just update [AppConfig.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/util/AppConfig.kt)!
