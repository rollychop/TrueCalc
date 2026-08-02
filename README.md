# True Calc

True Calc is a feature-rich Android application designed to provide users with a comprehensive suite of financial and utility calculators. Built with modern Android development practices, it offers a seamless and professional experience.

## 🚀 Features

- **Financial Suite**: EMI Calculator, Compound Interest, Recurring Deposit, and Mutual Fund (SIP/Lumpsum) planners.
- **Health Tools**: BMI and other utility calculators.
- **Smart Search**: Quickly find the tool you need.
- **History & Favorites**: Save your frequent calculations for easy access.
- **In-App Updates**: Always stay on the latest version with seamless update integration.
- **Modern UI**: Built entirely with Jetpack Compose and Material 3.

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
4. Build and run the `:app` module.

## 📄 License

This project is for personal use and development demonstration.
