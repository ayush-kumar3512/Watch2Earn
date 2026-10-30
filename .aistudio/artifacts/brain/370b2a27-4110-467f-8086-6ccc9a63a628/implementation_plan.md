# Watch2Earn - Unity Ads Production Suite & Live Telemetry

A production-grade native Android application featuring Unity Ads integration (Rewarded & Interstitial), persistent telemetry and performance analytics via DataStore, real-time network and IP geolocation telemetry, tactile haptic feedback, and a dark glassmorphic visual presentation.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions incorporate your preferences from the initial clarification:

- **Ad Mode Configuration**: Strictly set to production mode (`TEST_MODE = false`) with Unity Game ID `800407539`, rewarded placement `Rewarded_Android`, and interstitial placement `Interstitial_Android`.
- **Haptic Feedback**: Tactile haptic vibration enabled across ad triggers, button states, and coin reward celebrations.
- **Telemetry Ingestion**: Real-time IP and ISP geolocation from `https://ipwho.is/` executes upon launch and provides on-demand manual refresh with live state indication.
- **Button State Engine**: Non-disabled, always-clickable action triggers with inline loading spinners and a 30-second failsafe ad-wait timeout.

---

## 1. Overview & Core Concept

**Watch2Earn** serves as an interactive Unity Ads demonstration and real-time ad performance telemetry monitor. It allows mobile publishers and developers to experience production Unity Ads workflows firsthand, observe real listener callbacks, track live earnings estimates ($2.00 eCPM model), monitor user engagement (CTR, watched, skipped, load success/failure), and inspect live network parameters that directly influence ad fill rates (IP, region, ISP, connection type).

---

## 2. User Experience & Visual Design

### Key User Flows
1. **App Launch & Initialization**:
   - Unity Ads SDK initializes silently in the background with `IUnityAdsInitializationListener`.
   - Network geolocation fetch runs asynchronously against `ipwho.is` to resolve country, flag, region, city, and ISP.
   - Initial Rewarded and Interstitial ads are queued immediately via `UnityAds.load()`.
2. **Rewarded Ad Flow**:
   - User taps the "Watch Rewarded Ad (+10 Coins)" button.
   - If ready, ad displays immediately; if still loading, the button displays a nested circular progress indicator and locks further taps while waiting up to 30 seconds for the ad to resolve.
   - On completion (`onUnityAdsShowComplete` with `COMPLETED`), user receives +10 coins, haptic celebration, a coin burst animation, and a celebratory snackbar.
   - An ad reload is immediately triggered silently.
3. **Interstitial Ad Flow**:
   - User taps the "Show Interstitial Ad" button.
   - If ready, interstitial displays; on dismiss or skip, counters update and the next ad loads automatically.
4. **Live Telemetry & Diagnostics**:
   - Ads Engine Status card shows real-time status with green/amber/red indicators and verbatim raw error messages from Unity Ads SDK.
   - Performance metrics (clicks, impressions, skips, CTR %, coins, loads) persist across app restarts using Jetpack DataStore Preferences.
   - Users can reset telemetry counters at any time via a confirmation dialog.

### Visual Identity & Theme
- **Aesthetic Direction**: Premium dark glassmorphism with deep cosmic purple (`#0E0B1F`) to midnight navy (`#090D16`) gradient background.
- **Surface Elevation**: Translucent cards (`#1E1635` at 65% opacity) with subtle radiant borders (`#8A56FF` / `#483D8B` 25% alpha) and frosted blur effect styling.
- **Color Accents**:
  - Gold Accent: `#FFD700` and `#FFA000` for coins and reward badges.
  - Emerald Green: `#00E676` for active/ready states and load success.
  - Electric Violet: `#7C4DFF` for primary actions and interstitial triggers.
  - Crimson Amber: `#FF5252` and `#FFAB00` for skips, errors, and warnings.
- **Typography & Rhythm**: High-contrast, clean sans typography with tabular numerals for smooth count-up animations and session clocks.
- **Micro-Interactions**:
  - Button presses scale dynamically to 0.94 with spring damping.
  - Idle buttons feature a subtle undulating border shimmer.
  - Coin reward trigger triggers a multi-particle burst and animated tally increment.

