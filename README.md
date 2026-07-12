# La Bella Bakery — Customer App

A Kotlin Multiplatform (KMP) customer ordering app for a single bakery: browse items, build a
cart, check out, pay, and receive a pickup token. Built with **Compose Multiplatform** UI
shared across Android and iOS, following the architecture in
[`.specify/memory/architecture.md`](.specify/memory/architecture.md).

> **Current status:** early scaffold. The app boots to an auth gate → a placeholder
> "Continue with Google" login → a Home screen backed by **mock data** (`FakeCatalogDataSource`).
> Real Google Sign-In, the live server, and Razorpay are wired later (see the build plan).

## Platform support

| Platform | Status | Notes |
|----------|--------|-------|
| **Android** | ✅ Runnable | `composeApp` Android application |
| **iOS** | ✅ Runnable | `iosApp` Xcode project wrapping the shared `ComposeApp` framework |
| **Web** | ❌ Not a target yet | No `wasmJs`/JS target configured; the architecture currently scopes this app to Android + iOS. See [Web](#web-not-yet-supported). |

## Prerequisites

- **JDK 17** (project builds with Temurin/Corretto 17)
- **Android**: Android SDK (`compileSdk 36`, `minSdk 24`); Android Studio or the command-line SDK. Set `sdk.dir` in `local.properties` (git-ignored) or `ANDROID_HOME`.
- **iOS** *(building & running the iOS app requires a Mac — Xcode is macOS-only)*: **Xcode 16+** with an iOS Simulator, and **[XcodeGen](https://github.com/yonaskolb/XcodeGen)** if you need to regenerate the Xcode project (`brew install xcodegen`). The generated `iosApp/iosApp.xcodeproj` is committed, so XcodeGen is only needed when changing `iosApp/project.yml`. On Windows/Linux you can still edit shared code and build/run Android — only the iOS target is Mac-restricted.

The Gradle wrapper (`./gradlew`) pins the Gradle version — no local Gradle install needed.

## Running on Android

**From the command line** (device or emulator connected):

```sh
./gradlew :composeApp:installDebug        # build + install the debug APK
# or just build the APK without installing:
./gradlew :composeApp:assembleDebug       # -> composeApp/build/outputs/apk/debug/
```

**From Android Studio:** open the project root, let Gradle sync, then run the `composeApp`
run configuration on an emulator or device.

## Running on iOS

macOS + Xcode required. From the repo root:

```sh
# 1. (only if you changed iosApp/project.yml) regenerate the Xcode project:
cd iosApp && xcodegen generate && cd ..

# 2. Open in Xcode and press Run (selects the ComposeApp framework build automatically):
open iosApp/iosApp.xcodeproj
```

The Xcode project has a pre-build phase that runs
`./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` to compile and embed the shared
Kotlin framework, so a plain **Run** in Xcode builds everything.

**Headless build/run from the command line:**

```sh
# Build for a simulator
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.4' build

# Install + launch on a booted simulator
xcrun simctl install booted \
  ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator/Bakery.app
xcrun simctl launch booted com.bakery.customer.app
```

### Web (not yet supported)

There is no web target today. Adding one means declaring a Compose Multiplatform `wasmJs`
browser target in `composeApp/build.gradle.kts` (with a `wasmJsMain` entry point and an
`index.html`), then `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`. This also expands the
project beyond its current Android + iOS scope, so it should be added deliberately (and
reflected in `architecture.md`) rather than assumed. Ask before relying on it.

## Project structure

```
composeApp/          Android + iOS entry points, DI wiring, NavHost, theme
core/                common, network (Ktor), database (Room + DataStore), designsystem, ui
feature/             auth, home, search, catalog, cart, checkout  (each: domain / data / presentation)
iosApp/              Xcode project wrapping the shared ComposeApp framework
gradle/libs.versions.toml   version catalog (single source of dependency versions)
```

See [`.specify/memory/architecture.md`](.specify/memory/architecture.md) for layer
responsibilities, dependency flow, and the rules for adding features.

## Contributing

- **Conventional Commits are enforced.** Install the local hook once per clone:
  `./gradlew installGitHooks`. Messages must look like `feat(cart): add quantity stepper`;
  a `commit-lint` CI check re-validates every PR.
- **Branch model:** `main` (production) ← `develop` (integration) ← feature branches. Every
  change reaches `develop`/`main` via a pull request (a template pre-fills the description).
- See [CLAUDE.md](CLAUDE.md) for the full Git/push and architecture policies.
