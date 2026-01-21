# TrackerApp Architecture

## Overview

TrackerApp follows **Clean Architecture** principles with clear separation of concerns across three distinct layers. This architecture ensures the app is testable, maintainable, and scalable.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────┐   │
│  │  LocationScreen │  │  MapScreen  │  │   Components     │   │
│  └──────┬──────┘  └──────┬──────┘  └──────────────────┘   │
│         │                 │                                  │
│  ┌──────▼──────┐   ┌─────▼──────┐                          │
│  │ LocationVM  │   │  MapViewModel│                          │
│  └──────┬──────┘   └─────┬──────┘                          │
└─────────┼──────────────────┼───────────────────────────────┘
          │                  │
┌─────────▼──────────────────▼───────────────────────────────┐
│                      Domain Layer                            │
│  ┌──────────────────┐  ┌──────────────────────────────┐   │
│  │   Use Cases      │  │  Repository Interfaces        │   │
│  │ - GetLocation    │  │ - LocationRepository          │   │
│  │ - StartUpdates   │  │                               │   │
│  └─────────┬────────┘  └───────────┬──────────────────┘   │
│            │                        │                        │
│  ┌─────────▼────────────────────────▼──────────────────┐   │
│  │            Domain Models                             │   │
│  │   - Location                                         │   │
│  │   - LocationResult                                   │   │
│  │   - LocationError                                    │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────────────┬──────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────┐
│                       Data Layer                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │        LocationRepositoryImpl                        │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │          LocationDataSource                          │   │
│  │  - FusedLocationProviderClient                       │   │
│  │  - Location Permission Checks                        │   │
│  │  - Location Service Availability                     │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────┘
```

## Layer Breakdown

### 1. Presentation Layer (`presentation/`)

**Responsibility:** UI and user interaction

**Components:**
- **Screens:** Composable functions for each screen
  - `LocationScreen`: Main location tracking screen
  - `MapScreen`: Map visualization screen

- **ViewModels:** State management and UI logic
  - `LocationViewModel`: Manages location screen state
  - `MapViewModel`: Manages map screen state

- **UI States:** Sealed classes representing screen states
  - `LocationUiState`: Idle, Loading, Success, Error, PermissionDenied
  - `MapUiState`: Data class with location, zoom, loading states

- **Reusable Components:** (`components/`)
  - Buttons (Primary, Secondary, Accent)
  - Cards (Elevated, Info, Glass)
  - Indicators (Signal Quality, Loading, Error)

**Key Principles:**
- **StateFlow** for reactive state management
- **Unidirectional data flow**: Actions → ViewModel → State → UI
- **Lifecycle-aware** coroutines (viewModelScope)
- No business logic in composables

**Dependencies:** Domain layer only (via use cases)

---

### 2. Domain Layer (`domain/`)

**Responsibility:** Business logic and rules (platform-independent)

**Components:**

#### Models (`model/`)
- `Location`: Core domain model for geographical position
- `LocationResult`: Sealed class for operation results
- `LocationError`: Sealed class for error types
- `Result<T>`: Generic result wrapper with Loading/Success/Error

#### Repository Interfaces (`repository/`)
- `LocationRepository`: Contract for location data access
  - `getCurrentLocation(): LocationResult`
  - `getLocationUpdates(): Flow<LocationResult>`
  - `stopLocationUpdates()`
  - `hasLocationPermission(): Boolean`
  - `isLocationEnabled(): Boolean`

#### Use Cases (`usecase/`)
- `GetCurrentLocationUseCase`: Single location request
  - Validates permissions
  - Checks location services
  - Returns LocationResult

- `StartLocationUpdatesUseCase`: Continuous location tracking
  - Manages location update stream
  - Provides Flow of updates

**Key Principles:**
- **Platform-independent**: No Android framework dependencies
- **Dependency Inversion**: Depends on abstractions (interfaces)
- **Single Responsibility**: Each use case has one clear purpose
- **Immutable data**: Domain models are immutable data classes

**Dependencies:** None (pure Kotlin)

---

### 3. Data Layer (`data/`)

**Responsibility:** Data access and external service integration

**Components:**

#### Data Source (`source/`)
- `LocationDataSource`: Android-specific location implementation
  - Uses `FusedLocationProviderClient`
  - Handles permissions with `ContextCompat`
  - Provides coroutine-based async operations
  - **Critical Fix**: Tile loading on IO dispatcher (not UI thread)

#### Repository Implementation (`repository/`)
- `LocationRepositoryImpl`: Implements LocationRepository interface
  - Delegates to LocationDataSource
  - Could add caching logic
  - Transforms Android Location to domain model

**Key Principles:**
- **Repository Pattern**: Single source of truth
- **Abstraction**: Hides Android implementation details
- **Error Handling**: Comprehensive try-catch with typed errors
- **Coroutines**: Async operations with proper dispatchers

**Dependencies:**
- Domain layer (implements interfaces)
- Android framework (Location Services, Context)

---

## Cross-Cutting Concerns

### Core Layer (`core/`)

Shared utilities and constants used across layers:

#### Design System (`design/`)
- **Color.kt**: Airbus-inspired color palette
  - Primary: Deep Blue (#003087)
  - Secondary: Sky Blue (#00A3E0)
  - Accent: Safety Orange (#FF6B35)
  - Semantic colors for GPS signal quality

- **Typography.kt**: Material 3 typography system
  - Custom monospace style for coordinates
  - Consistent font hierarchy

- **Theme.kt**: Material 3 theme implementation
  - Dynamic color support (Android 12+)
  - Full dark mode support
  - Edge-to-edge theming

- **Spacing.kt**: 8dp grid system
  - Consistent spacing units
  - Corner radii definitions

#### Utilities (`utils/`)
- **LocationExtensions.kt**: Location helper functions
  - Distance/bearing calculations
  - Signal quality assessment
  - Mercator projection conversion

- **CoordinateFormatter.kt**: Coordinate formatting
  - Decimal Degrees (DD)
  - Degrees Decimal Minutes (DDM)
  - Degrees Minutes Seconds (DMS)

#### Constants (`constants/`)
- **AppConstants.kt**: Application-wide constants
  - Location update intervals
  - Map configuration
  - Animation durations
  - Routes

---

## Data Flow Example

### Scenario: User requests current location

```
1. User taps "Get Location" button
   ↓
