# Implementation Plan - Fix 404 & Model ID Correction

Resolve the 404 error by reverting the model ID to the working version (`gemini-2.5-flash`) and refining the configuration notes.

## User Review Required

> [!IMPORTANT]
> **Model Correction**: I mistakenly added the `-lite` suffix in the previous update. Based on your console screenshot, the correct model ID is **`gemini-2.5-flash`**. Reverting to this will fix the 404 error.

## Proposed Changes

### 1. Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/build.gradle.kts)
- Change `GEMINI_MODEL` back to `"gemini-2.5-flash"`.

### 2. Config Refinement

#### [MODIFY] [AppConfig.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/util/AppConfig.kt)
- Update documentation to reflect that `gemini-2.5-flash` is the primary stable model for your keys.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` and sync project.

### Manual Verification
1. **Chat**: Send a message and verify Buddy responds (404 should be gone).
2. **Rotation**: With your 5 keys active, verify Buddy handles messages smoothly even if individual keys hit limits occasionally.
