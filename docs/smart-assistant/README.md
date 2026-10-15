# Smart Assistant: natural-language tool calling, on device

> Status: **planned, Phase 0 not started yet.** The research was done in a cloud session (Oct 2026)
> with no Android SDK/NDK, so nothing here has been run on a device yet. Phase 0 runs on a
> desktop and Phases 1+ need Android Studio and the NDK.

## Goal

The user types (and later says) something like *"EMI for 25 lakh at 8.5% for 20 years"*. TrueCalc
works out which calculator and inputs they mean, runs **our existing calculation code** and shows
the answer, with a button to open the full calculator already filled in.

Constraints:
- **Fully offline.** No API calls, no server, no API key at runtime.
- **Numbers always come from our Kotlin math**, never from a language model's text. The model only
  picks the tool and fills in the arguments.

## Model choice: Cactus Needle 3

| | |
|---|---|
| What it is | A tiny model that turns a query into tool calls (`function_calls`) with arguments. It does no free-text chat, so it can't make up an answer. |
| Size | One `.cact` file, **8–29 MB**. It is "laddered": you can export 2 to 20 layers of the same weights to trade size for accuracy. |
| License | Apache-2.0 |
| Output | `function_calls`, `confidence` (0–1, calibrated), `reasoning`, `suppressed_calls` |
| Guarantees | A byte-level grammar built from our JSON schemas, so arguments always match the schema. An off-topic query returns an **empty list**, not a guess. |
| Many tools | With more than 5 tools, a retrieval head keeps the top 5 for each turn (we have 8) |
| Android | The engine ships for `android-arm64`, `android-armv7` and `android-riscv64` as a **static library and a header**, not an AAR |
| Speech, later | **Whistle** (16.9 MB) runs speech-to-text on the same engine. A voice clip goes in and tool calls come out, in one call. |

Older versions: Needle 1 (26M params, distilled from Gemini) and Needle 2 (~14 MB, ~28 MB RAM).
Use `generation=2` only if Needle 3 has problems on Android.

**Fallbacks if Needle fails Phase 0:** FunctionGemma-270M (Google, runs through LiteRT/MediaPipe,
about 10× bigger, easier to set up on Android), or the `com.cactuscompute:cactus` Kotlin SDK on
Maven (a general LLM engine, probably limited to the original Needle).

### Known caveats (from Needle's own docs)
- The **base** model fails 5 of its 6 bundled test suites at the default gate, mostly by missing calls
  or inventing values. **Plan to fine-tune it on our tools** (Phase 2).
- The engine flags numbers that don't appear in the text. "25 lakh" has to reach the engine as
  `2500000`, so we **rewrite the query before inference** (see `normalize()` in `spike.py`).
- No documented Hindi or Hinglish support. Start with English only.
- **Telemetry is on by default** in the engine binary. Set `NEEDLE_TELEMETRY=0` and
  `DO_NOT_TRACK=1` before loading it.
- Local LoRA fine-tuning drops the confidence head (`confidence` becomes `None`). Platform
  fine-tuning (paid) keeps it.

## Architecture

```
 user text ──► QueryNormalizer ──► NeedleEngine (JNI → libneedle.a) ──► ToolCall(name, args, confidence)
                (lakh/crore,          tools.json loaded once,                │
                 ft/in, ₹, commas)    runs on Dispatchers.Default            ▼
                                                                  ToolDispatcher
                                                     ┌────────────────┼────────────────┐
                                             conf ≥ 0.7         conf < 0.7 / held     empty
                                             run it              "Did you mean…?"     "Try: …" + tool list
                                                     ▼
                                    existing calc logic (domain/) → ResultCard
                                                     └── "Open in calculator" → Route.Calculator.X(restorationJson)
```

Planned files, following the project layout in `AGENTS.md`:

