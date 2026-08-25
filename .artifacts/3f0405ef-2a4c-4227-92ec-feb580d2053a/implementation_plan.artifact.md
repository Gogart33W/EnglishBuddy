# Implementation Plan - Fix Dictionary & Database Migrations

Resolve the dictionary translation issue and implement a stable Room migration strategy to prevent data loss.

## User Review Required

> [!IMPORTANT]
> **Database Migration**: I will move to Room version 7 and provide a proper `Migration` object. This will stop the "auto-reset" behavior you're seeing. However, if you've already had a reset to v6, those old messages are gone. This fix ensures it doesn't happen *again*.

> [!NOTE]
> **Dictionary Fix**: The dictionary failure is likely due to the strict `responseMimeType = "application/json"` without a corresponding `responseSchema` in the dictionary request. I will explicitly define the dictionary schema to match the `TutorResponse` logic.

## Proposed Changes

### 1. Data Layer (Room Migrations)

#### [MODIFY] [AppDatabase.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/local/AppDatabase.kt)
- Increment version to 7.
- Define `MIGRATION_6_7`.
- Remove `fallbackToDestructiveMigration()` in `MainActivity`.

#### [MODIFY] [MainActivity.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/MainActivity.kt)
- Update Room builder to use the migration object.

### 2. Repository Layer (Dictionary Fix)

#### [MODIFY] [ChatRepositoryImpl.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/data/repository/ChatRepositoryImpl.kt)
- **Schema Enforcement**: Define `dictionaryResponseSchema` and pass it in the `getWordDefinition` request.
- **Robustness**: Add logging to dictionary failures to track exact JSON parsing errors.

### 3. UI Layer (UX Refinement)

#### [MODIFY] [ChatScreen.kt](file:///home/gogart/AndroidStudioProjects/EnglishBuddy/app/src/main/java/com/gogart/englishbuddy/ui/ChatScreen.kt)
- Improve the dictionary loading state visibility.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify KSP and migration code.

### Manual Verification
1. **Dictionary**: Tap a word in the chat. Verify the loader appears and then the translation/transcription/example are displayed correctly.
2. **Migrations**:
   - Add a few messages and a saved word.
   - Deploy a small change to a DAO (without schema change).
   - Verify that existing data persists after the app restarts.
