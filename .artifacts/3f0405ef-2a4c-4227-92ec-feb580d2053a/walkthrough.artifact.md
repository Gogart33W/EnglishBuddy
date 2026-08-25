# Walkthrough - Adaptive Learning & Spaced Repetition (SRS)

I have implemented an adaptive learning loop that connects your past mistakes directly to Buddy's conversational memory, powered by a structured Spaced Repetition System (SRS).

## Key Enhancements

### 1. Adaptive Chat Memory
- **Mistake Integration**: Buddy now queries your top 5 most frequent or recent mistakes before every response.
- **Targeted Practice**: These "weaknesses" are injected into Buddy's system prompt. He will now subtly weave specific scenarios and questions into the chat to test your understanding of concepts you've previously struggled with.

### 2. Strict AI Data Contract
- **Native `responseSchema`**: I've implemented Gemini's native `responseSchema` enforcement. Instead of just "asking" for JSON, the engine is now hard-coded to strictly follow our `TutorResponse` structure. This ensures absolute UI stability and eliminates parsing errors.

### 3. Spaced Repetition (SRS) Notebook
- **SM-2 Lite Algorithm**: Your "Mistake Notebook" is now a dynamic review tool.
- **Interval Doubling**: When you correctly resolve a mistake, its review interval doubles (1 -> 2 -> 4 -> 8 days).
- **Mastery**: Once a mistake reaches an 8-day interval, it is marked as "Mastered" and removed from your daily review list.
- **Due Filtering**: The Mistakes tab now only shows errors that are "Due" for review today, keeping your study sessions focused and efficient.

## Changes at a Glance

### [Component: Data & DB]
- [MODIFY] [MistakeEntity.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/entity/MistakeEntity.kt) (Added interval, nextReview, isMastered)
- [MODIFY] [MistakeDao.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/dao/MistakeDao.kt) (Added Due and Weakness targeting queries)
- [MODIFY] [AppDatabase.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/AppDatabase.kt) (Migration to v6)

### [Component: Repository & AI]
- [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt) (Implemented schema enforcement and adaptive prompt injection)

### [Component: UI]
- [MODIFY] [MistakesScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/MistakesScreen.kt) (Updated review-due empty state)

## Verification
- Clean build: `./gradlew assembleDebug` passed.
- Adaptive prompt injection verified via logic tracing.
- SRS interval calculation (1 -> 2 -> 4 -> 8) implemented in `resolveMistake`.

> [!TIP]
> Keep resolving your "Due" mistakes daily to reach the 8-day mastery threshold!
