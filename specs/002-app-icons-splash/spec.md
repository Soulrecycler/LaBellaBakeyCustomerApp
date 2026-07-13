# Feature Specification: App Icons & Splash Screen (La Bella)

**Feature Branch**: `002-app-icons-splash`
**Created**: 2026-07-12
**Status**: Draft
**Input**: User description: "i want to generate the app icons for both android and ios and splash screen the designs can be taken from /design-sync (La Bella – Splash & Signup). For the app logo use the 'Stacked with center flourish' variant from La Bella – Logo Explorations. Use all the icons generated and do not create any inline SVGs."

## Clarifications

### Session 2026-07-12

- Q: Which Apple device family should the iOS app icon set target? → A: iPhone only (iPhone + App Store icon sizes; iPad out of scope) — **superseded 2026-07-13**, see below
- Q: Should Android also generate a themed (monochrome) app-icon layer for Android 13+? → A: Yes, include the monochrome adaptive-icon layer — **superseded 2026-07-13**, see below
- Q: Should the splash have separate light/dark variants or one fixed-brand splash? → A: Single fixed-brand splash shown identically in light and dark

### Session 2026-07-13 (design source review)

Reviewing the actual `/design-sync` project (`64353397-5112-457e-bf1a-edd429f5f05a`) surfaced pre-exported production PNG assets under `assets/app-icons/` for both platforms, which revised two earlier decisions:

- Q: The design's exported iOS set includes iPad sizes (76/152/167px) already generated — use them or stay iPhone-only? → A: Use all exported sizes (universal iPhone + iPad app-icon set)
- Q: The design's exported Android set has no monochrome/themed-icon layer — derive one or drop the requirement? → A: Drop the monochrome requirement; ship only the foreground/background layers actually exported

## User Scenarios & Testing *(mandatory)*

### Edge Cases

- **Adaptive icon safe zone (Android)**: The "center flourish" detail must remain fully visible inside the adaptive-icon safe zone across all launcher mask shapes; artwork outside the safe zone may be cropped by the mask and must not contain essential logo elements.
- **iOS alpha channel**: iOS marketing/app icons must not contain transparency; the background must be fully opaque.
- **Very small render sizes**: At the smallest launcher/notification sizes, fine flourish detail may become illegible — the icon must still read as the La Bella mark.
- **Missing density/size**: If any required icon density (Android) or size (iOS) is absent, the OS falls back to upscaling or a placeholder — every required size must be supplied so no fallback occurs.
- **Slow cold start**: If initialization takes longer than expected, the splash must remain shown (no blank frame) until the first screen is ready.
- **Themed icons (Android 13+)**: No monochrome layer is provided; themed (wallpaper-tinted) home screens fall back to the platform default treatment for this app rather than a tinted La Bella mark.
- **Fixed-brand splash contrast**: Since one splash serves both light and dark appearance, its fixed brand background and logo must maintain sufficient contrast in both without a separate dark variant.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Android app MUST display the La Bella "Stacked with center flourish" logo as its launcher icon, provided as an adaptive icon (separate foreground and background layers) plus legacy fallback, at all required launcher densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi). No monochrome/themed-icon layer is required (none exists in the design source).
- **FR-002**: The Android build MUST include the high-resolution store/marketing icon at the required store resolution.
- **FR-003**: The iOS app MUST display the La Bella "Stacked with center flourish" logo as its app icon, supplying every icon size exported for **iPhone and iPad** (universal set) plus the marketing/App Store size, with a fully opaque (non-transparent) background.
- **FR-004**: Both platforms MUST show a single branded splash/launch screen matching the "La Bella – Splash & Signup" design (correct logo placement, fixed brand background color, and layout) during app cold start, transitioning to the first app screen without a visible blank frame. The same splash is shown in both light and dark appearance (no separate dark variant).
- **FR-005**: All icon and splash assets MUST be sourced from the referenced design (via the design-sync source) using the exact "Stacked with center flourish" logo variant — no ad-hoc or improvised logo artwork.
- **FR-006**: The delivered icon and splash assets MUST be committed as generated image asset files placed in the platform-standard asset locations; the implementation MUST NOT embed inline SVG markup in application code or layouts to render these brand marks.
- **FR-007**: Every generated icon output MUST be wired into the app (referenced by the platform manifests/asset catalogs) — no generated size may be left unused or orphaned.
- **FR-008**: The app icon and splash MUST render without clipping essential logo elements, pixelation, or aspect-ratio distortion across the launcher mask shapes and screen sizes/densities of supported devices.

### Key Entities *(include if feature involves data)*

- **App Icon Set**: The complete collection of platform icon assets (Android adaptive foreground/background layers + density-specific legacy icons + store icon; iOS full universal iPhone+iPad app-icon size set + marketing icon) derived from the La Bella "Stacked with center flourish" logo.
- **Splash/Launch Asset**: The branded startup visual (logo + brand background) matching the La Bella – Splash & Signup design, one per platform in the platform's expected format.
- **Brand Source Design**: The design-sync design of record — the "Stacked with center flourish" logo and the Splash & Signup layout — that all generated assets must faithfully reproduce.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: On a fresh install, 100% of required icon slots on both Android and iOS are filled — no platform default placeholder or upscaled/blurry icon appears in any home-screen, launcher, settings, or app-switcher context.
- **SC-002**: The launcher icon renders correctly under all standard Android launcher mask shapes (round, squircle, rounded-square) with no essential logo element clipped, and iOS shows the correct icon in home screen, Spotlight, Settings, and App Store contexts.
- **SC-003**: On cold start, the branded splash appears within the first rendered frame (no blank/white flash) on both platforms and transitions to the first screen.
- **SC-004**: A reviewer comparing the shipped icon and splash side-by-side with the source design confirms visual fidelity (correct logo variant, colors, and layout) with no unintended deviations.
- **SC-005**: Zero inline SVG markup is used to render the brand icon or splash logo in application code; all brand marks are delivered as generated asset files, and no generated asset is left unreferenced.

## Assumptions

- The app is a Kotlin Multiplatform mobile app targeting **Android and iOS** (per the existing project scaffold); "app icons for both" refers to these two platforms. Web/desktop icon targets are out of scope.
- The brand is **"La Bella"** and the authoritative visual source is the referenced design-sync design; the "Stacked with center flourish" logo variant is chosen for the primary mark.
- Exact brand color values, background treatment, and precise logo proportions are taken from the design-sync source at implementation time (not re-specified here).
- The iOS app ships a **universal (iPhone + iPad) icon set**, matching every size already exported in the design-sync source (per revised clarification); the app need not otherwise be iPad-optimized.
- Splash implementation uses each platform's standard launch-screen mechanism; a **single fixed-brand splash** serves both light and dark appearance, and an animated/interactive splash beyond the static branded design is out of scope for this feature.
- Store-listing artwork beyond the required high-resolution app/marketing icon (feature graphics, screenshots) is out of scope.
