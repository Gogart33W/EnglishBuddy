# Implementation Plan - UI Polish & Dictionary Optimization

Address UI wrapping issues, improve empty states, make interactive elements more discoverable, and optimize dictionary token usage.

## User Review Required

> [!NOTE]
> **Label Change**: I will shorten "Vocabulary" to "Vocab" in the Bottom Navigation bar to prevent text wrapping and clipping on smaller screens.

> [!TIP]
> **Stop-Word Filter**: To save tokens, I will implement a local filter for common "stop words" (e.g., "a", "the", "is"). Tapping these will show a local definition instead of calling the Gemini API.

## Proposed Changes

### 1. UI Layer (Navigation & Layout)

#### [MODIFY] [MainScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/MainScreen.kt)
- Shorten `Screen.Vocabulary` label from "Vocabulary" to "Vocab".

#### [MODIFY] [VocabularyScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/VocabularyScreen.kt)
- Fix the empty state `Box` to correctly use `fillMaxSize()` relative to the scaffold's content area.
- Add an icon to the empty state for better visual balance.

### 2. UI Layer (Chat & Discoverability)

#### [MODIFY] [ChatScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/ChatScreen.kt)
- **`InteractiveText`**: Add a subtle visual hint (e.g., a very light background or a dashed bottom border) to clickable words so the user knows they can be tapped.
- **`WordDetailContent`**: Improve the layout to ensure it's perfectly centered and polished.

### 3. Optimization & Performance

#### [MODIFY] [ChatViewModel.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/viewmodel/ChatViewModel.kt)
- **Local Pre-check**: Implement a local check for very common words (articles, pronouns). If a user taps "the", show a hardcoded definition to save API costs and time.
- **Loading Lock**: Ensure multiple simultaneous taps on the same word don't trigger redundant API calls.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify no regressions.

### Manual Verification
1. **Bottom Bar**: Check that "Vocab" fits perfectly on one line.
2. **Empty State**: Open Vocabulary with no words; verify the message is centered and not cropped.
3. **Word Tapping**: Verify that words in Buddy's messages have a subtle visual indicator.
4. **Token Savings**: Tap "the" and "a"; verify (via Logcat) that no API call is made.
