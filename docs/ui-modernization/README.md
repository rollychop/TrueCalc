# UI Modernization & Tool Expansion Plan

> Status: **Phases 1–3 implemented** (Oct 2026): design system, shared tool components, all
> existing tools migrated, 3-tab navigation with the new Home, and the everyday tools pack.
> **Phase 4 (finance pack) is deferred.** Nothing has been built on Android yet: the work was
> done in a cloud container without the SDK, with the pure-Kotlin calc layer (138 unit tests)
> checked on the JVM and the Compose UI compile-checked against Compose Desktop with stubs.
> First step on a dev machine: `./gradlew :app:assembleDebug :app:testDebugUnitTest`, then a
> visual pass in light, dark and 200% font scale.
> Goal: **feature-rich, minimalist, consistent.** Every tool should look and behave the same, so
> adding tool #40 is as cheap as adding tool #9.

## Design principles
1. **One screen pattern for every tool**: inputs → live result → details. There is no "Calculate"
   button; results update as you type (this already works).
2. **Progressive disclosure**: show the 2–3 inputs most people need, and keep advanced options
   (fees, compounding, step-up) in a collapsed "More options" section.
3. **The result is the hero**: one big number with tabular figures, and a breakdown underneath.
   Everything else stays quiet.
4. **Colour carries meaning, not decoration**: neutral surfaces, one brand accent, and category tint
   used only on icons.
5. **Theme tokens only**: no hex colours, ad-hoc dp values or one-off shapes in screens.
6. **Offline first**: every tool works with no network.

---

## 1. Audit: what is inconsistent today

| # | Problem | Where | Fix |
|---|---|---|---|
| 1 | **Three different screen styles.** Health tools use plain `Column` + `.background(surface)` blocks and print results as raw text (`"BMI Value: …"`). Recurring Deposit uses `Card` with `surfaceVariant.copy(alpha = 0.3f)`, a description line and a hero result. EMI and CI use bordered `Column`s with a `headlineSmall` "Results"/"Calculations" title. | `health/*Screen.kt`, `RecurringDepositScreen.kt`, `EmiCalucatorScreen.kt`, `CompoundInterestScreen.kt` | A shared `ToolScaffold` with `InputSection`, `ResultHero` and `BreakdownSection` (§3) |
| 2 | **No real dark theme.** `DarkColorScheme` only sets `primary` and keeps template `PurpleGrey80`/`Pink80`. Surfaces, containers and outline come from the M3 default purple baseline, so dark mode looks off-brand. | `ui/theme/Theme.kt`, `Color.kt` | A full light and dark scheme generated from one seed (§2). Delete the `Purple*`/`Pink*` template colours. |
| 3 | **Category colours are hardcoded hex and chosen by name.** `getToolColors(name)` uses `name.contains("EMI")` and raw `Color(0xFF60A5FA)`, and decides dark mode from luminance. Renaming a tool changes its colour. | `ui/utils/ToolUtils.kt` | The category enum carries an accent role, mapped to scheme containers (`primaryContainer`, `tertiaryContainer`, …) |
| 4 | **Decimal inputs use `KeyboardType.Number`**, so on some keyboards you can't type "8.5". | All calculator screens | `KeyboardType.Decimal` for rates, weights and heights, built into a `NumberField` |
| 5 | The top-bar History action, with its `Json.encodeToString(Route…)`, is copy-pasted into **every** screen. There's no favourite, share or reset on tool screens. | All 6 calculator screens | A `ToolScaffold` top bar with History, Favourite, Share and Reset |
| 6 | Mixed spacing (`spacedBy(12.dp)` vs `16.dp`, padding 8/16/24) and corner radii (8/16/20/28 dp, plus `shapes.medium` vs `shapes.large`) | screens and components | `Spacing` tokens and an explicit `Shapes` in the theme (§2) |
| 7 | Typography is half-defined. Screens override `fontWeight = FontWeight.Bold` on app bar titles, and numbers aren't tabular, so results jitter while typing. | `Type.kt`, screens | Full type scale, a `numberStyle` with `tnum`, and no per-screen weight overrides |
| 8 | Roboto is loaded through the Google Fonts **provider**, which needs Play Services and the network on first run. Roboto is already the system font. | `Type.kt` | Use the system default (or bundle one variable font), and drop the provider dependency |
| 9 | Calculation logic lives inside ViewModels (`calculateEmi`, …), so it can't be reused (assistant, widgets) or unit-tested | `*ViewModel.kt` | Pure functions in `domain/calc/` with tests (§4) |
| 10 | The tool registry has three sources of truth: `calculatorsWithCategories`, a dead `calculatorsWithCategories1`, and per-category strings in `getCategorySummary`/`getCategoryIcon` | `CalculatorWithCategory.kt`, `ToolUtils.kt` | One `ToolRegistry` (§4) |
| 11 | Five bottom tabs (Home, Categories, Favorites, History, Settings). Categories and Favorites duplicate Home. | `MainAppScreen.kt` | Three tabs: **Home** (search, pinned, recent, all tools), **History**, **Settings** |
| 12 | Leftovers: `PlaceholderScreens.kt`, `AppState` global state, a misspelled `EmiCalucatorScreen.kt` / `compund_interest` package | various | Clean up during the migration |

