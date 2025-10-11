# Substitute Schedule App

A Kotlin Multiplatform application for viewing substitute schedules from DSBmobile, supporting Android, iOS, and Desktop (JVM) platforms.

## Features

- **Cross-platform**: Runs on Android, iOS, and Desktop (Windows, macOS, Linux)
- **DSBmobile Integration**: Fetches and displays substitute schedules from DSBmobile API
- **Modern UI**: Built with Jetpack Compose Multiplatform and Material 3 design
- **WebView Display**: Renders schedule content in an embedded web view
- **Navigation**: Easy switching between today's and tomorrow's schedules
- **Dark/Light Theme**: Automatic theme support based on system preferences

## Installation
Download the installer for your System from the [Releases Tab](https://github.com/KyNarec/SubstituteSchedule/releases)
* .apk file for Android
* .msi file for Windows
* .deb file for Linux

and execute it.


## Tech Stack

### Core
- **Kotlin Multiplatform** - Code sharing across platforms
- **Jetpack Compose Multiplatform** - Modern declarative UI framework
- **Material 3** - Google's latest design system

### Networking
- **Ktor Client** - HTTP client for API communication
- Platform-specific engines: OkHttp (Android/JVM), Darwin (iOS)

### Data Handling
- **kotlinx-serialization** - JSON serialization/deserialization
- **kotlinx-datetime** - Cross-platform date/time handling
- **Okio** - Efficient I/O operations and compression
- **UUID** - Cross-platform UUID generation

### UI Components
- **compose-webview-multiplatform** - WebView support across platforms
- **KCEF** (Desktop only) - Chromium Embedded Framework for JVM
- **Material Icons Extended** - Comprehensive icon set

### Navigation
- **Navigation Compose** - Type-safe navigation with Kotlin serialization

## Platform Support

### Android
- Minimum SDK: 24
- Target SDK: 35
- Uses OkHttp engine for networking

### iOS
- Supports ARM64 devices and Simulator
- Uses Darwin engine for networking
- Framework-based distribution

### Desktop (JVM)
- Requires Java 11+
- Distributable formats: DMG (macOS), MSI (Windows), DEB (Linux)
- Uses KCEF for enhanced WebView support

## Setup for development

### Prerequisites
- JDK 11 or higher
- Android Studio (for Android development)
- Xcode (for iOS development)
- Kotlin 2.0+

### Building

#### Android
```bash
./gradlew :composeApp:assembleDebug
```

#### iOS
```bash
./gradlew :composeApp:iosSimulatorArm64MainKlibrary
```
Then open the project in Xcode to run on simulator or device.

#### Desktop
```bash
./gradlew :composeApp:run
```

### Creating Distribution Packages

#### Desktop
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```


## Acknowledgments

- Built with [Jetpack Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- Uses [compose-webview-multiplatform](https://github.com/kevinnzou/compose-webview-multiplatform)
- Backend inspired by [DSBmobile-API](https://github.com/Sematre/DSBmobile-API)
- Desktop WebView powered by [KCEF](https://github.com/DatL4g/KCEF)
