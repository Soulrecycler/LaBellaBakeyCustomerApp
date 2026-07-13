---

description: "Task list for App Icons & Splash Screen (La Bella)"
---

# Tasks: App Icons & Splash Screen (La Bella)

**Input**: Design documents from `/specs/002-app-icons-splash/`
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [quickstart.md](./quickstart.md)

**Tests**: Not requested for this feature — verification is manual/visual per `quickstart.md` (static image assets have no runtime logic to unit-test).

**Story mapping note**: `spec.md`'s "User Scenarios & Testing" section no longer carries narrative user stories (removed per user request during specification), but its Functional Requirements and Success Criteria still split cleanly along the original two independently-testable increments. Tasks below use that same split for traceability:
- **US1 = App Icons** (Android + iOS home-screen/launcher icon) — FR-001, FR-002, FR-003, FR-005, FR-006, FR-007, FR-008 (icon-related); SC-001, SC-002
- **US2 = Splash/Launch Screen** (Android + iOS cold-start branding) — FR-004; SC-003

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which increment this task belongs to (US1 or US2)
- Paths are absolute to repo root: `composeApp/...`, `iosApp/...`, `gradle/...`

---

## Phase 1: Setup

**Purpose**: Project/dependency scaffolding shared by both increments

- [ ] T001 [P] Add `androidx-core-splashscreen` version + library alias entries to `gradle/libs.versions.toml`
- [ ] T002 [P] Add `implementation(libs.androidx.core.splashscreen)` to the `androidMain.dependencies` block in `composeApp/build.gradle.kts`
- [ ] T003 [P] Create empty destination directories: `composeApp/src/androidMain/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/`, `composeApp/src/androidMain/res/mipmap-anydpi-v26/`, `composeApp/src/androidMain/res/drawable/`, `iosApp/iosApp/Assets.xcassets/`, `specs/002-app-icons-splash/assets/`

---

## Phase 2: Foundational — Fetch design-sync assets (BLOCKS both increments)

**Purpose**: Pull the already-exported production PNGs and the brand color token from the `/design-sync` project (`64353397-5112-457e-bf1a-edd429f5f05a`) before any wiring can happen. Per [research.md](./research.md) R1/R2, these are used as-is — no re-rasterization.

**⚠️ CRITICAL**: No icon or splash wiring task can start until this phase is complete

- [ ] T004 [P] Read the brand background hex from the `La Bella - App Icons & Splash.dc.html` / `La Bella - Splash & Signup.dc.html` design pages via `DesignSync.get_file`; record it for use in T019 and T025
- [ ] T005 [P] Fetch `assets/app-icons/android/mipmap-mdpi/{ic_launcher.png,ic_launcher_foreground.png,ic_launcher_background.png}` via `DesignSync.get_file` into `composeApp/src/androidMain/res/mipmap-mdpi/`
- [ ] T006 [P] Fetch `assets/app-icons/android/mipmap-hdpi/{ic_launcher.png,ic_launcher_foreground.png,ic_launcher_background.png}` via `DesignSync.get_file` into `composeApp/src/androidMain/res/mipmap-hdpi/`
- [ ] T007 [P] Fetch `assets/app-icons/android/mipmap-xhdpi/{ic_launcher.png,ic_launcher_foreground.png,ic_launcher_background.png}` via `DesignSync.get_file` into `composeApp/src/androidMain/res/mipmap-xhdpi/`
- [ ] T008 [P] Fetch `assets/app-icons/android/mipmap-xxhdpi/{ic_launcher.png,ic_launcher_foreground.png,ic_launcher_background.png}` via `DesignSync.get_file` into `composeApp/src/androidMain/res/mipmap-xxhdpi/`
- [ ] T009 [P] Fetch `assets/app-icons/android/mipmap-xxxhdpi/{ic_launcher.png,ic_launcher_foreground.png,ic_launcher_background.png}` via `DesignSync.get_file` into `composeApp/src/androidMain/res/mipmap-xxxhdpi/`
- [ ] T010 [P] Fetch `assets/app-icons/android/play-store-512.png` via `DesignSync.get_file` into `specs/002-app-icons-splash/assets/play-store-icon.png` (not bundled in the app)
- [ ] T011 [P] Fetch the full iOS set `assets/app-icons/ios/AppIcon-{20,40,58,60,76,80,87,120,152,167,180,1024}.png` via `DesignSync.get_file` into `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`; the 1024 file exceeds the tool's 256 KiB read cap and returns truncated — re-fetch in a way that avoids truncation (e.g. request via an alternate/chunked path) and verify the decoded PNG is complete (correct byte length, opens cleanly) before treating it as done
- [ ] T012 [P] Fetch `assets/app-icons/splash/{labella-splash-logo.png,labella-splash-logo-1x.png,android12-splash-icon.png}` via `DesignSync.get_file` into `specs/002-app-icons-splash/assets/splash/` as a staging location for Phase 4

