# 📄 Pharma-Bazaar — Final Known Issues & Launch Readiness

**Application:** Pharma-Bazaar B2B Exchange  
**Build Version:** 1.0.0-RC1  
**Date:** August 6, 2026

---

## 🛑 1. Blocking Issues

* **NONE** — There are zero blocking issues in the Release Candidate build. All core buyer, seller, chat, order tracking, and notification paths build cleanly and function as expected.

---

## ⚠️ 2. Non-Blocking Issues & Observability Notes

| Issue ID | Category | Description | Workaround / Status | Impact Level |
| :---: | :--- | :--- | :--- | :---: |
| **NBI-01** | Firebase | Google Services warning during offline compilation when `google-services.json` relies on fallback credentials. | Handled via `MissingGoogleServicesStrategy.WARN` in Gradle. Offline demo mode uses Room database seamlessly. | **Low** |
| **NBI-02** | Gemini API | Rate-limit quota warnings if AI substitute searches exceed free-tier request rate. | App falls back gracefully to internal Room local database generic composition matching. | **Low** |
| **NBI-03** | FCM | FCM background push delivery requires active Google Play Services on physical Android device. | In-app notification fallback helper displays alerts locally if Play Services are absent. | **Low** |

---

## 🚀 3. Recommended Enhancements Before Public Store Launch

1. **Production Google-Services Configuration**:
   - Replace project placeholder Firebase project IDs with production Google Cloud project keys prior to publishing on Google Play Store.

2. **Gemini API Key Protection**:
   - Store Gemini API key securely in Google AI Studio Secrets Panel or backend proxy to prevent exposure in client APK.

3. **Multi-Region Cloud Firestore Indexing**:
   - Add composite indexes in Firebase Console for custom queries sorting simultaneously by `wholesalePrice` and `expiryDate`.

---

## ✅ Launch Recommendation

The current build is **STABLE, POLISHED, AND APPROVED** for candidate release.
