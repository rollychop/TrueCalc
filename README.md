# True Calc

True Calc is a feature-rich Android application designed to provide users with a comprehensive suite of financial and utility calculators. Built with modern Android development practices, it offers a seamless and professional experience.

## 🚀 Features

- **Financial Suite**: EMI Calculator, Compound Interest, Recurring Deposit, and Mutual Fund (SIP/Lumpsum) planners.
- **Health Tools**: BMI and other utility calculators.
- **Smart Search**: Quickly find the tool you need.
- **History & Favorites**: Save your frequent calculations for easy access.
- **In-App Updates**: Always stay on the latest version with seamless update integration.
- **Modern UI**: Built entirely with Jetpack Compose and Material 3.

## 🔮 Roadmap

- **Smart Assistant**: ask in plain English ("EMI for 25 lakh at 8.5% for 20 years") and get the answer from TrueCalc's own calculators, fully offline using an on-device Cactus Needle model. See [docs/smart-assistant](docs/smart-assistant/README.md).

## 🛠️ Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Dependency Injection**: Dagger Hilt
- **Database**: Room
- **Networking**: Retrofit & OkHttp
- **Analytics**: Firebase Analytics & Messaging
- **Concurrency**: Kotlin Coroutines & Flow
- **Architecture**: MVVM + Clean Architecture
- **Animations**: Lottie Compose
- **Charts**: Compose Charts

## 📁 Project Structure

```
app/src/main/java/com/brohit/truecalc/
├── common/         # Constants and global helpers
├── core/           # Core logic and math utilities
├── data/           # Repository impl, Room database, and API services
├── di/             # Hilt dependency injection modules
├── domain/         # Repository interfaces and domain models
└── ui/
    ├── components/ # Reusable UI components
    ├── navigation/ # Navigation graph and routes
    ├── screens/    # Feature-specific screens (EMI, History, etc.)
    └── utils/      # UI-specific helpers (Notifications, etc.)
```

## 🛠️ Setup & Installation

1. Clone the repository.
2. Open the project in Android Studio (Ladybug or later).
3. Ensure you have a `google-services.json` file in the `app/` directory (for Firebase features).
4. Build and run the `:app` module (`playDebug` variant, or `githubDebug` for the GitHub build).

## 📦 Releases

Two flavors: `play` (`com.brohit.truecalc`, Google Play) and `github` (`com.brohit.truecalc.github`, GitHub Releases; it installs alongside the Play app). Push a `v*` tag to publish a signed GitHub APK. See [docs/RELEASING.md](docs/RELEASING.md).

## 📄 License

This project is for personal use and development demonstration.