| Layer | File | Notes |
|---|---|---|
| native | `app/src/main/cpp/CMakeLists.txt`, `needle_jni.cpp` | Wraps `libneedle.a` |
| native libs | `app/src/main/cpp/needle/<abi>/libneedle.a`, `needle.h` | From `needle download android-arm64` |
| model | `app/src/main/assets/needle/needle3.cact` *(or downloaded on first use)* | 8–29 MB |
| model | `app/src/main/assets/needle/tools.json` | Copy of `docs/smart-assistant/tools.json` |
| data | `data/assistant/NeedleEngine.kt` | Kotlin/JNI bridge, a single instance |
| domain | `domain/assistant/AssistantRepository.kt`, `model/ToolCall.kt`, `model/AssistantResult.kt` | |
| core | `core/QueryNormalizer.kt` | Port of `normalize()` in `spike.py`, with unit tests |
| domain | `domain/assistant/ToolDispatcher.kt` | Tool name → calculation + `Route` with `restorationJson` |
| di | `di/AssistantModule.kt` | Hilt `@Singleton` engine |
| ui | `ui/screens/assistant/AssistantScreen.kt`, `AssistantViewModel.kt` | Or a mode inside `SearchScreen` |

**Pulling the math out first:** the formulas live inside the ViewModels today (for example
`EmiCalculatorViewModel.calculateEmi`). Move them into pure functions under `domain/` or
`core/MathUtils.kt` so both the screens and the assistant call the same code. This also gives us
unit tests for the math.

### Tool → screen restoration mapping

Use the keys the ViewModels already read from `restorationJson`:

| Tool | Route | restorationJson keys |
|---|---|---|
| `calculate_emi` | `Route.Calculator.Emi` | `principal`, `rate`, `term`, `isTermInYears` |
| `calculate_compound_interest` | `CompoundInterest(COMPOUND)` | `principal`, `rate`, `time`, `isTimeInYears`, `frequency` |
| `calculate_simple_interest` | `CompoundInterest(SIMPLE)` | `principal`, `rate`, `time`, `isTimeInYears` |
| `calculate_fixed_deposit` | `CompoundInterest(FIXED)` | `principal`, `rate`, `time`, `isTimeInYears`, `frequency` |
| `calculate_recurring_deposit` | `RecurringDeposit` | `principal`, `rate`, `time`, `isTimeInYears`, `interval`, `frequency` |
| `calculate_bmi` | `Bmi` | `weight`, `height` |
| `calculate_daily_calories` | `Calorie` | `age`, `weight`, `height`, `gender`, `activity` |
| `calculate_ideal_weight` | `IdealWeight` | `height`, `gender` |
| `calculate_percentage` | `Percentage` | `mode`, `first` (= `x`), `second` (= `y`), `direction` |
| `calculate_discount` | `Discount` | `price`, `discount`, `secondDiscount` (= `extra_discount_percent`), `flatOff` |
| `calculate_gst` | `Gst` | `amount`, `rate`, `mode` |
| `split_bill` | `SplitBill` | `bill`, `people`, `tip` (= `tip_percent`), `roundUp` (`"true"`/`"false"`) |
| `convert_units` | `UnitConverter` | `category`, `from`, `to`, `value` |
| `calculate_age` | `Age` | `dob`, `asOf` (ISO `YYYY-MM-DD`) |
| `calculate_dates` | `DateCalc` | `mode`, `start`, `end`, `includeEnd`, `amount`, `unit`, `subtract` |

The basic calculator (`Route.Calculator.Basic`, keys `expression`, `scientific`, `angle`) has no
tool: free-form arithmetic is better typed than parsed.

Enum values in `tools.json` match the app's own values (`CompoundingFrequency`, `RecurringInterval`,
`"Male"/"Female"`, the calorie activity labels, and the everyday tools' `mode`/`unit`/category
keys and unit ids from `domain/calc`), so they can be passed through without conversion.

---

## Roadmap

### Phase 0: desktop trial (about 1 day, no Android needed)
Files in this folder:
- `tools.json`: the 15 tools, in Needle/OpenAI function format
- `test_queries.jsonl`: 55 queries (50 tool calls including 2 multi-call, 5 off-topic refusals)
- `spike.py`: runs the model, scores the calls, prints accuracy, confidence buckets and latency

```sh
cd docs/smart-assistant
python3 -m venv .venv && source .venv/bin/activate
pip install cactus-needle
python spike.py            # base Needle 3, with normalization
python spike.py -v         # show every query
python spike.py --no-normalize   # how much the normalizer helps
```

- [ ] Run the base model and record the numbers below
- [ ] Add 20+ queries phrased the way real users type (typos, Hinglish words, "pm", "p.a.")
- [ ] Try smaller rungs: `needle build --layers 8 --out needle3-8L.cact`, then `python spike.py --weights needle3-8L.cact`

