# Porter Location Assistant 🚚📍

**Porter Location Assistant** is a native Android companion application built for Porter drivers and delivery partners. Incoming booking offers on the Porter partner app are visible for only ~15 seconds. This assistant immediately extracts the destination address, computes the straight-line distance from the driver's fixed home base using the Haversine formula, classifies the offer against custom radius and review buffer thresholds, and presents a non-intrusive floating status badge over the screen.

---

## 1. Core Architecture & Workflow

1. **Fixed Home Base**: The driver configures their permanent home location (manual coordinates or GPS lock). This is strictly distinguished from dynamic GPS telemetry.
2. **Modular Inspection Pipeline**:
   - **Primary**: Android `AccessibilityService` (`PorterAccessibilityService`) inspects visible Porter window nodes without taking screenshots or consuming excessive battery.
   - **Secondary / OCR Fallback**: `OcrFallbackModule` ready for user-authorized screen capture if Porter renders custom Canvas UI.
3. **Smart Address Parsing**: `BookingCardParser` extracts pickup and drop address blocks while discarding fare amounts (e.g. `₹137`), countdown timers (`Accept in 7s`), ledger balances, and route distance numbers (`(10.7 km)`).
4. **Geocoding Engine**: `CompositeGeocodingEngine` leverages Android system `Geocoder`, OpenStreetMap Nominatim fallback, and an in-memory LRU cache to resolve Indian addresses and Plus Codes (e.g. `45PG+JQC, Sri Nagar, Kolathur, Chennai`).
5. **Distance & Classification Formula**:
   - Computes great-circle straight-line distance via `HaversineCalculator`.
   - **WITHIN RANGE** (Green): `distance < (radius - buffer)` (e.g., `< 19.0 km` with default 20 km radius and 1 km buffer).
   - **REVIEW** (Amber): `(radius - buffer) <= distance <= (radius + buffer)` (e.g., `19.0 km .. 21.0 km`).
   - **OUTSIDE RANGE** (Red): `distance > (radius + buffer)` (e.g., `> 21.0 km`).
   - **UNKNOWN** (Gray): Missing coordinates, unresolved location, or ambiguous addresses.
6. **Floating Status Overlay**: Draggable `WindowManager` HUD badge that displays destination, distance from home, and colored status pill without obstructing Porter's "Accept" or "Skip" buttons. Auto-dismisses after 15 seconds.
7. **Local Persistence**: Full detection history and processing durations are saved locally on-device using Room SQLite.

---

## 2. Permissions Required & Setup

| Permission | Purpose | Where to Enable |
|---|---|---|
| **Accessibility Service** | Detect incoming booking offer dialogs and read visible destination text | Settings > Accessibility > Installed Apps > Porter Location Assistant |
| **Display Over Other Apps** (`SYSTEM_ALERT_WINDOW`) | Display the floating range warning badge over Porter | Settings > Apps > Special App Access > Display Over Other Apps |
| **Location Permission** (`ACCESS_FINE_LOCATION`) | Set Home coordinates from current device location | Prompted when clicking "Fill from Current GPS Location" |
| **Internet Access** (`INTERNET`) | OpenStreetMap Nominatim geocoding fallback | Declared in Manifest (standard install-time) |

---

## 3. Booking Simulator & Test Mode

The app contains an integrated **Test Lab / Booking Simulator** that allows testing without waiting for a real Porter order:
- **Preset 1 (from Screenshot)**: Ponniyammanmedu pickup to Kolathur destination, Chennai (`45PG+JQC, Sri Nagar, Kolathur, Chennai 600053`).
- **Preset 2 (Nearby)**: Anna Nagar East offer (~6 km).
- **Preset 3 (Far / Outside Range)**: Tambaram Sanatorium (~28 km).
- **Preset 4 (Ambiguous)**: Truncated or generic address.
- Drivers can paste arbitrary text or override coordinates to verify the exact mathematical classification and overlay behavior.

---

## 4. Policy Compliance & Safety

- **No Auto-Tapping**: In strict compliance with Google Play policies and Porter Terms of Service, the application **never** automatically taps "Accept" or "Skip". The driver always makes the manual choice.
- **Local Privacy**: Home coordinates, booking details, and detection logs remain 100% on the driver's device and are never sent to external servers.

---

## 5. Build & Test Instructions

### Requirements
- **JDK**: Java 11 or 17
- **Android SDK**: `compileSdk 36`, `minSdk 24`, `targetSdk 36`
- **Gradle**: Kotlin DSL (`gradle-8.x`+)

### Running Tests
Execute JVM unit tests and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```

### Compiling APK
```bash
gradle :app:assembleDebug
```
The resulting debug APK is located at `app/build/outputs/apk/debug/app-debug.apk`.
