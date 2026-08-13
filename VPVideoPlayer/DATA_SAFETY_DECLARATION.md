# Google Play Data Safety Declaration & Questionnaire Answers

## App Details
- **App Name:** VP Video Player
- **Package Name:** `com.ultraspeakai.vpvideoplayer`
- **Developer Account:** UltraSpeak AI

---

## Data Safety Questionnaire Responses

### 1. Data Collection & Sharing
- **Does your app collect or share any of the required user data types?**
  - **Answer:** No user personal data (photos, videos, audio, contacts, files) is collected or shared.
- **Does your app collect device or other IDs for advertising?**
  - **Answer:** Yes, via Google Mobile Ads SDK (AdMob) for diagnostic & ad serving purposes.

### 2. Data Types Declared

| Data Type | Collected | Shared | Purpose | Optional / Mandatory |
|---|---|---|---|---|
| **Photos & Videos** | **NO** | **NO** | Local playback & trimming only | N/A |
| **Audio Files** | **NO** | **NO** | Local playback & extraction only | N/A |
| **Device or other IDs** (Advertising ID) | **YES** (by AdMob SDK) | **YES** (AdMob) | Advertising & Analytics | Mandatory for AdMob |
| **App Info & Performance** (Crash logs) | **YES** (by AdMob SDK) | **YES** (AdMob) | App functionality & diagnostics | Mandatory for AdMob |

---

## 3. Security Practices
- **Is data encrypted in transit?** Yes, Google AdMob SDK uses standard HTTPS/TLS encryption.
- **Does your app provide a way for users to request data deletion?** Since VP Video Player does not maintain any user accounts or remote database servers, all data resides on-device and is deleted automatically when the user uninstalls the app or clears app data.
