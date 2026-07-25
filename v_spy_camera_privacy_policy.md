# Privacy Policy for V Spy Camera - Unlimited Free Recordings

**Effective Date:** July 25, 2026  
**App Name:** V Spy Camera - Unlimited Free Recordings  
**Developer Repository:** [https://github.com/ultraspeakai-maker/myapps](https://github.com/ultraspeakai-maker/myapps)  

---

## 1. Overview & Commitment to Privacy
**V Spy Camera - Unlimited Free Recordings** ("we", "our", or "the App") is committed to protecting your personal privacy. This application is designed to function primarily as a local background video recording utility. All video recordings, audio tracks, and media thumbnails generated through the App are stored **strictly on your local device** and are **never transmitted, uploaded, or shared** with external servers, databases, or third parties.

---

## 2. Information Collection and Storage
### A. Local Media Storage (Zero Server Transmission)
- **Captured Video & Audio:** All video recordings and captured audio remain 100% on your device's local storage within the public MediaStore directory (`DCIM/SecretVideoRecorder`).
- **No Cloud Services:** The App does not operate cloud storage servers, external user databases, or remote streaming backends.

### B. Technical & Advertising Data (Google AdMob)
The App is provided free of charge with no recording duration limits and is supported by Google AdMob advertising. AdMob may collect and process standard non-personally identifiable technical information to serve relevant banner, app open, and interstitial ads, including:
- Non-personal Device Identifier (e.g., Advertising ID)
- IP address (used strictly for coarse location/country level ad targeting)
- Diagnostic performance & crash metrics

For more details on how Google AdMob handles data, please review [Google's Privacy Policy](https://policies.google.com/privacy).

---

## 3. Device Permissions & Justification
To fulfill its primary functionality, **V Spy Camera** requests the following Android system permissions:

| Permission | Purpose & Necessity |
| :--- | :--- |
| `CAMERA` | Required to access device camera sensors (Front & Back) to record video. |
| `RECORD_AUDIO` | Required to record sound accompanying video recordings. |
| `FOREGROUND_SERVICE` | Enables the App to maintain an active recording session even when the screen is turned off or when navigating to other applications. |
| `FOREGROUND_SERVICE_CAMERA` | Complies with Android 14+ (API 34) requirements for foreground services accessing camera hardware. |
| `FOREGROUND_SERVICE_MICROPHONE` | Complies with Android 14+ (API 34) requirements for foreground services capturing microphone audio. |
| `POST_NOTIFICATIONS` | Displays a persistent status notification during active recording to ensure user awareness and system compliance. |
| `SYSTEM_ALERT_WINDOW` | Optional: Allows quick trigger overlays for background service management. |
| `VIBRATE` | Provides subtle haptic feedback when recording starts or stops. |
| `RECEIVE_BOOT_COMPLETED` | Restores user-configured scheduled recording alarms after device restart. |

---

## 4. Prominent Disclosure & Transparency
- **Visible Notification:** When background video recording is active, a persistent system notification is always active in the Android notification drawer.
- **User Control:** Users can start or stop recording at any time directly via the notification drawer or within the App dashboard.

---

## 5. Children's Privacy
The App does not knowingly collect or solicit personal information from children under the age of 13. The App contains no user registration or social features.

---

## 6. Security
Since all recorded media is stored locally in your device's file system (`DCIM/SecretVideoRecorder`), security of your saved videos is governed by your Android device's built-in security features (screen lock, encryption, biometric authentication).

---

## 7. Changes to This Privacy Policy
We may update our Privacy Policy periodically to reflect app updates or regulatory changes. Any updates will be posted directly to this document and our GitHub repository.

---

## 8. Contact & Developer Information
If you have any questions or feedback regarding this Privacy Policy, please reach out via our official GitHub project repository:

**Developer Repository:** [https://github.com/ultraspeakai-maker/myapps](https://github.com/ultraspeakai-maker/myapps)  
**App Package Name:** `com.camera.secretvideorecorder`
