# 💊 Pharma-Bazaar — B2B Medicine Marketplace & Exchange
> **বিটুবি ফার্মাসিউটিক্যাল ইনভেন্টরি ও মেডিসিন এক্সচেঞ্জ প্ল্যাটফর্ম (B2B Pharma Inventory & Medicine Exchange App)**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Firestore-orange.svg)](https://firebase.google.com/)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%20API-4285F4.svg)](https://ai.google.dev/)

---

## 📌 Overview (বিবরণ)

**Pharma-Bazaar** is a modern B2B pharmaceutical marketplace app built for retail pharmacies, medicine wholesalers, and pharmaceutical distributors in Bangladesh. It connects pharmacy owners directly with wholesale suppliers to streamline medicine procurement, clear near-expiry inventory, optimize bulk pricing, and prevent medicine shortages.

---

## ✨ Key Features (প্রধান বৈশিষ্ট্যসমূহ)

### 🏬 1. B2B Medicine Marketplace & Inventory Listings
- Browse wholesale medicine offers with complete details: **Brand Name, Generic Name, Strength, Form (Tablet, Syrup, Injection), Manufacturer Company, Batch Number, Expiry Date, MRP, and Wholesale Discounted Price**.
- Highlighting for **Verified Sellers, Near-Expiry Clearance Deals, and Bulk Minimum Order Quantities (MOQ)**.

### 🔥 2. Firebase Cloud Firestore & FCM Integration
- **Real-time Synchronization**: Instant cloud sync for inventory listings, buy orders, and shipment updates via Cloud Firestore.
- **Push Notifications**: Real-time order status change and chat message alerts via Firebase Cloud Messaging (FCM).

### 🤖 3. Gemini AI Smart Match & Analytics
- **AI Substitute Suggestions**: Recommends alternative brands with identical generic compositions during supply shortages.
- **Demand & Expiry Analytics**: Gemini AI analyzes fast-moving inventory and alerts on approaching expiration dates to minimize losses.

### 🔍 4. Multi-Parametric Search & Quick Filters
- Instant search across **Medicine Name, Generic Formula, Manufacturer, and Supplier Name**.
- Quick category filters for **Tablets, Syrups, Injections, Verified Wholesalers, and Discount percentage**.

### 🛒 5. Order Management & In-App Supplier Chat
- **Buy Requests & Order Tracking**: Complete lifecycle tracking with status stepper (*Pending ➔ Confirmed ➔ Shipped ➔ Delivered*).
- **Direct Chat**: Built-in messaging between retail pharmacists and wholesale suppliers for price negotiation.

### 🏢 6. Supplier Dashboard & Inventory Management
- Dedicated seller interface to add, edit, or remove inventory listings.
- **CSV Inventory Export**: Export active inventory lists for local accounting and record-keeping.

### 🔖 7. Watchlist & Price Drop Alerts
- Add critical shortage medicines to a personal watchlist with automated price-drop and restock indicators.

---

## 🛠️ Technology Stack (প্রযুক্তি)

| Category | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Architecture** | MVVM (Model-View-ViewModel), Clean Architecture |
| **Local Persistence** | Room Database (SQLite) + KSP |
| **Cloud Backend** | Firebase Cloud Firestore & Firebase Cloud Messaging (FCM) |
| **Artificial Intelligence** | Google Gemini 1.5/2.0 API (Generative AI) |
| **Async & State** | Kotlin Coroutines, StateFlow, Flow |
| **Navigation** | Navigation Compose |

---

## 📂 Project Structure (প্রজেক্ট স্ট্রাকচার)

```
com.example/
├── data/
│   ├── db/                 # Room Database, DAOs, and Entities
│   ├── remote/             # Firestore Service & Remote APIs
│   └── repository/         # Unified Repository Pattern Implementation
├── service/                # FCM Messaging & Gemini Services
├── ui/
│   ├── components/         # Reusable Jetpack Compose UI Components
│   ├── screens/            # Application Screens (Home, Cart, Dashboard, Search, etc.)
│   ├── theme/              # Material 3 Color Schemes, Typography, Shapes
│   └── viewmodel/          # PharmaViewModel State Holders
├── util/                   # Notification & CSV Export Utilities
└── MainActivity.kt         # Entry Point & Navigation Controller
```

---

## ⚙️ Setup & Installation (ইনস্টলেশন গাইড)

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/akikto/Pharma-Bazaar.git
   ```
2. **Open in Android Studio**:
   Open Android Studio (Ladybug or newer recommended) and import the project folder.

3. **Firebase & Gemini Configuration**:
   - Add your `google-services.json` file in the `app/` directory for Firebase integration.
   - Set your Gemini API key in `.env` or system environment variables:
     ```env
     GEMINI_API_KEY=your_gemini_api_key_here
     ```

4. **Build & Run**:
   Sync Gradle dependencies and run the project on a physical device or emulator running Android 8.0 (API level 26) or higher.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