---

## 2. Design system (Phase 1)

All in `ui/theme/`. The `design-system` skill from the **Design** plugin can drive this step.

**Colour**
- Seed `#2563EB` (current `PrimaryBlue`). Generate the full M3 light and dark schemes, including
  `surfaceContainer*` roles, with Material Theme Builder, and paste them into `Color.kt`.
- Keep dynamic colour as an option (it's already in Settings), but make the brand scheme the
  default.
- Category accents come from `ToolCategory.accent` and map to scheme roles: Finance → primary,
  Health → tertiary, Everyday → secondary, Convert / Math → a custom extended colour (pair it with
  `MaterialTheme` through a `CompositionLocal`).

**Tokens**
```kotlin
object Spacing { val xs = 4.dp; val s = 8.dp; val m = 12.dp; val l = 16.dp; val xl = 24.dp; val xxl = 32.dp }
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(32.dp)
)
```
**Type**: the full M3 scale, plus
`val NumberDisplay = displaySmall.copy(fontFeatureSettings = "tnum", fontWeight = SemiBold)`.

**Surfaces**: page background is `surface`, sections are `surfaceContainerLow` with `shapes.large`,
and there are no borders or alpha hacks.

**Motion**: a subtle `animateContentSize` on results, a counting animation on the hero number
(≤300 ms), and standard M3 nav transitions. Respect "remove animations".

**Accessibility**: 48 dp touch targets, contrast checked in both themes, `contentDescription` on
icon buttons, and text up to 200% font scale without clipping (the result hero wraps or
auto-shrinks).

## 3. Shared tool components (Phase 1)

`ui/components/tool/`:

| Component | Responsibility |
|---|---|
| `ToolScaffold(tool, onReset, shareText, content)` | `LargeTopAppBar` that collapses on scroll, with actions ★ Favourite · History · ⋯ (Share, Reset). Keyboard-aware scroll (`imePadding`), consistent page padding. |
| `InputSection { }` | `surfaceContainerLow` card with spacing `l` |
| `NumberField(state, label, suffix/prefix, decimal = true, currency = false)` | Replaces `CustomTextField` for numbers: decimal keyboard, ₹ prefix with lakh grouping, unit suffix (%, yrs, kg), inline error, clear button |
| `UnitToggle` | Segmented Years/Months, kg/lb, cm/ft |
| `OptionPicker` | Replaces `ExpandableSelector` / `DropdownMenu`: a chip row for ≤4 options, a bottom sheet for more |
| `MoreOptions { }` | Collapsible advanced inputs |
| `ResultHero(label, value, caption)` | The big number, empty state ("Enter amount to see result"), copy on long-press |
| `BreakdownSection(rows, chart?)` | `ResultRow` list plus an optional donut or bar chart |
| `ToolInfo` | A collapsible "How it's calculated" with the formula, for trust |

**Exit criteria for Phase 1:** all 6 current tools migrated, every screen built only from these
components, and no `Color(0x…)` or raw dp outside `ui/theme`.

## 4. Architecture for scaling tools (Phase 1–2)

```
domain/calc/            pure Kotlin, no Android: EmiCalc.kt, InterestCalc.kt, SipCalc.kt, GstCalc.kt, UnitConvert.kt …
                        + unit tests for each in app/src/test (and use BigDecimal for money where rounding matters)
ui/tools/ToolRegistry   data class ToolSpec(id, name, category: ToolCategory, keywords, icon, route, isNew)
                        single list feeding Home, Search, Favorites, History labels, assistant tools.json
ui/screens/tools/<tool>/ Screen + ViewModel per tool, built on ToolScaffold
```
- `ToolSpec.id` (a stable string) replaces matching by route class, which also fixes the
  favourites lookup for good.
- Search uses `keywords` ("loan", "home loan", "mortgage" → EMI).
- Adding a tool takes three files (calc, screen, VM), one registry line, one nav entry and tests.

## 5. New tools: everyday-useful, prioritised

Picked for daily use in India, offline, minimal inputs. ★ = highest value.

**Everyday**
| Tool | Why | Effort |
|---|---|---|
| ★ **Basic / Scientific calculator** | A calculator app without a calculator. Tape history and memory. | M |
| ★ **Percentage** (X% of Y, % change, X is what % of Y) | The most-searched calculation | S |
| ★ **Discount** (price, % off → final, saved; stacked discounts) | Shopping | S |
| ★ **GST** (add or remove, 5/12/18/28%, CGST/SGST split) | Business owners, invoices | S |
| ★ **Split bill / Tip** | Friends and restaurants | S |
| **Age calculator** (exact age, next birthday) | Forms, eligibility | S |
| **Date calculator** (days between, add/subtract days, working days) | Notice periods, due dates | S |
| **Fuel cost & mileage** | Commute and trip planning | S |
| **Number to words** (Indian lakh/crore + international) | Cheques, invoices | S |
| **Unit price compare** (₹/kg of pack A vs B) | Grocery | S |

**Finance**
| Tool | Why | Effort |
|---|---|---|
| ★ **SIP / Lumpsum / Step-up SIP** | Already promised in the README ("Mutual Fund planners") | M |
| ★ **Loan prepayment & compare** (tenure or EMI reduction, two loans side by side) | Builds on EMI | M |
| **PPF / Sukanya / NSC** | Fixed government schemes, stable formulas | M |
| **CAGR & absolute return** | Investments | S |
| **Inflation / future value** | Planning | S |
| **Retirement corpus & SWP** | Planning | M |
| **Income tax (new vs old regime)** | High value, but **slabs change every year**, so the rules need versioning per FY | L |

**Health**
| Tool | Why |
|---|---|
| **Body fat % (US Navy)**, **Water intake**, **BMR/TDEE** (merge into Calorie), **Due date** | Small additions to the existing Health set |

**Convert**
| Tool | Why |
|---|---|
| ★ **Unit converter**: length, weight, temperature, area (incl. sq ft, gaj, bigha), volume, speed, data, time. One screen with a category picker. | Daily use |
| **Currency** | Needs rates, so offline-first: the user enters a rate or it caches the last rate. Optional fetch later. |

**Math**: Ratio / proportion, Average, GCD/LCM, Fraction ↔ decimal, Roman numerals.

### Home without clutter as tools grow
- Top: search ("Search 40+ tools…", plus the assistant later).
- **Pinned** (favourites, reorderable) and **Recent** as horizontal chip rows.
- **All tools**: category sections with compact 2-column tiles, and a "New" badge.
- Category detail is reached from the section header. There's no separate Categories tab.

---

## 6. Roadmap

| Phase | Scope | Done when |
|---|---|---|
| **1. Foundation** (1–2 wk) | Theme tokens and full schemes · shared tool components · `domain/calc` + tests · `ToolRegistry` · migrate the 6 existing tools · `KeyboardType.Decimal` | ✅ Done. Audit items 1–10 closed; screenshots still to check on a device |
| **2. Navigation & Home** (3–4 days) | 3-tab nav · new Home (pinned, recent, sections) · search by keywords · clean-up (item 12) | ✅ Done |
| **3. Everyday pack** (1–2 wk) | Basic calculator, Percentage, Discount, GST, Split bill, Unit converter, Age, Date | ✅ Done. GST uses the GST 2.0 slabs (0/0.25/3/5/18/40) |
| **4. Finance pack** (1–2 wk) | SIP/Lumpsum/Step-up, Loan prepayment/compare, CAGR, Inflation, PPF | ⏸ Deferred |
| **5. Polish** | Share result as an image/text card, home-screen widgets (Glance) for the calculator and a pinned tool, a11y pass, baseline profiles | |
| **6. Smart assistant** | See [`../smart-assistant/`](../smart-assistant/README.md). Every new tool adds an entry to `tools.json`. | |

**Testing each phase** (on the developer machine, since the cloud container has no Android SDK):
`./gradlew :app:testDebugUnitTest` for calc tests, plus Compose previews in light, dark and 200%
font scale for every shared component. Consider screenshot tests (Roborazzi or Paparazzi) once the
components settle.

## Skills to use
- **Design** plugin (Anthropic): `design-system` for the tokens, `design-critique` on screenshots
  after each phase, `accessibility-review` before release, `ux-copy` for labels and empty states.
- **frontend-design** plugin: general UI craft guidance.
- Optional on the dev machine: **Android Emulator QA** or **Oh My Android** plugins for screenshot,
  dark mode and font-scale checks.
