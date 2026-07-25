# Google Play Console Data Safety Form Guide

When submitting **V Spy Camera - Unlimited Free Recordings** (`com.camera.secretvideorecorder`) to the Google Play Console, use the exact answers below for the **Data Safety Questionnaire**:

---

## 1. Data Collection and Security Questionnaire

### Q1: Does your app collect or share any of the required user data types?
- **Answer:** **Yes** (Reason: Google AdMob SDK processes device IDs for ad delivery. The app itself does not collect user files).

### Q2: Is all of the user data collected by your app encrypted in transit?
- **Answer:** **Yes** (AdMob ad requests use HTTPS/TLS encryption).

### Q3: Do you provide a way for users to request that their data be deleted?
- **Answer:** **Yes** (All video recordings are saved locally on the user's device. Users can delete any recording directly inside the App gallery or through their device File Manager).

---

## 2. Specific Data Types Breakdown

| Data Type | Collected? | Shared? | Purpose | Optional or Required? |
| :--- | :--- | :--- | :--- | :--- |
| **Photos & Videos** | **No** (Stored locally on device only) | **No** | N/A | N/A |
| **Audio Files** | **No** (Stored locally on device only) | **No** | N/A | N/A |
| **Device or other IDs** (Advertising ID) | **Yes** (By AdMob) | **Yes** (With AdMob) | Advertising & Analytics | Required for AdMob ad serving |
| **App Performance & Diagnostics** | **Yes** (By AdMob/Google Play Services) | **No** | Analytics / Crash reporting | Automated by SDK |

---

## 3. Play Console Foreground Service Declaration (Android 14+ / API 34)

When prompted for **Foreground Service Types**:
- Select: **Camera** (`FOREGROUND_SERVICE_CAMERA`)
- Select: **Microphone** (`FOREGROUND_SERVICE_MICROPHONE`)

### Justification Text for Reviewer:
> "V Spy Camera allows users to record high quality video and audio continuously while using other apps or with the device screen locked/turned off. A foreground service with a persistent notification is used to ensure the recording session remains active, stable, and visible to the user at all times."
