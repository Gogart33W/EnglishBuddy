# Implementation Plan - Adaptive Learning & Spaced Repetition

Connect mistake tracking to Buddy's conversational memory, enforce strict JSON response schemas, and implement an SRS-based review system for errors.

## User Review Required

> [!IMPORTANT]
> **Database Migration**: I will update the Room database to version 6. This includes adding SRS fields (`intervalDays`, `nextReviewTimestamp`, `isMastered`) to the `mistakes` table. Existing mistakes will be reset to a 1-day interval.

> [!NOTE]
> **Schema Enforcement**: By using Gemini's `responseSchema`, the AI will no longer "forget" to include specific fields or return invalid JSON, making the UI highly stable.

## Proposed Changes

### 1. Data Layer (Mistakes & SRS)

#### [MODIFY] [MistakeEntity.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/entity/MistakeEntity.kt)
- Add `intervalDays: Int = 0`
- Add `nextReviewTimestamp: Long = 0`
- Add `isMastered: Boolean = false`

#### [MODIFY] [MistakeDao.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/dao/MistakeDao.kt)
- Add `getTopWeaknesses(limit: Int)`: Fetch most frequent/recent mistakes for AI prompting.
- Add `getDueMistakes(currentTime: Long)`: Fetch mistakes due for review (where `nextReviewTimestamp <= currentTime` and `!isMastered`).
- Update queries to support SRS state updates.

#### [MODIFY] [AppDatabase.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/AppDatabase.kt)
- Increment version to 6 and apply destructive migration.

### 2. Domain & Repository (Adaptive AI)

#### [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt)
- **Strict Schema**: Define a `tutorResponseSchema` object using the `ResponseSchema` DTO and pass it in `GenerationConfig`.
- **Adaptive Memory**: In `sendMessage`, query the top 5 weaknesses from `MistakeDao` and inject them into the system instruction.
- **SRS Update**: Modify `resolveMistake` to implement doubling intervals (1 -> 2 -> 4 -> 8 days) and set the next review time.

### 3. UI Layer (Mistakes Notebook)

#### [MODIFY] [ChatViewModel.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/viewmodel/ChatViewModel.kt)
- Update `allMistakes` observation to filter for "Due" items only.

#### [MODIFY] [MistakesScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/MistakesScreen.kt)
- Show a "No reviews due today" empty state if all mistakes are scheduled for the future or mastered.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify KSP and Room schema generation.

### Manual Verification
1. **Adaptive Prompting**: Send a message with a recurring mistake (e.g., using "go" instead of "went"). Verify Buddy mentions this rule in the conversational body later.
2. **Strict Schema**: Verify (via logs or UI stability) that Buddy's JSON responses always follow the expected structure.
3. **SRS Loop**:
    - Resolve a mistake in the Notebook.
    - Verify it disappears from the "Due" list.
    - Manually adjust system time (if possible) or check DB to verify `nextReviewTimestamp` is set correctly for 2 days later.
