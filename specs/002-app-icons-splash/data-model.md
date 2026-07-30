# Phase 1 Data Model: Asset Manifest (La Bella Icons & Splash)

The "entities" here are asset sets. This file is the authoritative **size matrix** every delivered file must satisfy. All PNGs are the already-exported files pulled from the design-sync project (research R1/R2) — no re-rasterization. Nothing here is left unreferenced (FR-007).

## Entity: Brand source files (from design-sync `assets/app-icons/`)

| Token | Source path | Use |
|-------|-------------|-----|
| Android foreground/background per density | `android/mipmap-{density}/ic_launcher_foreground.png` / `ic_launcher_background.png` | adaptive `<foreground>` / `<background>` |
| Android legacy launcher per density | `android/mipmap-{density}/ic_launcher.png` | legacy fallback (API 24–25) |
| Android store icon | `android/play-store-512.png` | Play Store listing (not bundled) |
| iOS icon set | `ios/AppIcon-{20,40,58,60,76,80,87,120,152,167,180,1024}.png` | universal iPhone+iPad app icon + marketing |
| Splash logo | `splash/labella-splash-logo.png` (+ `-1x`) | iOS launch image |
| Android 12 splash icon | `splash/android12-splash-icon.png` | `windowSplashScreenAnimatedIcon` |
> Brand background hex for the adaptive `<background>`/splash background is read from the design (Splash & Signup / App Icons & Splash pages) at fetch time; do not hardcode a guess.
> No Android monochrome/themed-icon asset exists in the export — themed icons are out of scope (see spec Edge Cases).

## Entity: Android launcher icon set — `composeApp/src/androidMain/res`

**Adaptive canvas** = 108dp; safe zone already respected by the exported foreground artwork.

| Density | Scale | Legacy `ic_launcher.png` (48dp) | Adaptive `ic_launcher_foreground.png` / `_background.png` (108dp) |
|---------|-------|-----------------------------------|-------------------------------------------------------------------|
| mdpi    | 1×    | 48×48   | 108×108 |
| hdpi    | 1.5×  | 72×72   | 162×162 |
| xhdpi   | 2×    | 96×96   | 216×216 |
| xxhdpi  | 3×    | 144×144 | 324×324 |
| xxxhdpi | 4×    | 192×192 | 432×432 |

All files above are pulled directly from `assets/app-icons/android/mipmap-{density}/` — no resizing needed.

- `mipmap-anydpi-v26/ic_launcher.xml`: `<adaptive-icon>` with `<foreground>@mipmap/ic_launcher_foreground`, `<background>@mipmap/ic_launcher_background`. **No `<monochrome>` entry** (none exported).
- **No `ic_launcher_round`** and no `android:roundIcon` in the manifest: the export has no round PNGs, so a round resource could only live under `anydpi-v26` — unresolvable at `minSdk = 24`. API 26+ launchers mask the adaptive icon themselves; API 24–25 falls back to the legacy `ic_launcher.png`.
- **Play Store icon** (not bundled): exported `play-store-512.png` → `specs/002-app-icons-splash/assets/play-store-icon.png`.

## Entity: Android splash — `composeApp/src/androidMain/res`

| File | Source | Notes |
|------|--------|-------|
| `drawable/splash_icon.png` | exported `splash/android12-splash-icon.png` | `windowSplashScreenAnimatedIcon` |
| `values/colors.xml` → `splash_background` | brand hex (from design) | `windowSplashScreenBackground` |
| `values/themes.xml` → `Theme.App.Starting` | — | `postSplashScreenTheme` = app theme |

## Entity: iOS app icon set — `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset` (universal: iPhone + iPad, opaque)

Exported pixel files (all committed as-is, opaque, no alpha): `AppIcon-{20,40,58,60,76,80,87,120,152,167,180,1024}.png`.

- `Contents.json` maps each file to its standard Apple universal-appiconset slot (`idiom: iphone` @2x/@3x, `idiom: ipad` @1x/@2x, `idiom: ios-marketing` @1x for the 1024 file) by matching each file's pixel size to the slot it satisfies — every exported size is used, none left unreferenced (FR-007).
- Wire into `project.pbxproj`; set `ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon`.

## Entity: iOS splash — `iosApp/iosApp/Assets.xcassets`

| File | Source | Notes |
|------|--------|-------|
| `LaunchLogo.imageset` | exported `splash/labella-splash-logo.png` (+ `-1x` variant) | `Info.plist` `UILaunchScreen.UIImageName = LaunchLogo` |
| `BrandBackground.colorset` | brand hex (from design) | launch background |

> Known limitation: the export provides only 1x (401×275) and 2x (802×550) launch logos — no 3x. On 3x devices iOS scales the 2x asset up, so the launch logo is marginally soft there. Fixing it needs a 3x export from the design source; re-rasterizing locally is out of scope (research R1/R2).

## Wiring checklist (no orphans — FR-007)

- [X] Manifest `<application android:icon>` → mipmaps (no `android:roundIcon` — see above)
- [X] Manifest launcher-activity/application `android:theme` → `Theme.App.Starting`
- [X] `MainActivity` calls `installSplashScreen()` before `super.onCreate()`
- [X] `libs.versions.toml` + `androidMain` deps → `androidx-core-splashscreen`
- [X] `project.pbxproj` references `Assets.xcassets`; `ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon`
- [X] `Info.plist` `UILaunchScreen` → `UIImageName` + background color
- [X] Every PNG in the matrices above exists and is referenced; none orphaned