2. LocationScreen calls: viewModel.getCurrentLocation()
   ↓
3. LocationViewModel:
   - Sets state to Loading
   - Launches coroutine
   - Calls: getCurrentLocationUseCase()
   ↓
4. GetCurrentLocationUseCase:
   - Checks hasLocationPermission()
   - Checks isLocationEnabled()
   - Calls: repository.getCurrentLocation()
   ↓
5. LocationRepositoryImpl:
   - Delegates to: dataSource.getCurrentLocation()
   ↓
6. LocationDataSource:
   - Creates cancellation token with timeout
   - Calls: fusedLocationClient.getCurrentLocation()
   - Waits for result with timeout
   - Converts Android Location to domain Location
   - Returns: LocationResult.Success(location)
   ↓
7. Use Case validates location
   ↓
8. ViewModel updates state:
   - _uiState.value = LocationUiState.Success(location)
   ↓
9. LocationScreen recomposes
   - Collects new state via uiState.collectAsState()
   - Displays location with SignalQualityIndicator
```

---

## Design Decisions

### Why Clean Architecture?

1. **Testability**: Each layer can be tested independently
2. **Maintainability**: Clear separation of concerns
3. **Scalability**: Easy to add features without breaking existing code
4. **Flexibility**: Can swap implementations (e.g., different location provider)

### Why StateFlow over LiveData?

- Native Kotlin coroutine support
- Better composable integration
- Type-safe null handling
- Easier testing

### Why Manual DI (not Hilt)?

- Simplicity for this project size
- Demonstrates architecture without DI framework overhead
- Easy to migrate to Hilt later if needed

### Why Sealed Classes for States?

- **Type-safe**: Compile-time checking
- **Exhaustive**: Forces handling of all cases
- **Clear**: Each state has exactly the data it needs

---

## Error Handling Strategy

### Layered Error Handling

1. **Data Layer**:
   - Catches all exceptions
   - Converts to domain-specific errors
   - Example: `SecurityException` → `LocationError.PermissionDenied`

2. **Domain Layer**:
   - Validates business rules
   - Returns typed errors via `LocationResult`

3. **Presentation Layer**:
   - Maps errors to UI states
   - Displays user-friendly messages
   - Provides recovery actions (retry, open settings)

### Error Types

```kotlin
sealed class LocationError {
    PermissionDenied    // User hasn't granted permission
    ServicesDisabled    // Location services off
    Timeout             // Request took too long
    NetworkError        // Connectivity issue
    ProviderUnavailable // GPS unavailable
    Unknown             // Unexpected error
}
```

---

## Performance Optimizations

### 1. Tile Loading (MapScreen)

**Problem (Original):** HTTP tile loading on UI thread caused lag

**Solution:**
```kotlin
runBlocking(Dispatchers.IO) {
    // Load tiles in background
}
```

### 2. State Management

- **StateFlow** instead of mutableStateOf for better lifecycle handling
- **Immutable states** prevent accidental mutations
- **Lazy loading** where appropriate

### 3. Compose Best Practices

- `remember` for expensive calculations
- `LaunchedEffect` with proper keys
- Stable parameters to avoid recomposition
- `derivedStateOf` for computed values

---

## Testing Strategy

### Unit Tests

1. **ViewModels**:
   - Test state transitions
   - Mock use cases
   - Verify coroutine behavior

2. **Use Cases**:
   - Test business logic
   - Mock repositories
   - Verify error handling

3. **Repository**:
   - Test data transformations
   - Mock data sources

### UI Tests

- Compose UI tests with `@Preview`
- Screenshot tests for design verification

---

## Future Enhancements

### Short-term
- [ ] Add location history (Room database)
- [ ] Implement continuous tracking
- [ ] Add distance/speed calculations
- [ ] Settings screen for format preferences

### Medium-term
- [ ] Migrate to Hilt for DI
- [ ] Add comprehensive test coverage
- [ ] Implement offline map caching
- [ ] Add export functionality (GPX, KML)

### Long-term
- [ ] Background location service
- [ ] Route planning
- [ ] Multi-user support
- [ ] Cloud sync

---

## Dependencies

### Core
- **Kotlin**: 2.1.0
- **Coroutines**: 1.9.0
- **Android SDK**: compileSdk 35, minSdk 26

### Jetpack
- **Compose BOM**: 2024.11.00
- **Material 3**: Latest via BOM
- **Navigation Compose**: 2.8.4
- **Lifecycle**: 2.8.7

### Location
- **Play Services Location**: 21.3.0
- **Accompanist Permissions**: 0.36.0

### Maps
- **MapCompose**: 3.1.0 (OpenStreetMap)

---

## Contributors

**Architecture Design:** Clean Architecture principles by Robert C. Martin
**Implementation:** TrackerApp Development Team
**Design System:** Inspired by Airbus aviation aesthetic

---

## License

[Your License Here]