**Go / no-go:** accuracy ≥ 85% **and** zero wrong calls auto-run at confidence ≥ 0.7. If the base
model misses, go to Phase 2 (fine-tune) before Android work. If tuned models still miss, switch to
the FunctionGemma fallback.

| Date | Model | Normalized | Accuracy | Auto-run wrong | Refusals | p50 ms |
|---|---|---|---|---|---|---|
| | needle3 (20L) | yes | | | | |

### Phase 1: Android integration (2–3 days)
1. **Get the engine.** On the desktop: `needle download android-arm64 --out third_party/needle`
   (and `android-armv7` if needed). Check what the folder contains (`libneedle.a`, header, CLI).
   Put the `.a` and the header under `app/src/main/cpp/needle/`.
2. **Smoke test before writing code:** `adb push` the `needle` CLI from that folder together with
   `needle3.cact` and `tools.json` to `/data/local/tmp`, then run
   `./needle --model needle3.cact --tools tools.json` with a query. This confirms the engine works
   on the phone and gives real latency.
3. **NDK and CMake.** Install NDK and CMake from the SDK Manager. In `app/build.gradle.kts`:
   ```kotlin
   android {
       defaultConfig {
           ndk { abiFilters += listOf("arm64-v8a") }   // add "armeabi-v7a" if we ship 32-bit
       }
       externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
   }
   ```
   `CMakeLists.txt`:
   ```cmake
   cmake_minimum_required(VERSION 3.22)
   project(truecalc_needle CXX)
   add_library(needle STATIC IMPORTED)
   set_target_properties(needle PROPERTIES IMPORTED_LOCATION
       ${CMAKE_SOURCE_DIR}/needle/${ANDROID_ABI}/libneedle.a)
   add_library(needle_jni SHARED needle_jni.cpp)
   target_include_directories(needle_jni PRIVATE ${CMAKE_SOURCE_DIR}/needle/include)
   # 16 KB page alignment, required for Play on Android 15+ devices
   target_link_options(needle_jni PRIVATE "-Wl,-z,max-page-size=16384")
   target_link_libraries(needle_jni needle log m)
   ```
   (If the static library depends on `c++_shared` or OpenMP, add them here. Check its symbols with
   `nm`.)
4. **The JNI bridge.** The C API, as the Python package calls it:
   ```c
   int  needle_load(const char* data, uint64_t len);           // Needle 3: load .cact bytes
   int  needle_init(const char* tools_json, const char* system, const char* /*see header*/);
   int  needle_complete(const char* text, const float* pcm, int pcm_len,
                        int max_new_tokens, char* out, int out_len);  // out = JSON, rc < 0 on error
   int  needle_embed(const char* text, const float* pcm, int pcm_len, float* out, int out_len);
   void needle_reset(void);
   ```
   **Check the third `needle_init` argument and the call order against the shipped `needle.h`**.
   The list above comes from the Python package's ctypes bindings.
   ```cpp
   // needle_jni.cpp (sketch)
   extern "C" JNIEXPORT jint JNICALL
   Java_com_brohit_truecalc_data_assistant_NeedleEngine_nativeLoad(JNIEnv* env, jobject, jbyteArray weights) { ... }
   extern "C" JNIEXPORT jint JNICALL
   Java_com_brohit_truecalc_data_assistant_NeedleEngine_nativeInit(JNIEnv* env, jobject, jstring tools, jstring system) { ... }
   extern "C" JNIEXPORT jstring JNICALL
   Java_com_brohit_truecalc_data_assistant_NeedleEngine_nativeComplete(JNIEnv* env, jobject, jstring text, jint maxTokens) {
       // 64 KB buffer, as the Python package uses; call needle_reset() first (stateless queries)
   }
   ```
   ```kotlin
   @Singleton
   class NeedleEngine @Inject constructor(@ApplicationContext private val context: Context) {
       private val mutex = Mutex()          // the engine is a process-wide singleton, not thread-safe
       suspend fun complete(text: String): NeedleResponse = mutex.withLock {
           withContext(Dispatchers.Default) { ensureLoaded(); parse(nativeComplete(text, 512)) }
       }
       private external fun nativeLoad(weights: ByteArray): Int
       private external fun nativeInit(tools: String, system: String): Int
       private external fun nativeComplete(text: String, maxTokens: Int): String
       companion object { init { System.loadLibrary("needle_jni") } }
   }
   ```
   - Call `Os.setenv("NEEDLE_TELEMETRY", "0", true)` and `Os.setenv("DO_NOT_TRACK", "1", true)`
     **before** `System.loadLibrary`.
   - Load the model lazily, the first time the assistant opens, not in `TrueCalcApp.onCreate`.
   - Pass `system = "date: <today>; locale: en-IN; device: phone"`.
   - **ProGuard:** release builds use `isMinifyEnabled = true`, so keep native methods:
     `-keepclasseswithmembernames class * { native <methods>; }`.
   - The engine cannot unload weights. Load one `.cact` per process.
