# Walkthrough - Complete language learning platform

I have transformed EnglishBuddy into a complete language learning platform with bottom navigation, markdown rendering, dictionary caching, and user profiling.

## Key Features

### 1. App Navigation & New Tabs
- **Bottom Navigation**: Added a `NavigationBar` with 4 main tabs:
    - **Chat**: The core conversation engine with Buddy.
    - **Vocabulary**: A collection of saved words with "Flashcard" mode (flip-to-reveal translation).
    - **Mistakes**: A dedicated notebook tracking all past errors with a "Try Again" practice feature.
    - **Profile**: Displays CEFR level (A1-C2), streaks, and vocabulary stats.

### 2. Markdown Rendering
- **`MarkdownText` Utility**: Implemented a custom parser to render `**bold**`, `*italic*`, and `` `inline code` `` directly in Buddy's messages.
- **Enhanced System Prompt**: Buddy now uses markdown for emphasis and is strictly prohibited from repeating explanations already present in the correction block.

### 3. Smart Dictionary Caching
- **Zero-Token Cache**: Implemented `DictionaryEntity` in Room. When you tap a word, Buddy first checks the local database. Only new words trigger a Gemini API call.
- **Save to Vocabulary**: Added a "⭐" button in the Word BottomSheet to save words for later review in the Vocabulary tab.

### 4. Level-Based Adaptive Prompting
- **User Profiling**: Created `UserProfileEntity` to track your CEFR level.
- **Adaptive AI**: Buddy dynamically adjusts his vocabulary and grammar complexity based on your profile, ensuring the conversation is always at the right level for you.

## Changes at a Glance

### [Component: Data & DB]
- [NEW] [DictionaryEntity.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/entity/DictionaryEntity.kt) / [DictionaryDao.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/dao/DictionaryDao.kt)
- [NEW] [UserProfileEntity.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/entity/UserProfileEntity.kt) / [UserProfileDao.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/dao/UserProfileDao.kt)
- [MODIFY] [AppDatabase.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/AppDatabase.kt) (Migration to v4)

### [Component: UI & UX]
- [NEW] [MainScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/MainScreen.kt) (Scaffold + BottomBar)
- [NEW] [VocabularyScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/VocabularyScreen.kt)
- [NEW] [MistakesScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/MistakesScreen.kt)
- [NEW] [ProfileScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/ProfileScreen.kt)
- [NEW] [MarkdownText.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/util/MarkdownText.kt)

## Verification
- Clean build: `./gradlew assembleDebug` passed.
- Navigation stack management verified for state restoration across tabs.

> [!TIP]
> Use the Profile tab to increase your level once Buddy's responses start feeling too easy!
