# AI Agent Guidelines - True Calc

Welcome, AI Agent! This document provides the necessary context and rules to help you work efficiently on the **True Calc** project.

## 🎯 Project Scope
True Calc is a high-performance, professional calculator and financial utility app for Android. It focuses on accuracy, user experience, and a modern UI.

**Core Features:**
- **Financial Calculators**: EMI, Compound Interest, Mutual Funds, Recurring Deposits.
- **Health Utilities**: BMI, etc.
- **History & Favorites**: Persistent storage for calculations.
- **Search**: Deep search for specific tools.
- **Analytics & Updates**: Firebase integration and In-app updates.

## 🏗️ Core Architecture
The project follows **MVVM (Model-View-ViewModel)** with **Clean Architecture** principles.

- **`data/`**: Data sources (Room `dao`, Retrofit `GitHubApi`) and Repository implementations.
- **`domain/`**: Pure Kotlin logic. Interfaces for repositories, business models, and use cases (if applicable).
- **`ui/`**: Jetpack Compose code. 
    - `screens/`: Feature-specific UI.
    - `components/`: Reusable UI widgets (e.g., `ResultRow`); tool screens use `components/tool/` (`ToolScreen`, `NumberField`, `ResultHero`, …).
    - `navigation/`: Centralized routing in `AppNavGraph.kt`.
- **`di/`**: Dagger Hilt modules.
- **`core/`**: Critical math utilities (`MathUtils.kt`) and text processing.

## 🤖 Sub-Agent Orchestration (`task` tool)
To maintain context efficiency, delegate "noisy" or implementation-heavy tasks to sub-agents.

**When to Delegate:**
1. **New Calculator Implementation**: "Create a new GST calculator including the domain logic, ViewModel, and Compose screen."
2. **Database Expansion**: "Add a new table to Room for 'Tax History' and implement the corresponding DAO and Repository."
3. **Bulk UI Components**: "Create a set of 5 different chart components using the `compose-charts` library."
4. **Unit Testing**: "Write comprehensive unit tests for all functions in `MathUtils.kt`."

**Prompting Best Practices:**
- Provide the exact package path.
- Reference existing patterns (e.g., "Follow the pattern used in `EMIScreen.kt`").
- Explicitly state the expected files to be created/modified.

## 🛠️ Coding Standards
- **Language**: Kotlin 100%.
- **UI**: Jetpack Compose with Material 3.
- **Dependency Injection**: Dagger Hilt (`@AndroidEntryPoint`, `@HiltViewModel`).
- **Persistence**: Room Database.
- **Async**: Kotlin Coroutines & Flow.
- **Navigation**: Type-safe navigation where possible, following `Route` definitions.

## 💡 Tips for the Main Agent
- Keep the `MainActivity.kt` clean; it handles app-level logic like updates.
- Always check `Constant.kt` for shared values.
- Verify `MathUtils.kt` before implementing new calculation logic to avoid duplication.

## 🔮 Planned: Smart Assistant (on-device natural language)
Plan, model research (Cactus Needle 3) and a desktop trial kit live in [`docs/smart-assistant/`](docs/smart-assistant/README.md).
- `tools.json` is the tool contract; keep it in sync with calculators and their `restorationJson` keys.
- `spike.py`'s `normalize()` is the reference for the future `core/QueryNormalizer.kt`.
- The cloud container has no Android SDK/NDK: native/JNI work and on-device testing happen on the developer's machine.

## 🎨 Planned: UI modernization & tool expansion
Audit, design system, shared tool components and the prioritised tool list live in [`docs/ui-modernization/`](docs/ui-modernization/README.md).
New tool screens must use the shared `ToolScaffold` components and theme tokens once Phase 1 lands; no hardcoded colours or one-off spacing.