5. **Port `normalize()` to `core/QueryNormalizer.kt`**, with JUnit tests that use the same inputs as
   `spike.py`.
6. **Instrumented test:** run `test_queries.jsonl` on a device (`androidTest`) and log accuracy and
   p50/p95 latency for the table below.

| Device | ABI | Model | Accuracy | p50 ms | p95 ms | RAM MB | APK +MB |
|---|---|---|---|---|---|---|---|
| | | | | | | | |

### Phase 2: fine-tune on TrueCalc tools (when the base model misses the bar)
- Grow `test_queries.jsonl` into training data. Aim for 300–1,000 examples, including off-topic
  `answers: []` examples.
- **Local (free):** `pip install "cactus-needle[train]"`, then
  `needle finetune data.jsonl --epochs 10 --out adapter.safetensors`, then
  `needle build --lora adapter.safetensors --layers 8 --out truecalc.cact`. Downside: no confidence
  score, so the app must always confirm, or use `validation`/grounding signals instead.
- **Cactus Platform (paid):** `needle platform generate --tools tools.json --examples 1000`, then
  `needle platform finetune …`. Keeps the calibrated confidence and scores every depth, so we can
  pick the smallest model that passes.
- Re-run `spike.py --weights truecalc.cact` and the on-device test, and record the results above.

### Phase 3: product UI
- [ ] Entry point: a "sparkle" mode in `PersistentSearchBar` / `SearchScreen` ("Ask TrueCalc…").
      Fall back to normal search when the model returns no call.
- [ ] `ResultCard`: tool name, the inputs it understood (as editable chips), key results, and an
      **Open in calculator** button that sends `restorationJson`.
- [ ] Confirm step below the threshold: "Did you mean EMI for ₹25,00,000 at 8.5% for 20 years?"
- [ ] Log each run through `HistoryManager.log(...)` so assistant results show up in History.
- [ ] Analytics: count of calls, refusals and confirms, never the query text.
- [ ] Settings toggle, and decide between the model inside the APK and an on-demand download
      (Play Asset Delivery, or a first-run download from the GitHub release). The app is offline
      either way once the model is there.
- [ ] Update `tools.json` whenever a calculator is added (Unit Conversion and Math categories later).

### Phase 4: voice (optional)
- [ ] Add `whistle.cact` (16.9 MB) and record 16 kHz mono PCM, then pass it to `needle_complete`
      through `pcm` and `pcm_len`.
- [ ] `RECORD_AUDIO` permission and a mic button in the assistant bar.

---

## Open questions to settle in Phase 0/1
1. Exact contents of the `android-arm64` folder: header name, extra libraries it needs, whether a
   shared `.so` is also published.
2. Real latency and RAM on a mid-range phone (the targets for the 8-layer and 20-layer models).
3. Does the engine's grounding check run in C++ (`validation.ungrounded` in the JSON) or only in the
   Python wrapper? If only in Python, we add the same check in Kotlin: every number argument must
   appear in the normalized text.
4. Is `exclusiveMinimum` / `default` in our schemas respected by the grammar, or should defaults be
   applied in Kotlin?
5. Telemetry: confirm that the environment variables turn it off in the Android build.

## Sources
- Needle repo and `llms.txt`: <https://github.com/cactus-compute/needle>
- `cactus-needle` on PyPI (3.2.2 was checked; its `agent/fetch.py` lists the Android platforms and
  says Android ships a static lib): <https://pypi.org/project/cactus-needle/>
- Cactus engine: <https://github.com/cactus-compute/cactus>
- Needle blog and guides (designing tools, confidence, fine-tuning, supported devices, porting):
  <https://cactuscompute.com/blog/needle>, <https://cactuscompute.com/needle>
- Raspberry Pi write-up on Needle 2:
  <https://www.raspberrypi.com/news/turn-text-input-into-actions-with-needle-a-14mb-function-calling-llm/>
