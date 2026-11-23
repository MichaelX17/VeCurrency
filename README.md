# VeCurrency

VeCurrency is a native Android application developed in Kotlin using Jetpack Compose and Material 3. It provides users with Venezuelan currency exchange rates (USD BCV, EUR BCV, and USDT Binance P2P) along with a multi-currency conversion calculator operating with high monetary precision.

## Screenshots

| Dashboard Screen | Calculator Screen |
|---|---|
| ![Dashboard Screenshot](app/src/main/res/drawable/dashboard_preview.png) | ![Calculator Screenshot](app/src/main/res/drawable/calculator_preview.png) |

## App Features

- **Rates Dashboard**: Real-time display of USD (BCV), EUR (BCV), and USDT exchange rates with source attribution, update timestamps, and quick-copy functionality.
- **Conversion Calculator**: Multi-currency conversion supporting USD, EUR, USDT, and VES with instant swapping, custom decimal entry, and equivalency breakdown.
- **Price Trend History**: 5-day historical price tracking and visualization using locally persisted daily rate history and API synchronization.
- **Dashboard Snapshot Export**: High-resolution image summary generation and sharing via system sharesheet.
- **Smart Scheduling**: Foreground-only automatic synchronization respecting official BCV schedule slots (09:30, 15:00, 21:30 VET) and fixed USDT time slots (00:00, 04:00, 08:00, 12:00, 16:00, 20:00 VET).
- **Offline Resilience**: Local caching via Room database ensures instant offline availability and robust error resilience.

## Technologies Used

- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose, Material 3
- **Architecture**: MVVM with unidirectional data flow and Kotlin Coroutines & Flows
- **Local Persistence**: Room Database
- **Network & Parsing**: OkHttp 4, Jsoup, kotlinx.serialization
- **Dependency Management**: Gradle Version Catalogs, KSP

## Legal Disclaimer

VeCurrency is an independent informational tool. The developer does not manage, set, influence, or guarantee any financial prices, exchange rates, or market valuations displayed within the application. All rates are obtained directly from official public sources and third-party market providers for informational purposes only.
