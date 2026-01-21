# TrackerApp 🛩️

> **Professional GPS tracking app with aviation-inspired design and Clean Architecture**

A modern Android location tracking application built with Jetpack Compose, following Clean Architecture principles and featuring a professional Airbus-inspired design system.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-BOM%202024.11.00-blue)](https://developer.android.com/jetpack/compose)
[![Material3](https://img.shields.io/badge/Material-3-blue)](https://m3.material.io/)
[![MinSDK](https://img.shields.io/badge/MinSDK-26-orange)](https://developer.android.com/studio/releases/platforms)

---

## ✨ Features

### 🎯 Core Functionality
- **Precise GPS Tracking**: High-accuracy location tracking using Google Play Services
- **Interactive Map**: OpenStreetMap integration with smooth zoom and pan
- **Real-time Signal Quality**: Visual GPS signal strength indicator
- **Multiple Coordinate Formats**: DD, DDM, DMS support
- **Location History**: Track your movement over time *(Coming soon)*

### 🎨 Design
- **Airbus-Inspired Theme**: Professional aviation aesthetic
  - Deep Blue (#003087) - Trust & Professionalism
  - Sky Blue (#00A3E0) - Aviation & Clarity
  - Safety Orange (#FF6B35) - Call-to-Action
- **Material Design 3**: Latest design system with dynamic colors
- **Dark Mode**: Full dark theme support
- **Glassmorphism**: Modern overlay effects on map
- **Smooth Animations**: Subtle transitions and loading states

### 🏗️ Architecture
- **Clean Architecture**: Clear separation of concerns (Presentation → Domain → Data)
- **MVVM Pattern**: ViewModel-based state management
- **StateFlow**: Reactive state updates with Kotlin Coroutines
- **Repository Pattern**: Single source of truth for data
- **Use Cases**: Encapsulated business logic
- **Dependency Injection Ready**: Manual DI with Hilt-ready structure

### 🚀 Performance
- **Background Tile Loading**: No UI blocking during map tile downloads
- **Optimized Recomposition**: Proper Compose best practices
- **Lifecycle-Aware**: Automatic cleanup and resource management
- **Timeout Handling**: Prevents hanging location requests

### 🛡️ Robust Error Handling
- **Comprehensive Error Types**: Typed errors for all scenarios
- **User-Friendly Messages**: Clear error explanations
- **Recovery Actions**: Retry, open settings, request permission
- **Graceful Degradation**: App remains functional on errors

---

## 📸 Screenshots

| Location Screen | Map View | GPS Signal Quality |
|----------------|----------|-------------------|
| *Coming soon* | *Coming soon* | *Coming soon* |

---

## 🛠️ Tech Stack

### Core
- **Language**: Kotlin 2.1.0
- **UI Framework**: Jetpack Compose
- **Build System**: Gradle Kotlin DSL
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35

### Jetpack Libraries
- **Compose BOM**: 2024.11.00
- **Material 3**: Dynamic colors, theming
- **Navigation Compose**: 2.8.4
- **Lifecycle ViewModel**: 2.8.7
- **Lifecycle Runtime Compose**: 2.8.7

### Location & Maps
- **Google Play Services Location**: 21.3.0
- **Accompanist Permissions**: 0.36.0
- **MapCompose**: 3.1.0 (OpenStreetMap)

### Async & Reactive
- **Kotlin Coroutines**: 1.9.0
- **StateFlow/Flow**: Reactive state management

### Testing
- **JUnit**: 4.13.2
- **Coroutines Test**: 1.9.0
- **Turbine**: 1.1.0 (Flow testing)
- **Compose UI Test**: Latest via BOM

---

## 📦 Project Structure

```
app/src/main/java/com/example/trackerapp/
├── core/
│   ├── design/              # Design system (colors, typography, theme)
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   ├── Spacing.kt
│   │   └── Theme.kt
│   ├── utils/               # Utilities and extensions
│   │   ├── LocationExtensions.kt
│   │   └── CoordinateFormatter.kt
│   └── constants/           # App-wide constants
│       └── AppConstants.kt
│
├── data/
│   ├── repository/          # Repository implementations
│   │   └── LocationRepositoryImpl.kt
│   └── source/              # Data sources
│       └── LocationDataSource.kt
│
├── domain/
│   ├── model/               # Domain models
│   │   ├── Location.kt
│   │   └── LocationResult.kt
│   ├── repository/          # Repository interfaces
│   │   └── LocationRepository.kt
│   └── usecase/             # Business logic use cases
│       ├── GetCurrentLocationUseCase.kt
│       └── StartLocationUpdatesUseCase.kt
│
└── presentation/
    ├── location/            # Location screen
    │   ├── LocationScreen.kt
    │   ├── LocationViewModel.kt
    │   └── LocationUiState.kt
    ├── map/                 # Map screen
    │   ├── MapScreen.kt
    │   ├── MapViewModel.kt
    │   └── MapUiState.kt
    ├── components/          # Reusable UI components
    │   ├── Buttons.kt
    │   ├── Cards.kt
    │   └── Indicators.kt
    └── navigation/          # Navigation setup
        └── Navigation.kt
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or higher
- Android SDK 35
- Gradle 8.3+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/TrackerApp.git
   cd TrackerApp
   ```

2. **Open in Android Studio**
   - File → Open → Select the `TrackerApp` directory
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**
   - Connect Android device or start emulator
   - Click "Run" (▶️) in Android Studio
   - Or: `./gradlew installDebug`

### Configuration

#### Location Permissions
The app automatically requests location permissions at runtime. Permissions required:
- `ACCESS_FINE_LOCATION`: High-accuracy GPS
- `ACCESS_COARSE_LOCATION`: Network-based location

#### Internet Permission
Required for downloading map tiles:
- `INTERNET`

All permissions are declared in `AndroidManifest.xml`.

---

## 📱 Usage

### Getting Your Location

1. **Launch the app** - Opens to Location Screen
2. **Grant permissions** - Tap "Grant Permission" when prompted
3. **Get location** - Tap "Get Location" button
4. **View details** - See coordinates, accuracy, signal quality

### Viewing on Map

1. **Navigate to map** - Tap "Show Map" button
2. **Interact with map**:
   - **Zoom**: Use + / - buttons or pinch gesture
   - **Pan**: Drag to move around
   - **Center**: Tap 📍 button to center on your location
3. **View info**: Position card shows current coordinates

### Coordinate Formats

The app displays coordinates in:
- **Decimal Degrees (DD)**: `52.520008° N, 13.404954° E`
- **Degrees Decimal Minutes (DDM)**: `52° 31.200' N, 13° 24.297' E` *(Coming)*
- **Degrees Minutes Seconds (DMS)**: `52° 31' 12.0" N, 13° 24' 17.8" E` *(Coming)*

---

## 🏛️ Architecture

TrackerApp follows **Clean Architecture** with clear layer separation:

### Layer Overview

```
┌──────────────────────────────────────┐
│      Presentation Layer              │
│  (Screens, ViewModels, UI States)   │
└───────────────┬──────────────────────┘
                │
┌───────────────▼──────────────────────┐
│         Domain Layer                 │
│  (Use Cases, Models, Repositories)   │
└───────────────┬──────────────────────┘
                │
┌───────────────▼──────────────────────┐
│          Data Layer                  │
│  (Repository Impl, Data Sources)     │
└──────────────────────────────────────┘
```

### Key Principles

1. **Dependency Rule**: Inner layers don't know about outer layers
2. **Dependency Inversion**: Depend on abstractions (interfaces)
3. **Single Responsibility**: Each class has one clear purpose
4. **Separation of Concerns**: UI logic ≠ Business logic ≠ Data logic

For detailed architecture documentation, see [ARCHITECTURE.md](./ARCHITECTURE.md).

---

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- **ViewModels**: State transitions and business logic
- **Use Cases**: Business rules and error handling
- **Repository**: Data transformations
- **UI**: Compose UI tests (in progress)

---

## 🎨 Design System

### Colors

| Color | Hex | Usage |
|-------|-----|-------|
| Airbus Blue | `#003087` | Primary, trust |
| Sky Blue | `#00A3E0` | Secondary, aviation |
| Safety Orange | `#FF6B35` | Accent, CTA |
| Silver | `#C0C0C0` | Tertiary, precision |

### Typography
- **Headlines**: Bold, commanding presence
- **Body**: Regular with 1.6 line height for readability
- **Coordinates**: Monospace for technical data

### Spacing
- **Base Unit**: 4dp
- **Grid System**: 8dp
- **Consistent padding**: 16dp (md) for most containers

---

## 🔧 Configuration

### Build Variants
- **Debug**: Development builds with debugging enabled
- **Release**: Production builds with ProGuard *(configured)*

### Build Configuration
```kotlin
android {
    compileSdk = 35
    minSdk = 26
    targetSdk = 35

    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

---

## 🐛 Known Issues

- Map tiles require internet connection (no offline support yet)
- Continuous location tracking not yet implemented
- Settings screen in development

---

## 🗺️ Roadmap

### Version 1.1
- [ ] Continuous location tracking
- [ ] Location history with Room database
- [ ] Distance and speed calculations
- [ ] Settings screen (coordinate format, theme)

### Version 1.2
- [ ] Export location data (GPX, KML)
- [ ] Offline map caching
- [ ] Route visualization
- [ ] Waypoint management

### Version 2.0
- [ ] Background location service
- [ ] Geofencing
- [ ] Multi-user support
- [ ] Cloud synchronization

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable/function names
- Add KDoc comments for public APIs
- Write tests for new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

**TrackerApp Development Team**
- Architecture & Implementation
- Design System inspired by Airbus aviation aesthetic
- Clean Architecture by Robert C. Martin

---

## 🙏 Acknowledgments

- **OpenStreetMap**: Map tiles and data
- **Material Design 3**: Design system and components
- **Jetpack Compose**: Modern UI toolkit
- **Airbus**: Design inspiration
- **Clean Architecture**: Robert C. Martin

---

## 📞 Support

For issues, questions, or suggestions:
- **Issues**: [GitHub Issues](https://github.com/yourusername/TrackerApp/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/TrackerApp/discussions)
- **Email**: your.email@example.com

---

## 📊 Project Status

**Current Version**: 1.0.0
**Status**: ✅ Production Ready
**Last Updated**: December 2025

---

## 🌟 Star History

If you find this project useful, please consider giving it a ⭐️!

---

**Built with ❤️ and Kotlin**
