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

UtsavSetu brings Indian festival discovery and daily Panchang information together with practical puja preparation tools. Core festival content and artwork are intended to remain available locally on the device.

### Application areas

- **Today** — daily festival and Panchang overview.
- **Calendar** — festival dates in calendar and agenda views.
- **Festivals** — browse the bundled festival catalogue and discovered observances.
- **Panchang** — tithi, nakshatra, yoga, karana and daily timing calculations.
- **Festival details** — festival information, puja vidhi and samagri checklists.
- **Reminders** — scheduled observance notifications.
- **Preferences** — location, language and application settings.

The codebase includes local persistence (Room and DataStore) and optional Firebase-related integrations. Feature availability may depend on configuration and device permissions.

## Downloads

### Test builds

Every successful build on `main` uploads debug APK/AAB artifacts:

1. Open [Actions → Android build](https://github.com/rkj00777/Utsavsetu/actions/workflows/android-build.yml).
2. Select a successful run on `main`.
3. Download `UtsavSetu-debug-APK` to install on a test device, or `UtsavSetu-debug-AAB` for bundle inspection.
4. Verify the matching SHA-256 checksum artifact.

Workflow artifacts are temporary and currently retained for 30 days. Debug builds are for testing only and are not production Play Store releases.

### Production releases

The [GitHub Releases page](https://github.com/rkj00777/Utsavsetu/releases) is the durable distribution location. A versioned release is published only when a `v*` tag is built with all production signing secrets configured. It contains:

- `UtsavSetu-release.apk` — signed APK for direct installation/testing.
- `UtsavSetu-release.aab` — signed Android App Bundle for Play Console upload.
- `SHA256SUMS.txt` — integrity checksums.

The workflow deliberately does **not** publish debug-signed packages as production releases. If signing secrets are missing, the tag run warns and skips production release publication.

## Configure production signing (repository owner)

The Android release signing configuration in `app/build.gradle.kts` uses keystore alias `upload` and reads the keystore path and passwords from environment variables.

1. Create and securely back up the Android upload keystore locally. Keep the keystore and passwords private; never commit them or send them in chat.
2. Base64-encode the keystore file locally. For example, on macOS/Linux:
   ```bash
   base64 -w 0 my-upload-key.jks > keystore.base64.txt
   ```
   On macOS, if `-w` is unsupported, use `base64 my-upload-key.jks | tr -d '\\n' > keystore.base64.txt`.
3. In GitHub, open **Settings → Secrets and variables → Actions → New repository secret** and add:
   - `KEYSTORE_BASE64`: the complete Base64 text from the local file.
   - `STORE_PASSWORD`: keystore password.
   - `KEY_PASSWORD`: private-key password for alias `upload`.
4. Delete the temporary Base64 text file securely after configuring the secret. Retain the original keystore and recovery copy in a secure password manager/offline backup.
5. Confirm the app's `versionCode` is incremented and `versionName` is correct in `app/build.gradle.kts` before tagging.
6. Push a version tag only after tests and device QA pass, for example:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
7. Confirm the tag workflow builds the release variant and that the GitHub Release contains the release APK, AAB and checksums. Verify the signing certificate and checksums before distribution.

**Important:** Keep the upload keystore backed up. Losing it can complicate future app updates. For Google Play App Signing, follow Play Console's upload-key/app-signing-key setup and use the upload key expected by that Play Console app.

## Build locally

### Requirements

- Android Studio with Android SDK support for API 36
- JDK 17
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

Expected debug outputs:

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- AAB: `app/build/outputs/bundle/debug/app-debug.aab`

The debug signing configuration expects `debug.keystore` at the project root. CI generates a disposable debug key automatically.

## OBB / expansion files

UtsavSetu does **not** currently require an OBB expansion file. Festival artwork and other bundled resources are packaged with the application. Android App Bundles are the preferred Play distribution format; an empty or artificial `.obb` file would not add functionality. Only introduce expansion files if there is a genuine large-asset delivery requirement and corresponding runtime download/validation code.

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
- Use GitHub Actions secrets for CI signing credentials.
- Configure Firebase for the exact Android application ID before enabling live cloud features.
- Replace development/test advertising configuration with the correct production configuration before distribution.
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