---

## 3. Key Product Decisions & Trade-Offs

- **Plain Unity Ads SDK APIs Only**:
  - *Chosen Approach*: Exclusively use `UnityAds.initialize(context, UNITY_GAME_ID, TEST_MODE, listener)`, `UnityAds.load(placementId, listener)`, and `UnityAds.show(activity, placementId, UnityAdsShowOptions(), listener)`.
  - *Why*: Strict adherence to SDK contract prevents deprecation issues, invalid bidding parameters, or runtime crashes.
- **Raw Error Transparency**:
  - *Chosen Approach*: Display unadulterated error codes and messages returned by Unity Ads listeners (`UnityAdsLoadError`, `UnityAdsShowError`, `UnityAdsInitializationError`).
  - *Why*: Allows immediate diagnosis of ad network issues (e.g., NO_FILL, TIMEOUT, NOT_INITIALIZED) on real hardware.
- **Data Persistence via Jetpack DataStore**:
  - *Chosen Approach*: Use `androidx.datastore:datastore-preferences` with Kotlin Coroutines Flow for ad telemetry and coin totals.
  - *Why*: Crash-safe, transactional, asynchronous persistence that survives application lifecycle events without database overhead.
- **Real Network Telemetry via OkHttp**:
  - *Chosen Approach*: Direct OkHttp background query to `https://ipwho.is/` parsed safely with Moshi/JSON.
  - *Why*: Unity Ads fill rates heavily depend on IP reputation, Geo/Carrier, and connection stability. Real telemetry gives instant actionable context.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                                  │
│             (Portrait Only, Edge-to-Edge, Dark Theme)                     │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                             MainViewModel                                  │
│  - Session Timer StateFlow          - User Message SharedFlow              │
│  - Haptic Event Triggers            - Ad Button Pending State              │
└──────────────┬──────────────────────┬──────────────────────┬───────────────┘
               │                      │                      │
               ▼                      ▼                      ▼
┌─────────────────────────┐ ┌───────────────────┐ ┌─────────────────────────┐
│     UnityAdsManager     │ │ AdStatsRepository │ │ NetworkLocationService  │
│  - UnityAds.initialize  │ │ (DataStore Prefs) │ │ - OkHttp Dispatcher     │
│  - UnityAds.load        │ │ - Coins           │ │ - ipwho.is Endpoint     │
│  - UnityAds.show        │ │ - Impressions     │ │ - Real Geo & ISP Data   │
│  - Raw Error Collector  │ │ - Clicks & CTR    │ │ - Status & Errors       │
└─────────────────────────┘ └───────────────────┘ └─────────────────────────┘
               │                      │                      │
               └──────────────────────┼──────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                         Jetpack Compose UI Tree                            │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────┐  │
│  │     HeaderSection     │ │    CoinBalanceCard    │ │  AdsEngineCard   │  │
│  └───────────────────────┘ └───────────────────────┘ └──────────────────┘  │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────┐  │
│  │    ActionButtons      │ │   AdPerformanceGrid   │ │  NetworkCard     │  │
│  └───────────────────────┘ └───────────────────────┘ └──────────────────┘  │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────┐  │
│  │   DeviceSessionCard   │ │   CoinBurstOverlay    │ │  ResetStatDialog │  │
│  └───────────────────────┘ └───────────────────────┘ └──────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

### Module & Dependency Specifications
- Gradle Version Catalog `libs.versions.toml`:
  - `unityAds = "4.21.0"` -> `com.unity3d.ads:unity-ads`
  - `datastorePreferences = "1.1.7"` -> `androidx.datastore:datastore-preferences`
  - `okhttp = "4.10.0"`
- Target SDK: 34, Min SDK: 24, Application ID: `com.aizoid.unityadsdemo`.
- Permissions: `android.permission.INTERNET`, `android.permission.ACCESS_NETWORK_STATE`, `android.permission.VIBRATE`.
