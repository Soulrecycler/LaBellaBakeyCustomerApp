# Implementation Plan: App Icons & Splash Screen (La Bella)

**Branch**: `002-app-icons-splash` | **Date**: 2026-07-12 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/002-app-icons-splash/spec.md`

## Summary

Deliver branded launcher/app icons and a cold-start splash for the Bakery (La Bella) Compose Multiplatform app on **Android and iOS (universal iPhone+iPad)**, using **already-exported production PNG assets** found in the claude.ai design project via `/design-sync` (`assets/app-icons/`) — the **"Stacked with center flourish"** logo and the **Splash & Signup** layout. All brand marks ship as the **fetched raster (PNG) asset files** wired into the platform-native resource locations — Android adaptive mipmaps (foreground + background) with legacy PNG fallbacks, and an iOS `AppIcon.appiconset`. No inline SVG is committed to render these marks. Every fetched asset is referenced by the platform manifests/asset catalogs (no orphans).

## Technical Context

**Language/Version**: Kotlin 2.x (KMP), Swift (iosApp host), plus platform resource XML/plist
**Primary Dependencies**: Compose Multiplatform; Android Gradle (androidApplication); **androidx core-splashscreen** (NEW — backports the Android 12 splash API to minSdk 24); Xcode asset catalog for iOS
**Storage**: N/A (static image assets fetched from design-sync and committed to the repo)
**Testing**: Manual/visual verification on device+simulator (see quickstart.md); no unit tests — assets have no runtime logic
**Target Platform**: Android minSdk 24 / targetSdk 36; iOS **universal iPhone + iPad** (revised — use all exported sizes)
**Project Type**: Mobile app (Compose Multiplatform: `composeApp` + `iosApp` Xcode host)
**Performance Goals**: Splash visible within the first rendered frame (no blank/white flash); icons render crisp with no upscaling at any required density/size
**Constraints**: iOS app icon must be fully opaque (no alpha); Android artwork must respect the adaptive-icon safe zone (already composed in the export); single fixed-brand splash serves both light and dark; no Android monochrome/themed-icon layer (not present in the export)
**Scale/Scope**: 2 platforms, 1 logo variant, 1 splash design; 1 store icon + full Android mipmap set + full universal iOS appicon set + 2 splash assets — all pre-exported, fetched via DesignSync

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

`.specify/memory/constitution.md` is still the **unfilled template** (all `[PLACEHOLDER]` principles) — no ratified principles exist to gate against, so there are **no constitution violations**. Gate: **PASS (vacuous)**.

Project-level rules that DO apply and are honored by this plan:
- **CLAUDE.md architecture rule** — icons/splash are platform resources placed in the native homes (`composeApp/src/androidMain/res`, `iosApp/iosApp/Assets.xcassets`), consistent with architecture §18 (`androidMain` / `iosApp` platform layers). No `commonMain` or module-boundary changes; no conflict with the architecture.
- **User constraint** — "do not create any inline SVGs" and "use all the icons generated": honored by FR-006/FR-007 (raster deliverables, every size wired in).

Re-check after Phase 1: **still PASS** — design adds only platform resources + one well-scoped Android dependency (core-splashscreen), no architectural deviation.

## Project Structure

### Documentation (this feature)

```text
specs/002-app-icons-splash/
├── plan.md              # This file
├── research.md          # Phase 0 output — sourcing + platform decisions
├── data-model.md        # Phase 1 output — the asset size matrix (the real "schema")
├── quickstart.md        # Phase 1 output — build & visual-verify steps
├── checklists/
│   └── requirements.md  # From /speckit-specify
└── tasks.md             # From /speckit-tasks (NOT created here)
```

*No `contracts/` — this feature exposes no external API/CLI/schema interface; it delivers static platform resources. Skipped per plan template guidance.*

### Source Code (repository root)

```text
composeApp/src/androidMain/
├── AndroidManifest.xml                     # add android:icon + android:roundIcon; set splash theme
├── kotlin/.../MainActivity.kt              # installSplashScreen() before super.onCreate
└── res/
    ├── mipmap-anydpi-v26/
    │   ├── ic_launcher.xml                  # <adaptive-icon> foreground+background (no monochrome)
    │   └── ic_launcher_round.xml
    ├── mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/
    │   ├── ic_launcher.png                  # legacy (API 24–25 fallback) — fetched from design-sync
    │   ├── ic_launcher_foreground.png       # adaptive foreground — fetched from design-sync
    │   └── ic_launcher_background.png       # adaptive background — fetched from design-sync
    ├── drawable/
    │   └── splash_icon.png                  # splash icon — fetched from design-sync
    ├── values/
    │   ├── colors.xml                        # brand background + splash colors
    │   └── themes.xml                        # Theme.App.Starting (postSplashScreenTheme)

iosApp/iosApp/
├── Assets.xcassets/                         # NEW catalog (added to project.pbxproj Resources)
│   ├── AppIcon.appiconset/                  # universal iPhone+iPad sizes + 1024 marketing (opaque) — fetched from design-sync
│   ├── LaunchLogo.imageset/                 # splash logo — fetched from design-sync
│   └── BrandBackground.colorset/            # splash background color
├── Info.plist                               # UILaunchScreen → UIImageName + UIColorName
└── (project.pbxproj)                        # add Assets.xcassets ref + ASSETCATALOG_COMPILER_APPICON_NAME

gradle/libs.versions.toml                    # add androidx-core-splashscreen

# Non-bundled deliverable (not shipped in the app package):
specs/002-app-icons-splash/assets/           # play-store 512 icon (fetched, for console upload)
```

**Structure Decision**: Assets live in the two platform-native resource homes (`androidMain/res`, `iosApp/.../Assets.xcassets`) — the standard Compose Multiplatform placement and the platform layers named in architecture §18. Files are fetched as-is from the design-sync export (no re-rasterization). The Play Store 512×512 icon (uploaded to the console, not bundled) is kept under the feature's `assets/` folder for traceability.

## Complexity Tracking

No constitution violations — table not required. The only added dependency (androidx core-splashscreen) is the Google-recommended way to get a correct branded splash across minSdk 24 → API 36 in one path, versus hand-rolling a launch-theme `windowBackground` drawable that double-flashes on Android 12+. Justified.
