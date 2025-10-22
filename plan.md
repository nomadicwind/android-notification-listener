# Android Notification Listener Development Plan

## 1. Objectives
- Capture Android status bar notifications (e.g., WeChat) and surface their metadata (title, text, package, timestamp) inside a custom Android app.
- Provide a lightweight UI that confirms notification access status, displays captured events, and logs to Logcat for debugging.
- Lay a foundation that can be expanded with filtering, persistence, and external integrations.

## 2. Environment Prerequisites (macOS)
- macOS 12+ on an Apple Silicon or Intel MacBook Pro with at least 8 GB RAM and 10 GB free disk space.
- Stable internet connection for downloading Android Studio, SDK components, and app dependencies.
- Optional: Homebrew for managing supporting tools (e.g., `wget`, `git`) if not already installed.

## 3. Development Environment Setup
1. **Install Android Studio**
   - Download current stable Android Studio from <https://developer.android.com/studio>.
   - Run the installer, launch Android Studio, and follow the initial setup wizard.
2. **Configure Android SDK Components**
   - In Android Studio, open `Tools ▸ SDK Manager`.
   - Install latest stable Android SDK Platform (API 34 or target device API), Android SDK Build-Tools, Android SDK Platform-Tools, and Android Emulator.
   - In `SDK Tools` tab, ensure `Android SDK Command-line Tools (latest)` is installed.
   - Confirm the SDK location (default `~/Library/Android/sdk`) and export `ANDROID_HOME`/`ANDROID_SDK_ROOT` in shell profile if you plan to use CLI tools.
3. **Validate Command-line Access**
   - From a terminal, run `~/Library/Android/sdk/platform-tools/adb version` to confirm platform-tools work.
   - Accept any prompted licenses via `sdkmanager --licenses` if needed.
4. **Set Up an Emulator**
   - In Android Studio, open `Tools ▸ Device Manager` and create a Pixel-class Virtual Device using API 34 (Google Play image preferred for easier WeChat installation).
   - Enable hardware acceleration (Intel HAXM on Intel Macs, Hypervisor Framework on Apple Silicon) when prompted.
5. **Prepare a Physical Device (Optional but recommended)**
   - On the Android phone: enable Developer Options (tap build number 7x), toggle USB debugging, and allow file transfer.
   - Connect via USB and authorize the computer (`adb devices` should list the device).
6. **Install WeChat for Testing**
   - For emulator with Google Play image: sign in and install WeChat from Play Store.
   - Otherwise, download the latest WeChat APK from the official site and sideload with `adb install wechat.apk`.
7. **Set Up Version Control & Tooling**
   - Initialize Git in the project directory if not already.
   - Optionally install `ktlint` or `detekt` via Gradle for code quality checks.

## 4. Project Initialization
1. Create a new Android Studio project (Empty Activity or Empty Compose Activity) using Kotlin, minimum SDK 21+.
2. Configure `build.gradle` with latest Kotlin version and enable Jetpack Compose if choosing Compose UI.
3. Add dependencies for lifecycle (`lifecycle-runtime-ktx`), logging (Timber or Android `Log`), and optional persistence (Room) if planning to store history.
4. Set up package structure: `ui/`, `service/`, `data/`, `util/`.

## 5. Core Implementation Roadmap
1. **Notification Access Service**
   - Create a class extending `NotificationListenerService`.
   - Override `onListenerConnected`, `onNotificationPosted`, and `onNotificationRemoved`.
   - Extract relevant fields from `StatusBarNotification.notification.extras` with null-safety.
   - Filter notifications by package name (e.g., `com.tencent.mm`) when desired.
   - Broadcast or post captured data to the app via `LiveData`, `Flow`, or a shared repository.
2. **Permissions & Settings Flow**
   - In `AndroidManifest.xml`, declare the service with `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`.
   - Implement a UI affordance that checks `NotificationManagerCompat.getEnabledListenerPackages` and, if disabled, launches `ACTION_NOTIFICATION_LISTENER_SETTINGS`.
   - Provide clear user guidance about enabling access.
3. **Data Handling**
   - Define a data model (e.g., `NotificationEvent`) capturing package, title, text, timestamp, and optional extras.
   - Store events in an in-memory list for MVP; add Room database later for persistence.
   - Consider sanitizing or truncating content for privacy and performance.
4. **User Interface**
   - Display service status (Enabled/Disabled) and latest captured notifications in a list (RecyclerView or Compose `LazyColumn`).
   - Include filters (by app, timeframe) and a clear option to clear history.
   - Surface debug information (e.g., last event timestamp) for easier troubleshooting.
5. **Logging & Diagnostics**
   - Add structured logging (using `Log` or Timber) for received notifications and errors.
   - Optionally add a hidden developer screen that shows raw `extras` payload.
6. **Future Enhancements (Planned but not MVP)**
   - Persist events locally with Room, export to JSON/CSV, or forward to a backend service.
   - Add foreground service or WorkManager task to sync notifications when conditions meet.
   - Implement keyword-based alerts or push notifications to another device.

## 6. Testing & Validation
- **Unit Tests:** Validate notification parsing helpers and filtering logic with Robolectric/JUnit.
- **Instrumentation Tests:** Use `NotificationManager` APIs (API 29+) or mock notification posting where possible; rely on manual tests for permission flows.
- **Manual QA Checklist:**
  1. Enable notification listener permission and verify the app reflects the status.
  2. Receive WeChat notification and confirm it appears in the UI and Logcat.
  3. Dismiss notifications and ensure removal events are handled as expected.
  4. Verify app behavior with multiple apps and rapid notifications.
- **CI Considerations:** Configure GitHub Actions (or similar) to run `./gradlew lint test` on push.

## 7. Privacy & Security Considerations
- Request notification access only when needed and explain clearly why to the user.
- Avoid transmitting notification content off-device without explicit consent.
- Consider redacting sensitive fields before storing or exporting data.
- Store data securely if persistence is added (encrypted shared preferences or database).

## 8. Milestones & Timeline (Flexible)
1. **Environment Ready (Day 1-2):** Android Studio installed, emulator/device verified, WeChat test account prepared.
2. **Project Skeleton (Day 2-3):** New project, service scaffold, basic UI shell committed.
3. **Notification Capture MVP (Day 4-5):** Listener operational, logging and basic display working.
4. **Polish & Testing (Day 6-7):** UI improvements, filtering, manual QA, add unit tests.
5. **Stretch Goals (Post-MVP):** Persistence, export, automation, or backend integration based on needs.

## 9. Documentation & Knowledge Transfer
- Maintain a README with build/run instructions and permission requirements.
- Document troubleshooting tips (e.g., re-enabling listener, adb commands) and keep `plan.md` updated as tasks progress.
- Record known limitations (e.g., notifications from apps with encrypted payloads) for future work.
