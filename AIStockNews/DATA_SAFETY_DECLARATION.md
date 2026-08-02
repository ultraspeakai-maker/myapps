# Google Play Console Data Safety Questionnaire Guide - AI Stock News

This document provides exact answers for filling out the **Data Safety Questionnaire** in Google Play Console for **AI Stock News**.

---

## 1. Data Collection and Security
- **Does your app collect or share any of the required user data types?**  
  👉 **Yes** *(Google Mobile Ads SDK collects anonymized device identifiers for advertising & diagnostics)*.
- **Is all of the user data collected by your app encrypted in transit?**  
  👉 **Yes** *(All HTTP/HTTPS connections use standard TLS/SSL encryption)*.
- **Do you provide a way for users to request that their data be deleted?**  
  👉 **Yes** *(No user accounts or personal profiles are created)*.

---

## 2. Specific Data Types (Google AdMob SDK)

| Data Category | Data Type | Collected? | Shared? | Purpose | Optional / Required |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Device or other IDs** | Device or other IDs *(e.g., AAID / Advertising ID)* | Yes | Yes | Advertising or marketing, Analytics, App functionality | Required by AdMob SDK |
| **App info and performance** | Crash logs & Diagnostics | Yes | Yes | Analytics, Fraud prevention & Security | Required by Google Play services |

---

## 3. Play Store Security Declaration
- **Target Audience:** General Audience (Ages 13+)
- **SEBI Disclaimer Declaration:** The app contains an opening disclaimer indicating it is strictly an informational tool and is NOT SEBI registered.
- **Third-Party SDKs:** Google Mobile Ads SDK (AdMob `play-services-ads:23.0.0`).
