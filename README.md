# UtsavSetu

<p align="center">
  <strong>Your Indian festival, Panchang and puja companion</strong><br/>
  An offline-first Android application built with Kotlin and Jetpack Compose.
</p>

<p align="center">
  <a href="https://github.com/rkj00777/Utsavsetu/actions/workflows/android-build.yml">
    <img src="https://github.com/rkj00777/Utsavsetu/actions/workflows/android-build.yml/badge.svg?branch=main" alt="Android build"/>
  </a>
</p>

## Overview

UtsavSetu brings Indian festival discovery and daily Panchang information together with practical puja preparation tools. The application is designed to keep core festival content and artwork available locally on the device.

### Current application areas

- **Today** — daily festival and Panchang overview.
- **Calendar** — festival dates in calendar and agenda views.
- **Festivals** — browse the bundled festival catalogue and discovered observances.
- **Panchang** — tithi, nakshatra, yoga, karana and daily timing calculations.
- **Festival details** — festival information, puja vidhi and samagri checklists.
- **Reminders** — scheduled observance notifications.
- **Preferences** — location, language and application settings.

The codebase also includes local persistence (Room and DataStore), optional Firebase-related integrations, and locally bundled festival artwork. Feature availability may depend on configuration and device permissions.

## Download

### Latest GitHub Release

Once a versioned release has been published, download the APK here:

**[Download latest UtsavSetu APK](https://github.com/rkj00777/Utsavsetu/releases/latest/download/UtsavSetu.apk)**

The release page also includes an Android App Bundle (AAB), where available:

**[View all releases](https://github.com/rkj00777/Utsavsetu/releases)**

### Download a build from GitHub Actions

Every successful build workflow uploads APK and AAB files as workflow artifacts:

1. Open [Actions → Android build](https://github.com/rkj00777/Utsavsetu/actions/workflows/android-build.yml).
2. Select the latest successful run on the `main` branch.
3. Scroll to **Artifacts**.
4. Download `UtsavSetu-APK` or `UtsavSetu-AAB`.

Workflow artifacts are retained for a limited period (configured in the workflow). Versioned GitHub Releases are the durable download location.

> **Important — testing builds:** The automated workflow currently produces debug-variant APK/AAB files signed with a temporary debug key. These are for development and testing, not Play Store production publication. A production release must be built and signed with the app owner's private upload/release keystore. Never commit that keystore or its passwords to this repository.

## Build locally

### Requirements

- Android Studio with Android SDK support for API 36
- JDK 17 or the JDK version required by the configured Android Gradle Plugin
- Internet access for the first dependency and Gradle distribution download

### Build and test

On macOS/Linux:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew bundleDebug
```

On Windows:

```bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
gradlew.bat bundleDebug
```

Expected outputs:

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- AAB: `app/build/outputs/bundle/debug/app-debug.aab`

The debug signing configuration in this repository expects `debug.keystore` at the project root. If it is absent, Android Studio/CI setup must create a compatible debug keystore before building.

## OBB / expansion files

UtsavSetu does **not** currently require an OBB expansion file. Its festival artwork and other bundled resources are packaged with the application. Android App Bundles (AABs) are the preferred Play distribution format; creating an empty or artificial `.obb` file would not add functionality and must not be presented as a valid app expansion. Only introduce Play APK Expansion Files if the app later has a genuine large-asset delivery requirement and the corresponding runtime download/validation code is implemented.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Android Gradle Plugin and Gradle Wrapper
- Room and DataStore
- Firebase libraries (optional/configuration dependent)

## Repository layout

```text
app/
  src/main/       Android application source and bundled resources
  src/test/       JVM and Robolectric tests
  src/androidTest/ instrumentation tests
gradle/           Gradle version catalog and wrapper
firestore.rules   Firestore security rules
```

## Configuration and security

- Do not commit `.env`, Firebase service-account credentials, signing keystores, signing passwords, or private API keys.
- Use GitHub Actions secrets for CI-only credentials.
- Configure Firebase for the exact Android application ID before enabling live cloud features.
- Replace any development/test advertising configuration with the correct production configuration before distribution.
- Review permissions, privacy disclosures and third-party SDK disclosures before publishing.

## Contributing

1. Create a feature branch.
2. Keep changes scoped and preserve offline-first behaviour.
3. Add or update tests for domain and UI changes.
4. Run `./gradlew testDebugUnitTest` and build the debug APK.
5. Open a pull request with a concise description and test evidence.

## Disclaimer

Panchang and festival observances can vary by location, tradition and regional convention. Users should select the correct location and tradition and consult trusted local sources for consequential religious observances. The application is an informational companion, not a substitute for local customary guidance.

---

Repository: [rkj00777/Utsavsetu](https://github.com/rkj00777/Utsavsetu)
