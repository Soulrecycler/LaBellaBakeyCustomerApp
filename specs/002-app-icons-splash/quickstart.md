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
- [ ] App drawer + home show the La Bella icon (not the default) — sharp, no upscaling.
- [ ] Launcher masks: round / squircle / rounded-square all render without clipping the center flourish.
- [ ] API 24–25 device shows the legacy PNG icon (no blank).
- [ ] Android 13+ "Themed icons" on → monochrome La Bella mark tints to wallpaper.
- [ ] Cold start shows the brand splash (logo on brand bg) with **no white flash**, then the first screen.

## iOS (macOS + Xcode required)

```bash
open iosApp/iosApp.xcodeproj   # or build via xcodebuild
```

Verify:
- [ ] Home screen, Spotlight, Settings, App Switcher all show the La Bella icon (no placeholder).
- [ ] Icon has no transparency (opaque background).
- [ ] Cold start shows the branded launch (logo on brand bg), then the first screen.
- [ ] Archive/validate: no "missing/invalid icon" or "alpha channel" warning.

## Fidelity check (SC-004)

- [ ] Side-by-side vs the design-sync source: correct "Stacked with center flourish" variant, correct brand colors and layout, no unintended deviation.

## No-inline-SVG check (SC-005)

```bash
# should return nothing for brand-mark rendering in app code/layouts
grep -rIn "<svg" composeApp/src iosApp/iosApp --include=*.kt --include=*.swift --include=*.xml
```
- [ ] Brand marks are PNG asset files; adaptive-icon XML / Contents.json only *reference* them.
- [ ] No generated asset is orphaned (every file in data-model.md matrices is referenced).
