# Porter Location Assistant - Implementation Plan

An intelligent, safety-oriented driver companion app for Porter partners that monitors incoming booking offers in real-time, extracts destination and pickup details, calculates the exact straight-line distance from a driver's fixed home location using the Haversine formula, and displays an immediate visual warning (WITHIN RANGE, REVIEW, OUTSIDE RANGE, or UNKNOWN) via a lightweight floating overlay.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and will be adhered to throughout implementation:
> - **Timely Warning Style**: Floating overlay badge rendered over the screen via Android `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` with clear dismiss, manual override, and non-blocking layout parameters.
> - **Geocoding Engine**: Android system `Geocoder` paired with a robust asynchronous fallback to OpenStreetMap (Nominatim) and a local persistent coordinate cache for high speed and resilience in Indian metropolitan areas (e.g. Chennai).
> - **Pre-Populated Test Presets**: Includes the exact scenario from the driver's screenshot (Ponniyammanmedu pickup to Kolathur destination, Chennai, Tamil Nadu) in the booking simulator for instant validation.
> - **Strict Threshold Consistency**: Per the user prompt specification and Haversine boundaries (Default Radius = 20 km, Buffer = 1 km):
>   - `< 19.0 km` (radius - buffer) $\rightarrow$ **WITHIN RANGE** (Green)
>   - `19.0 km .. 21.0 km` (radius $\pm$ buffer) $\rightarrow$ **REVIEW** (Amber)
>   - `> 21.0 km` (radius + buffer) $\rightarrow$ **OUTSIDE RANGE** (Red)
>   - Coordinates missing / unresolvable $\rightarrow$ **UNKNOWN** (Gray)

---

### 1. Overview & Core Concept

When a driver receives a Porter order, the offer dialog is visible for approximately 15 seconds. Drivers must quickly determine whether the drop location is within a manageable distance from home. 

The **Porter Location Assistant** runs as an authorized background accessibility service, detects the presence of a Porter booking modal, extracts destination text, resolves latitude/longitude asynchronously, computes distance from the driver's fixed home base, and presents a color-coded status badge directly on top of the screen within 1 second. Drivers retain 100% manual control over booking acceptance; the app never clicks "Accept" or "Skip".

---

### 2. User Experience & Visual Design

#### A. Aesthetic Direction & Theme
- **Theme**: Utilitarian, high-contrast, modern Material 3 design with dark mode support.
- **Palette**: Deep navy/slate foundation (`#0D1520`), electric cyan primary accent (`#0284C7`), crisp typography, with high-visibility semantic status badges:
  - Green (`#16A34A` / `#22C55E`): WITHIN RANGE
  - Amber (`#D97706` / `#F59E0B`): REVIEW
  - Red (`#DC2626` / `#EF4444`): OUTSIDE RANGE
  - Slate Gray (`#64748B` / `#94A3B8`): UNKNOWN / PENDING

#### B. Core App Navigation & Screens
The app features an M3 Bottom Navigation Bar with 4 main tabs:
1. **Dashboard**:
   - Status header: Live Monitoring pill (`STOPPED`, `RUNNING`, or `PERMISSION REQUIRED`).
   - One-tap Start / Stop Monitoring controls.
   - Fixed Home summary card with quick edit button.
   - Latest Detected Booking card displaying pickup, destination, calculated straight-line distance, classification pill, extraction duration, and resolution time.
   - Quick Diagnostic Card showing service health, overlay permission, accessibility binding state, and geocoder latency.
2. **Home Location & Range Settings**:
   - Editable Home Coordinates (Latitude & Longitude) with format validation.
   - "Use Current GPS Location" button (requests location permission on-demand).
   - Saved label (e.g., "Home - Kolathur, Chennai").
   - Destination Radius slider & input (Default: `20 km`).
   - Review Buffer slider & input (Default: `1 km`).
   - Real-time dynamic preview of resulting zones (`< 19 km`, `19-21 km`, `> 21 km`).
   - Persisted safely via Jetpack DataStore / Room preferences.
3. **Booking Simulator & Test Lab**:
   - Text paste area for raw accessibility tree text or mock booking payload.
   - Presets dropdown: "Chennai Kolathur Offer (from Screenshot)", "Anna Nagar (Nearby)", "Tambaram (Far - Outside Range)", "Ambiguous Landmark (Review)".
   - Manual overrides for pickup text, destination text, and direct test coordinates.
   - "Run Simulation" button: simulates accessibility event propagation, parses fields, calculates distance, and triggers the test overlay badge.
   - Detailed inspection card showing parse confidence, tokens matched, regex extraction breakdown, and geocoding duration.
4. **Detection History**:
   - Filterable, searchable list of past detections stored in Room SQLite.
   - Filter by classification (Within Range, Review, Outside Range, Unknown).
   - Clear history and retention limits (e.g. 100 items).
   - Detailed dialog viewing raw extracted text, resolution timestamps, and processing latency.

