# AGENTS.md

## Cursor Cloud specific instructions

### Product overview

**PharmaBazaar** (`com.aistudio.pharmabazaar.b2b`) is a Kotlin/Jetpack Compose Android app — a B2B pharmaceutical marketplace. There is no backend server in this repo; the app uses embedded Room/SQLite locally and optionally Firebase/Gemini cloud services.

### Services

| Service | Required? | Notes |
|---------|-----------|-------|
| Android SDK | Yes | Installed at `$HOME/Android/Sdk` (platform 36.1, build-tools 36.0.0) |
| Gradle wrapper | Yes | Generated on first setup (`gradle wrapper --gradle-version 9.3.1`); AGP 9.1.1 requires Gradle ≥ 9.3.1 |
| `debug.keystore` | Yes | Required by `app/build.gradle.kts` signing config; auto-created at repo root if missing |
| `.env` | Yes | Copy from `.env.example`; used by Secrets Gradle Plugin for `GEMINI_API_KEY` |
| Android emulator | Optional | AVD `pharma_dev` (Pixel 8, API 36, google_apis/x86_64); headless, software-rendered |
| Firebase / Gemini | Optional | App works offline with seeded local data; `google-services.json` is not committed |

### Common commands

See `app/build.gradle.kts` for build config. Standard Gradle tasks:

| Task | Command |
|------|---------|
| Build debug APK | `./gradlew :app:assembleDebug` |
| Lint | `./gradlew :app:lintDebug` |
| Unit tests | `./gradlew :app:testDebugUnitTest` (see caveat below) |
| Install on emulator | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |
| Launch app | `adb shell am start -n com.aistudio.pharmabazaar.b2b/com.example.MainActivity` |

### Environment variables

`ANDROID_HOME` and `ANDROID_SDK_ROOT` must point to `$HOME/Android/Sdk`. These are set in `~/.bashrc` on the Cloud VM.

### Emulator caveats

- No KVM in Cloud VMs — start with software rendering: `emulator -avd pharma_dev -no-window -no-audio -gpu swiftshader_indirect -accel off`
- First boot can take 5–10+ minutes; wait for `adb shell getprop sys.boot_completed` to return `1`
- Grant notification permission to avoid setup dialogs: `adb shell pm grant com.aistudio.pharmabazaar.b2b android.permission.POST_NOTIFICATIONS`
- System ANR dialogs can appear under software emulation; tap **Wait** or reboot the AVD if `adb` becomes unresponsive

### Unit test caveat

`GreetingScreenshotTest.kt` references removed symbols (`MyApplicationTheme`, `Greeting`) and prevents the entire `:app:testDebugUnitTest` compile step from succeeding. `ExampleRobolectricTest` also expects a stale app name (`"My Application"` vs `"ফার্মা বাজার"`). Until those tests are updated, use `./gradlew :app:lintDebug` and `./gradlew :app:assembleDebug` as the primary verification path.

### Optional cloud setup

For full Firebase/Gemini features, add `app/google-services.json` and set a real `GEMINI_API_KEY` in `.env`.
