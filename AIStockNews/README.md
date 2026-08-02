# AI Stock News 📈📰

**AI Stock News** is a modern, privacy-focused Android application that provides real-time Indian stock market intelligence, institutional **Smart Money** tracking (FII, DII, Mutual Funds, Promoters, Large Investors), AI-driven Market News aggregation, live stock search, and 1-minute background auto-refreshing.

---

## 📱 Features

- 💼 **Smart Money Tracking:** Track institutional buying/selling activities across Mutual Funds, FIIs, DIIs, Promoters, and Large Investor portfolios with real-time stock search.
- 📰 **AI Market News Feed:** Real-time news updates with AI sentiment analysis (Positive, Neutral, Negative) and relative timestamps ("Just now", "2 mins ago").
- 🔍 **Live Stock Directory:** Real-time search across Top 500 Indian stocks (NSE/BSE) with instant price flashes.
- 🔄 **1-Minute Background Auto-Refresh:** Continuous 60-second periodic background loop updating stock quotes and news timestamps.
- 🌐 **Offline Network Protection:** Mandatory active internet connection detector overlay ensuring zero stale cache errors.
- 📢 **AdMob Monetization:** Pinned sticky bottom-bar Banner Ads and App Open Ads integration.
- ⚖️ **SEBI Compliance Notice:** Explicit initial disclaimer detailing informational status and non-SEBI registered nature of the tool.

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose & Material 3
- **Architecture:** MVVM (Model-View-ViewModel), StateFlow, Coroutines
- **Navigation:** AndroidX Navigation3
- **Monetization:** Google Mobile Ads (AdMob) SDK (`play-services-ads:23.0.0`)
- **Networking & Data:** ConnectivityManager, Yahoo Finance Live Sync

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Jellyfish (or higher)
- JDK 17
- Android SDK Platform 36 (minSdk 24)

### Build Command
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew --no-configuration-cache clean assembleDebug
```

---

## 📄 License & Disclaimer
AI Stock News is strictly an informational tool. We are NOT registered with SEBI. All data is gathered from public open sources.
Developer Support: [ultraspeakai@gmail.com](mailto:ultraspeakai@gmail.com)