**Checkpoint**: All source PNGs exist locally at their fetch destinations; brand hex recorded. Both increments below can now proceed (in parallel if staffed).

---

## Phase 3: App Icons (US1) 🎯 MVP

**Goal**: Android and iOS both display the La Bella "Stacked with center flourish" icon on the home screen/launcher, correctly masked/opaque, at every required size — no placeholder, no upscaling.

**Independent Test**: Build and install on an Android emulator/device and iOS simulator/device (per `quickstart.md` Android/iOS sections); confirm the icon renders correctly in launcher, app switcher, and settings, with no clipping under any launcher mask shape and no transparency on iOS.

### Implementation for US1

- [X] T013 [US1] Create `composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher.xml` as an `<adaptive-icon>` with `<foreground>@mipmap/ic_launcher_foreground</foreground>` and `<background>@mipmap/ic_launcher_background</background>` (no `<monochrome>` entry, per revised clarification)
- [X] T014 [US1] Create `composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher_round.xml` with the same `<adaptive-icon>` structure as T013
- [X] T015 [US1] Update `composeApp/src/androidMain/AndroidManifest.xml`: add `android:icon="@mipmap/ic_launcher"` and `android:roundIcon="@mipmap/ic_launcher_round"` to the `<application>` element
- [X] T016 [US1] Create `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/Contents.json` mapping each fetched `AppIcon-*.png` (T011) to its correct `idiom` (`iphone`/`ipad`/`ios-marketing`) and `scale` slot per the universal-appiconset layout in [data-model.md](./data-model.md); every fetched size must appear in exactly one slot
- [X] T017 [US1] Add the `Assets.xcassets` group as a `PBXFileReference` and to the Resources build phase in `iosApp/iosApp.xcodeproj/project.pbxproj`
- [X] T018 [US1] Set the `ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon` build setting for the `iosApp` target in `iosApp/iosApp.xcodeproj/project.pbxproj` (already present in the scaffold)

**Checkpoint**: App icons are fully functional and testable independently of the splash work in Phase 4.

---

## Phase 4: Splash / Launch Screen (US2)

**Goal**: Both platforms show the single fixed-brand La Bella splash on cold start, with no blank/white frame, transitioning cleanly to the first screen.

**Independent Test**: Cold-launch on Android (API 24, API 26, and API 33+ if available) and iOS (per `quickstart.md`); confirm the branded splash (logo centered on brand background) appears immediately and transitions to the first screen with no flash.

### Implementation for US2

- [X] T019 [US2] [P] Add a `splash_background` color entry (brand hex from T004) to `composeApp/src/androidMain/res/values/colors.xml`
- [X] T020 [US2] Create `composeApp/src/androidMain/res/values/themes.xml` defining `Theme.App.Starting` with `windowSplashScreenBackground=@color/splash_background`, `windowSplashScreenAnimatedIcon=@drawable/splash_icon`, and `postSplashScreenTheme` set to the app's existing theme
- [X] T021 [US2] [P] Copy the fetched `android12-splash-icon.png` (T012) into `composeApp/src/androidMain/res/drawable/splash_icon.png`
- [X] T022 [US2] Update `composeApp/src/androidMain/AndroidManifest.xml`: set the launcher activity's (or application's) `android:theme` to `@style/Theme.App.Starting` (depends on T020)
- [X] T023 [US2] Update `composeApp/src/androidMain/kotlin/com/bakery/customer/app/MainActivity.kt`: call `installSplashScreen()` before `super.onCreate()` (depends on T002)
- [X] T024 [US2] [P] Create `iosApp/iosApp/Assets.xcassets/LaunchLogo.imageset/Contents.json` and copy the fetched `labella-splash-logo.png` / `-1x` variant (T012) into the imageset
- [X] T025 [US2] [P] Create `iosApp/iosApp/Assets.xcassets/BrandBackground.colorset/Contents.json` using the brand hex from T004
- [X] T026 [US2] Update `iosApp/iosApp/Info.plist`: set the `UILaunchScreen` dict's `UIImageName` to `LaunchLogo` and add a background color key referencing `BrandBackground` (depends on T024, T025)

**Checkpoint**: Splash is fully functional and testable independently; both increments (US1 + US2) now complete the feature.

