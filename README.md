# ConnectLens

ConnectLens is an Android application for call log analytics and communication insights. Built with Jetpack Compose, Material 3, and Clean Architecture, it processes call history entirely on-device to deliver interactive visual analytics without cloud dependencies or external network requests.

---

## Features

- **Dashboard**: High-level communication overview including total calls, talk time, average call duration, peak calling windows, and top contact interactions.
- **Interactive Canvas Charts**: Custom Compose Canvas charts providing tap-to-inspect tooltips for daily volume, 24-hour activity heatmaps, day-of-week intensity, and call duration histograms.
- **Deep Analytics**: On-device computation of communication balance, call type distribution (incoming, outgoing, missed), duration buckets, and behavioral insights.
- **Call Logs & Search**: Searchable and filterable call history with quick action launchers for calls and SMS.
- **Contact Insights**: Individual contact breakdown displaying timeline records, total duration, and relationship interaction frequency.
- **Privacy First**: Designed without the `INTERNET` permission (`android.permission.INTERNET` is omitted). All call records are processed and cached locally in a Room database.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Clean Architecture & Unidirectional Data Flow (UDF)
- **Dependency Injection**: Hilt
- **Local Database**: Room + DataStore Preferences
- **Graphics**: Custom Jetpack Compose Canvas rendering
- **Async**: Kotlin Coroutines & StateFlow

### Project Structure

```
com.connectlens.app/
├── core/
│   ├── common/         # Helper utilities (formatting, phone numbers)
│   ├── designsystem/   # Theme, typography, colors, and shapes
│   └── ui/             # Reusable UI components & custom Canvas charts
├── data/
│   ├── local/          # Room database DAOs and entity models
│   ├── platform/       # System ContentProvider accessors (CallLogReader)
│   └── repository/     # Repository implementations & DataStore preferences
├── di/                 # Dependency injection modules
├── domain/
│   ├── model/          # Core domain models
│   ├── repository/     # Repository contracts
│   └── usecase/        # Analytics engine & business logic
├── feature/            # Screen implementations (Dashboard, Analytics, Contacts, Call History)
└── navigation/         # Bottom Navigation Bar and Jetpack Navigation graph
```

---

## Building and Running

### Prerequisites

- Android Studio (Hedgehog or newer)
- Android SDK 35/36
- JDK 17+

### Build via Gradle

```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest

# Install on Connected Device
./gradlew installDebug
```

The output APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Run via ADB

```bash
# Install APK
adb install -r "app/build/outputs/apk/debug/app-debug.apk"

# Launch main activity
adb shell am start -n com.connectlens.app.debug/com.connectlens.app.MainActivity
```

---

## Testing

Unit tests for the analytics engine cover time-range filtering, hourly distribution, call duration classification, and peak window calculations.

Run tests:
```bash
./gradlew test
```

---

## License

MIT License. See [LICENSE](LICENSE) for details.