#### C. Floating Status Overlay
- Lightweight non-intrusive HUD rendered via `WindowManager`:
  - Positioned safely near the top/middle of the screen, draggable to prevent obstructing Porter's "Accept in 7s" or "Skip" buttons.
  - Shows Destination Name, Straight-Line Distance (`X.X km from home`), and prominent Colored Status Tag.
  - Quick dismiss button (`✕`) and "Stop Service" button.
  - Automatically collapses or auto-dismisses after 15 seconds if not closed.

---

### 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale & Trade-offs |
|---|---|---|
| **Screen Inspection** | Android `AccessibilityService` (`AccessibilityNodeInfo`) | Zero-friction, battery-efficient, no video stream overhead, respects Android security model. Does not take continuous screenshots. |
| **OCR Fallback Architecture** | ML Kit Text Recognition ready module | Clean decoupling: if accessibility node inspection returns blank text for drop location (e.g. custom canvas in future Porter updates), fallback module can be triggered with explicit user media projection. |
| **Geocoding Strategy** | Android `Geocoder` + OpenStreetMap Nominatim fallback + Local LRU Cache | Guarantees instant response (<20ms) for previously seen areas while avoiding dependency on proprietary paid API keys. Works reliably for Indian addresses and Plus Codes (e.g. `45PG+JQC, Sri Nagar, Kolathur`). |
| **Distance Calculation** | Haversine Formula with Great Circle Geometry | Fast, deterministic, no network calls, strictly accurate for straight-line bounds. Labeled prominently as "Straight-line distance". |
| **Acceptance Automation** | Strictly Prohibited / User Manual Decision | Complies fully with Google Play policies, Porter Terms of Service, and safety requirements. The app assists decision-making; the driver taps their own screen. |

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       PORTER LOCATION ASSISTANT                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
          ┌───────────────────────────┴───────────────────────────┐
          ▼                                                       ▼
┌───────────────────────────────────┐               ┌──────────────────────────┐
│ UI Layer (Jetpack Compose M3)     │               │ Background Service Layer │
├───────────────────────────────────┤               ├──────────────────────────┤
│ • DashboardScreen                 │               │ • PorterAccessibility-   │
│ • HomeSettingsScreen              │               │   Service (Node parser)  │
│ • BookingTestScreen (Simulator)   │               │ • OverlayManager         │
│ • HistoryScreen                   │               │   (WindowManager HUD)    │
│ • Shared AppViewModel & StateFlow │               │ • LocationResolverWorker │
└─────────────────┬─────────────────┘               └─────────────┬────────────┘
                  │                                               │
                  ▼                                               ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ Domain & Processing Pipeline                                                 │
├──────────────────────────────────────────────────────────────────────────────┤
│ 1. BookingCardParser (Regex & semantic label extractor: Pickup / Drop)       │
│ 2. GeocodingEngine (Android Geocoder -> Nominatim HTTP -> In-Memory Cache)  │
│ 3. HaversineDistanceCalculator (Input validation, WGS-84 coordinates math)   │
│ 4. RangeClassifier (Radius & Review Buffer formula engine)                   │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ Data Layer (Room & Preferences)                                              │
├──────────────────────────────────────────────────────────────────────────────┤
│ • AppDatabase (Room 2.6) -> DetectionHistoryDao & HistoryEntity              │
│ • SettingsRepository -> DataStore / Encrypted SharedPrefs (Home lat, lng)   │
└──────────────────────────────────────────────────────────────────────────────┘
```

#### Step-by-Step Implementation Sequence:
1. **Dependencies & Manifest**:
   - Configure `build.gradle.kts` with Room, KSP, Coroutines, OkHttp (for Nominatim fallback), Lifecycle Compose, and Location services.
   - Configure `AndroidManifest.xml` with `SYSTEM_ALERT_WINDOW`, `BIND_ACCESSIBILITY_SERVICE`, `ACCESS_FINE_LOCATION`, `INTERNET`.
   - Register `PorterAccessibilityService` with `accessibility_service_config.xml`.
2. **Core Domain Engines**:
   - `HaversineCalculator`: pure Kotlin math function calculating distance between two `(lat, lon)` points in km.
   - `RangeClassifier`: classifies distance against `(radius, buffer)`.
   - `BookingCardParser`: parses pickup and destination text from node hierarchies or raw text strings, stripping extraneous numbers (fares like ₹137, timer countdowns like `7s`, and route km like `10.7 km`).
   - `GeocodingEngine`: handles geocoding with caching, timeouts, and fallback.
3. **Room Database & History**:
   - `DetectionRecord` entity, DAO with queries (`getAllDetections`, `insert`, `clearAll`).
   - `SettingsRepository` for Home location, radius, and buffer.
4. **Service & Floating Overlay**:
   - `PorterAccessibilityService` listening for `TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOW_CONTENT_CHANGED` filtering for Porter app package (`com.theporter.partner` or test simulator).
   - `OverlayWindowManager` presenting the M3 status overlay card.
5. **Compose UI Implementation**:
   - Modern Material 3 dark-themed UI with Dashboard, Settings, Test Screen, and History.
6. **Unit & Robolectric Tests**:
   - Comprehensive test suite for Haversine distance, boundary conditions, parser regexes, and classifier states.
