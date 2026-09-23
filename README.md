# ConnectLens 📞🔍

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575.svg?style=flat&logo=materialdesign)](https://m3.material.io)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Local--First-brightgreen.svg?style=flat&logo=shield)](https://github.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **ConnectLens** is a modern, privacy-first Android application that analyzes your permitted phone call history to surface personal communication insights. All computation is performed **100% locally on your device** without any cloud servers or telemetry.

---

## 📸 Overview

ConnectLens gives you complete visibility into your communication habits without compromising your personal privacy:

- 📊 **Call Volume & Talk Time**: Track total calls, cumulative talk time, and average call duration.
- 🕒 **Time Range Filtering**: Filter insights across **Last 7 Days**, **Last 30 Days**, **This Month**, or **All Time**.
- 📈 **Custom Canvas Charts**: Custom-built, animated bar charts for call trends and donut charts for call-type breakdowns.
- 👤 **Contact Analytics**: Per-contact deep dives showing interaction history, top contacts, and duration distribution.
- 🔎 **Search & Filter**: Instant search across call logs by contact name or phone number with type filtering (Incoming, Outgoing, Missed).
- 🛡️ **Privacy Guarantees**: Zero internet permissions declared (`INTERNET` is omitted), excluded from cloud backups, and data stays entirely on-device.

---

## 📱 Features Summary

| Feature | Description |
|---|---|
| **Dashboard** | Displays total calls, talk time, average duration, unique contacts count, interactive bar chart, donut chart, and top contacts. |
| **Contact Analytics** | Per-contact breakdown with aggregate stats, timeline chart, and history list. |
| **Call History** | Real-time searchable and filterable list of all call records. |
| **Analytics Engine** | On-device calculation of communication metrics and automated text insights. |
| **Settings** | Live permission monitor, data clearance tool, and privacy policy breakdown. |
| **Onboarding** | Interactive 5-page introduction highlighting local data ownership. |

---

## 🏗️ Architecture

ConnectLens follows **Android Clean Architecture** guidelines with **MVVM**, **Unidirectional Data Flow (UDF)**, and **Dagger Hilt** for dependency injection.

```
com.connectlens.app/
├── core/
│   ├── common/           # Utility helpers (PhoneNumberUtils, TimeUtils)
│   ├── designsystem/     # Color, Theme (Material 3), Typography, Shapes
│   ├── permissions/      # Permission management composable state holders
│   └── ui/               # Core components (SummaryCard, ContactRow, Charts, States)
├── data/
│   ├── local/            # Room database entity models, DAOs, and database definition
│   ├── platform/         # ContentProvider accessors (CallLogReader, ContactsReader)
│   └── repository/       # Repository implementations & DataStore preferences
├── di/                   # Hilt DI modules (DatabaseModule, RepositoryModule)
├── domain/
│   ├── model/            # Clean domain entities (CallRecord, ContactStats, DashboardStats)
│   ├── repository/       # Repository interfaces
│   └── usecase/          # AnalyticsEngine, GetDashboardStats, GetCallHistory
├── feature/              # UI screens & ViewModels (Composable features)
│   ├── onboarding/
│   ├── dashboard/
│   ├── contacts/
│   ├── callhistory/
│   ├── analytics/
│   └── settings/
└── navigation/           # Type-safe Jetpack Navigation Graph
```

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material 3 Design System
- **Dependency Injection**: Dagger Hilt 2.53.1
- **Database & Storage**: Room 2.7.0 + DataStore Preferences
- **Asynchronous Flow**: Kotlin Coroutines + StateFlow / SharedFlow
- **Navigation**: Jetpack Navigation Compose 2.8.5
- **Charts**: Custom Compose Canvas-based rendering (Zero external chart libraries)
- **Build System**: Gradle 8.11.1 + Android Gradle Plugin 8.7.3 + KSP

---

## 🔐 Privacy & Security Baseline

ConnectLens is designed around a **Local-First Privacy Architecture**:

1. 🚫 **No Network Access**: The `android.permission.INTERNET` permission is **not declared** in `AndroidManifest.xml`. The app is physically incapable of making network requests.
2. 🔒 **Local Storage Only**: Call log caches are stored in a local SQLite Room database on the device's internal storage (`/data/user/0/com.connectlens.app.debug/databases/`).
3. 📦 **No Backup Leaks**: Database files are explicitly excluded from Android Cloud Backup and device-to-device transfers via `backup_rules.xml` and `data_extraction_rules.xml`.
4. 🧹 **User Data Control**: Users can clear local analytics cache anytime via **Settings → Clear Local Analytics Cache**. Clearing the app's cache **never** deletes the user's system phone logs.

---

## 🚀 Getting Started & Building

### Prerequisites

- **Android Studio** (Hedgehog | 2023.1.1 or newer)
- **Android SDK** (API 35/36 installed)
- **JDK**: Android Studio bundled JBR (JDK 17/21)

### 🔨 Building via Command Line

```powershell
# Set JAVA_HOME to Android Studio JBR (Recommended)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Build Debug APK
.\gradlew.bat assembleDebug

# Run Unit Tests
.\gradlew.bat testDebugUnitTest

# Install on Connected Phone/Emulator
.\gradlew.bat installDebug
```

> The compiled debug APK will be located at:
> `app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Testing on Your Physical Android Device (ADB)

To test ConnectLens directly on your phone:

1. **Enable Developer Options**:
   - Go to **Settings → About Phone** on your device.
   - Tap **Build Number** 7 times until you see *"You are now a developer!"*.
2. **Enable USB Debugging**:
   - Go to **Settings → System → Developer Options**.
   - Toggle **USB Debugging** to **ON**.
3. **Connect & Authorize**:
   - Connect your phone to your PC via USB cable.
   - When prompted on your phone, check **"Always allow from this computer"** and tap **Allow**.
4. **Install via ADB**:
   ```cmd
   adb install -r "app/build/outputs/apk/debug/app-debug.apk"
   ```
   Or launch directly:
   ```cmd
   adb shell am start -n com.connectlens.app.debug/com.connectlens.app.MainActivity
   ```

---

## 🧪 Unit Tests

ConnectLens includes automated unit testing for the core analytics engine (`AnalyticsEngineTest`), covering:
- Correct computation of total calls, talk time, and averages.
- Proper handling of edge cases (empty logs, zero duration calls, missing contact names).
- Time range windowing and formatting.

Run tests via command line:
```powershell
.\gradlew.bat testDebugUnitTest
```
Test report output: `app/build/reports/tests/testDebugUnitTest/index.html`

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
