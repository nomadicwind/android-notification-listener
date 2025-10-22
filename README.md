# Android Notification Listener

An Android sample app that captures status bar notifications (e.g., WeChat) via `NotificationListenerService`, displays them in a Compose UI, and logs their metadata for debugging.

## Prerequisites
- macOS 12+ with Android Studio (latest stable) and Android SDK API 34 installed.
- Java 17 (bundled with Android Studio).
- Optional physical Android device with USB debugging enabled or an emulator configured through Android Studio.
- WeChat (or any target app) installed on the device/emulator for testing.

See `plan.md` for a full environment preparation guide, including emulator creation and APK sideloading tips.

## Getting Started
1. **Clone the repository**
   ```bash
   git clone https://github.com/nomadicwind/android-notification-listener.git
   cd android-notification-listener
   ```
2. **Open in Android Studio**
   - Choose *Open an Existing Project* and select this directory.
   - Let Gradle sync; install any missing SDK components when prompted.
3. **Build**
   - Select a connected device or emulator.
   - Press **Sync Project with Gradle Files**, then **Run ▶** in Android Studio or execute `./gradlew installDebug` from the terminal.
4. **Start the App**
   - From Android Studio use **Run ▶**, or on the device open the newly installed *Notification Listener* app from the launcher.
   - If you used the command line, launch the app manually after `installDebug` completes.

## Grant Notification Access
1. Launch the app on your device/emulator.
2. Tap **Open Settings** in the app or navigate to **Settings ▸ Notifications ▸ Notification access**.
3. Enable access for *Notification Listener*.
4. Trigger a notification (e.g., send a WeChat message) and verify it appears in the app list and in Logcat.

## Project Structure
```
app/
 ├── src/main/java/com/example/notificationlistener/
 │    ├── data/NotificationRepository.kt        # In-memory store for captured notifications
 │    ├── listener/AppNotificationListenerService.kt
 │    ├── ui/theme/                             # Compose Material 3 theme setup
 │    └── MainActivity.kt                       # Compose UI and permission flow
 ├── src/main/res/                              # Resources (strings, icons, themes)
 └── build.gradle.kts                           # Module configuration with Compose enabled
```

## Testing & Tooling
- Run unit tests: `./gradlew test`
- Run instrumentation tests (requires emulator/device): `./gradlew connectedAndroidTest`
- Static analysis / lint: `./gradlew lint`

## Next Steps & Enhancements
- Persist captured notifications (Room, DataStore).
- Add filters (e.g., WeChat-only toggle) and search.
- Export or sync notifications to another service.
- Harden privacy: redact sensitive data, add opt-in for persistence/export.

## Troubleshooting
- **Service not receiving notifications:** Revoke and re-enable notification access; ensure the service appears enabled in settings.
- **WeChat on emulator:** Use a Google Play system image or sideload the official APK via `adb install`.
- **Gradle sync issues:** Verify you installed matching Android Gradle Plugin and SDK versions (AGP 8.2.2 with Gradle 8.2).
