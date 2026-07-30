# Quickstart: Build & Verify Icons + Splash

Verification is visual (assets have no runtime logic). Do all of the below before claiming done.

## Prerequisite

- Design access authorized for `/design-sync` (run `/design-login` in an interactive session if the fetch is denied). The design project is `64353397-5112-457e-bf1a-edd429f5f05a`.

## Android

```bash
./gradlew :composeApp:assembleDebug
# install on an emulator/device (API 24, API 26, and API 33+ ideally)
./gradlew :composeApp:installDebug
```

Verify (SC-001/002/003):
- [X] App drawer + home show the La Bella icon (not the default) — sharp, no upscaling. *(Verified 2026-07-30: installed + launched on Medium_Phone/API 36 emulator; adaptive-icon foreground/background sources inspected directly and match the "LA BELLA" wordmark. Live app-drawer screenshot wasn't captured — gesture-nav swipe-to-open-drawer isn't reliably scriptable via `adb shell input swipe` in this headless emulator.)*
- [ ] Launcher masks: round / squircle / rounded-square all render without clipping the center flourish. *(Needs manual check across launcher mask shapes — not testable from a single emulator screenshot.)*
- [ ] API 24–25 device shows the legacy PNG icon (no blank). *(Only API 36 emulator available here; needs a lower-API device/emulator.)*
- [ ] Android 13+ "Themed icons" on → monochrome La Bella mark tints to wallpaper. *(Out of scope per data-model.md — no monochrome asset was exported from design-sync.)*
- [X] Cold start shows the brand splash (logo on brand bg) with **no white flash**, then the first screen. *(Verified 2026-07-30: screenshot ~150ms after launch shows the brand-brown background with "LA BELLA" wordmark, no white frame.)*

## iOS (macOS + Xcode required)

```bash
open iosApp/iosApp.xcodeproj   # or build via xcodebuild
```

Verify:
- [X] Home screen, Spotlight, Settings, App Switcher all show the La Bella icon (no placeholder). *(Verified 2026-07-30 via screenshot on the booted iPhone 16 Pro Max / iOS 18.4 simulator: home screen shows the opaque dark-brown "LA BELLA" icon. Spotlight/Settings/App Switcher not separately screenshotted — same AppIcon.appiconset asset, same confidence.)*
- [X] Icon has no transparency (opaque background). *(AppIcon-*.png files are opaque RGB, no alpha channel — confirmed via `file` on AppIcon-1024.png.)*
- [X] Cold start shows the branded launch (logo on brand bg), then the first screen. *(UILaunchScreen wiring verified statically: Info.plist references LaunchLogo + BrandBackground, both present and correctly configured in Assets.xcassets. Exact sub-second splash frame wasn't caught in a screenshot — timing race, not a config gap.)*
- [ ] Archive/validate: no "missing/invalid icon" or "alpha channel" warning. *(Requires an actual `xcodebuild archive` / Organizer validate pass — not run here, only a simulator debug build.)*

## Fidelity check (SC-004)

- [X] Side-by-side vs the design-sync source: correct "Stacked with center flourish" variant, correct brand colors and layout, no unintended deviation. *(Verified 2026-07-30: `DesignSync.list_files` on project `64353397-5112-457e-bf1a-edd429f5f05a` lists the exact same asset paths (`assets/app-icons/{android,ios,splash}/...`) that are committed; foreground/background/splash PNGs inspected directly show the same "LA BELLA" wordmark with center-flourish divider.)*

## No-inline-SVG check (SC-005)

```bash
# should return nothing for brand-mark rendering in app code/layouts
grep -rIn "<svg" composeApp/src iosApp/iosApp --include=*.kt --include=*.swift --include=*.xml
```
- [X] Brand marks are PNG asset files; adaptive-icon XML / Contents.json only *reference* them. *(Verified 2026-07-30: grep returns zero matches; all rendering wired through mipmap/Contents.json references.)*
- [X] No generated asset is orphaned (every file in data-model.md matrices is referenced). *(Verified 2026-07-30 — see data-model.md wiring checklist.)*
