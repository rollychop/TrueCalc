# Releasing

TrueCalc has two distribution flavors:

| Flavor | Package | App name | Signed by | Updates |
|---|---|---|---|---|
| `play` | `com.brohit.truecalc` | True Calc | Your Play upload key (not in this repo) | Play in-app updates |
| `github` | `com.brohit.truecalc.github` | True Calc GH | `keystore/truecalc-github.jks` (public) or a secret keystore | "Check for updates" opens GitHub Releases |

The two packages differ, so both builds install side by side and the GitHub build can never
replace or update the Play app.

Gradle task names now include the flavor: `assemblePlayDebug`, `assembleGithubRelease`,
`testPlayDebugUnitTest`, `bundlePlayRelease`, and so on. In Android Studio, pick the variant
from **Build Variants**.

## GitHub release (automated)

```sh
git tag v1.1.0
git push origin v1.1.0
```
`.github/workflows/release.yml` then:
1. runs the unit tests
2. builds `assembleGithubRelease` with `versionName = 1.1.0` and `versionCode = 1000 + run number`
3. publishes a GitHub Release with `TrueCalc-1.1.0.apk`, `SHA256SUMS.txt` and auto-generated notes

`.github/workflows/ci.yml` runs the tests and builds the GitHub APK on every push to `master` and
on every PR. The APK is uploaded as a workflow artifact.

## Signing: current setup and the better option

**Now:** the GitHub build is signed with `keystore/truecalc-github.jks`, which is committed along
with its passwords (`keystore/github-keystore.properties`). That makes builds reproducible with
zero setup, but **anyone can sign an APK as "True Calc GH"**. Someone could publish a malicious
APK that Android accepts as an update to a sideloaded GitHub build. The Play app is not affected,
because it has a different package and key.

**Better, and the workflow already supports it:** use a private keystore stored in repository
secrets. No code changes are needed.
```sh
keytool -genkeypair -keystore private-github.jks -storetype PKCS12 -alias truecalc \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 private-github.jks   # paste into the secret below
```
Under **Settings → Secrets and variables → Actions**, add `SIGNING_KEYSTORE_BASE64`,
`SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD`. Once those are set,
release builds use the private key automatically. Then delete `keystore/` from the repo.

Switch **before** many people install the GitHub build. Android refuses an update signed with a
different key, so anyone on the public-key build has to uninstall and reinstall once.

Never use either keystore for the Play build. Keep the Play upload key outside the repo, or use
Play App Signing.

## Firebase for the GitHub flavor

`app/src/github/google-services.json` is currently a copy of the Play config with the package
renamed, so the build works. It reuses the Play app's Firebase app ID, so analytics and FCM from
the GitHub build are attributed to the Play app or may be rejected. To fix this, register an
Android app with package `com.brohit.truecalc.github` in the Firebase console, download its
`google-services.json`, and replace that file.
