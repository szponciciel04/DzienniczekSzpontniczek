# Project Guidelines

## Architecture
- This is a Kotlin Multiplatform mobile app with a single Gradle module: `:composeApp`.
- Shared logic lives in `composeApp/src/commonMain/kotlin/io/github/szpontium`.
- Platform-specific implementations belong in `composeApp/src/androidMain` and `composeApp/src/iosMain`.
- Keep boundaries clear:
  - `api/` for remote integrations and protocol details.
  - `session/` for runtime session state and persistence glue.
  - `viewmodel/` for screen/application state orchestration.
  - `ui/` and `navigation/` for Compose UI and navigation.

## Build and Test
- Preferred Android debug build command: `./gradlew :composeApp:assembleDebug`
- Preferred Android release build command: `./gradlew :composeApp:assembleRelease`
- iOS app entry point is `iosApp/iosApp.xcodeproj` (build/run from Xcode).
- Use Kotlin/JVM 11 target conventions already defined in Gradle files.

## Conventions
- Use Koin DI patterns from `composeApp/src/commonMain/kotlin/io/github/szpontium/di/AppModule.kt` (`single { ... }`, `viewModel { ... }`).
- Use `StateFlow` for long-lived UI state and `Channel`/events for one-off UI actions (see LoginViewModel pattern).
- Use kotlinx.serialization `@Serializable` models for network payloads.
- Keep platform HTTP engine differences in `platform/HttpClientFactory.*.kt`; do not hardcode platform engine details in shared business logic.

## API-Specific Gotchas
- Treat eduVULCAN and VULCAN Dzienniczek as separate login/registration flows (`SzpontHebeCeApi` vs `SzpontHebeApi`).
- Ensure credentials are registered before data calls; missing registration leads to `restUrl not set` failures.
- Use the announcements endpoint as singular `mobile/announcement/byPupil`.
- Grades can include decimal JSON values for numerator/denominator; preserve flexible deserialization behavior.

## Working Rules for Agents
- Do not edit generated output under `build/`, `api/build/`, or `composeApp/build/`.
- Prefer minimal, targeted edits and keep existing package structure/style.
- Link to existing docs instead of duplicating long guides:
  - `README.md`
  - `docs/getting-started.md`
  - `docs/basic-usage.md`
  - `docs/login-and-hebece-client.md`
  - `docs/eduvulcan-login-flow.md`
  - `docs/prometheus-login-helper.md`