---

## Phase 5: Polish & Cross-Cutting Verification

**Purpose**: Confirm nothing is orphaned, nothing regresses the build, and every success criterion in `spec.md` is met

- [ ] T027 [P] Run the no-inline-SVG check from `quickstart.md`: `grep -rIn "<svg" composeApp/src iosApp/iosApp --include=*.kt --include=*.swift --include=*.xml` and confirm no matches (SC-005)
- [ ] T028 [P] Walk the "Wiring checklist" in `data-model.md` and confirm every fetched PNG (T005–T012) is referenced by a manifest/asset-catalog entry with none orphaned (FR-007, SC-005)
- [ ] T029 Run `./gradlew :composeApp:assembleDebug` and resolve any build errors introduced by the new resources/dependency
- [ ] T030 Execute the full `quickstart.md` device/simulator verification checklist (Android + iOS icon and splash checks, fidelity check vs. the design source) and confirm SC-001 through SC-004 pass

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup (needs T003's directories to exist) — BLOCKS Phase 3 and Phase 4
- **US1 – App Icons (Phase 3)**: Depends on Phase 2 (needs fetched Android/iOS icon PNGs from T005–T011) — no dependency on US2
- **US2 – Splash (Phase 4)**: Depends on Phase 2 (needs T004 brand hex, T012 splash PNGs) and T002 (splash dependency) — no dependency on US1
- **Polish (Phase 5)**: Depends on both Phase 3 and Phase 4 being complete

### Within Each Phase

- T013/T014 (Android adaptive XML) can be written in parallel with T016–T018 (iOS appiconset wiring) — different files, different platforms
- T019/T021/T024/T025 are parallel (different files); T020 depends on T019 (references `@color/splash_background`); T022 depends on T020; T026 depends on T024 and T025

### Parallel Opportunities

- All of Phase 1 (T001–T003) in parallel
- All of Phase 2 (T004–T012) in parallel — independent fetches to independent destination files
- Within Phase 3: T013/T014 (Android) parallel with T016 (iOS Contents.json); T017/T018 (same pbxproj file) are sequential with each other and with T016
- Within Phase 4: T019, T021, T024, T025 in parallel; T020, T022, T023, T026 are sequential where noted above
- Phase 3 and Phase 4 can be worked on in parallel by different people once Phase 2 completes

---

## Parallel Example: Phase 2 (Foundational fetch)

```bash
# Launch all independent design-sync fetches together:
Task: "Fetch Android mdpi assets into composeApp/src/androidMain/res/mipmap-mdpi/"
Task: "Fetch Android hdpi assets into composeApp/src/androidMain/res/mipmap-hdpi/"
Task: "Fetch Android xhdpi assets into composeApp/src/androidMain/res/mipmap-xhdpi/"
Task: "Fetch Android xxhdpi assets into composeApp/src/androidMain/res/mipmap-xxhdpi/"
Task: "Fetch Android xxxhdpi assets into composeApp/src/androidMain/res/mipmap-xxxhdpi/"
Task: "Fetch full iOS AppIcon set into iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/"
Task: "Fetch splash assets into specs/002-app-icons-splash/assets/splash/"
```

---

## Implementation Strategy

### MVP First (App Icons only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational fetch (CRITICAL — blocks both increments)
3. Complete Phase 3: App Icons (US1)
4. **STOP and VALIDATE**: Install on Android + iOS, confirm the icon renders correctly everywhere (independent of splash)
5. This alone satisfies SC-001/SC-002 — a reasonable MVP checkpoint

### Incremental Delivery

1. Setup + Foundational fetch → assets ready
2. Add App Icons (US1) → validate independently → icon-only milestone
3. Add Splash (US2) → validate independently → full feature complete
4. Polish (Phase 5) → confirm no orphaned assets, build is clean, full quickstart passes

### Parallel Team Strategy

1. One person/agent completes Setup + Foundational fetch
2. Once done: Developer A takes US1 (App Icons), Developer B takes US2 (Splash) — no shared files, no cross-dependency
3. Both converge on Phase 5 polish once their phase's checkpoint passes

---

## Notes

- [P] tasks touch different files with no unmet dependency
- [Story] label maps each task to US1 (App Icons) or US2 (Splash) per the mapping note at the top of this file
- No test tasks — this feature delivers static assets with manual/visual verification only (`quickstart.md`)
- The only genuine external risk is T011's 256 KiB truncation on `AppIcon-1024.png` — do not commit a truncated file
- Commit after each task or logical group; stop at either checkpoint to validate independently
