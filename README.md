# Titanium Trades Companion ⚡

A native **crypto trading dashboard companion** for Android. Live price tracking, position P/L, and **stop-loss / take-profit push alerts** delivered straight to your phone — even when the app is closed.

Built with **Kotlin + Jetpack Compose + Material 3** on a deep **OLED-black & gold** palette.

![Android Build](https://github.com/messaynew-cyber/titanium-trades-companion/actions/workflows/android-build.yml/badge.svg)

---

## ✨ Features

- 🔴 **Live SOL/USD price** — auto-refreshes every 30s (CoinGecko feed, Binance fallback).
- 📈 **Position dashboard** — at-a-glance status vs your stop-loss & take-profit (In Profit / In Loss / SL hit / TP reached).
- 🔔 **Push alerts** — a background WorkManager worker checks the price every 15 min and fires a high-priority notification the moment price crosses your SL or TP (works with the app closed).
- 💰 **P/L tracking** — instant % return vs entry.
- 🎨 **Premium dark UI** — OLED-black background + gold accents, monospace price numerals, trading-terminal feel.
- 📱 **Works offline gracefully** — caches last price, no crash when the feed is unreachable.

## 🛠 Stack

| Layer | Tech |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Language | Kotlin |
| Architecture | Single-activity, MVVM, Unidirectional Data Flow |
| State | StateFlow + `StatefulFlow`, `collectAsStateWithLifecycle` |
| Nav | Navigation Compose (type-safe routes) |
| Background | WorkManager periodic worker (survives app kill) |
| Price feed | CoinGecko public API → Binance fallback (no API key needed) |
| Unit tests | JUnit (pure `AlertEngine` logic) |

## 🚀 Getting Started

**Option A — Just install it (no code):**
Grab the debug APK from the **Actions** tab (or a tagged release asset) and sideload it.

**Option B — Build from source:**
```bash
./gradlew :app:assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

## 🎯 Usage

1. Open the app → the dashboard boots straight to the live SOL price.
2. Tap **⚙ (top-right)** → enter your **entry**, **stop-loss**, and **take-profit**.
3. Grant the notification permission when prompted (Android 13+).
4. Let it run — you'll get a push the moment price hits your levels, app open or not.

## 🧪 Tests

```bash
./gradlew testDebugUnitTest
```
`AlertEngineTest` covers SL/TP trigger logic, alert suppression, and P/L math — no device required.

## 📁 Project Layout

```
app/src/main/java/com/titanium/trades/
├── TitaniumApp.kt          # Application: notification channels + schedules worker
├── MainActivity.kt          # Single activity + Compose navigation
├── MainViewModel.kt        # UI state holder (StateFlow combine)
├── data/
│   ├── model/Models.kt      # TradeConfig, PriceSnapshot, signals
│   ├── PriceApi.kt          # CoinGecko → Binance price fetch (IO dispatcher)
│   ├── AlertEngine.kt       # Pure SL/TP alert decision logic (unit-tested)
│   └── TradeRepository.kt   # Config persistence (SharedPreferences) + StateFlow
├── notif/
│   └── NotificationHelper.kt # Alert notifications
├── worker/
│   └── PriceAlertWorker.kt  # Background 15-min price check
└── ui/
    ├── theme/               # OLED-black + gold Material 3 theme
    └── screens/             # Dashboard + Settings
```

## 📄 License

MIT — use it, fork it, trade safer.
