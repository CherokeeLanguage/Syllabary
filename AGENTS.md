# AGENTS.md — CherokeeLanguage/Syllabary

A Cherokee-syllabary drill game on libGDX 1.9.10 (Java 7 source level), built
as a Gradle multi-project: `core` (game logic), `desktop` (LWJGL2 backend),
`android` (APK + the shared asset home), `ios` (RoboVM).

## Build & run

- **`./gradlew` needs a Gradle-6.1-era JDK (Java 8–13).** On newer JDKs every
  real Gradle task dies at configuration with
  `NoClassDefFoundError: Could not initialize class org.codehaus.groovy.vmplugin.v7.Java7`
  (verified on JDK 21 and JDK 25; `./gradlew --version` itself does run).
  Point `JAVA_HOME` at a compatible JDK before invoking the wrapper.
- Desktop: `./gradlew desktop:run` (working dir is `android/assets`),
  `desktop:debug`, `desktop:dist` (fat jar → `desktop/build/libs/`). Launcher:
  `com.cherokeelessons.syllabary.one.desktop.DesktopLauncher`.
- Android: `./gradlew android:assembleDebug|assembleRelease` (APKs →
  `android/build/outputs/apk/…`); `copyAndroidNatives` hooks itself into every
  package task. The SDK comes from `local.properties` `sdk.dir` (gitignored,
  machine-local) or `ANDROID_HOME`; `android:run` drives `adb`.
- **Android release signing resolves `RELEASE_STORE_FILEA`/`RELEASE_STORE_FILEB`,
  `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` —
  referenced in `android/build.gradle` but defined nowhere in this repo**
  (they come from the build machine's config or `-P` properties).
- iOS runs through RoboVM on macOS; the signing identity is configured in the
  root `build.gradle` (`IOSLauncher` entrypoint).
- The app version lives in three synced places: root `build.gradle`
  `allprojects.version`, android `versionName`, android `versionCode`
  (currently 3.27 / 327).

## Layout facts

- **All runtime assets live in `android/assets`** — `core`, `desktop`, and
  `ios` all read them from there (sourceSets resources / project links;
  desktop `run` uses it as its working dir). Put new assets there, not in
  module `src/` trees.
- `audio/`, `artwork/`, `artwork-generated/` hold source/working asset
  material — no build file references them. `notes.md` is a game-loop design
  note.
- No test suite, no CI, and no README exist in this repo today.
- Trunk is `master`; the pre-commit/pre-push hooks block direct trunk commits
  and pushes — work on a feature branch and let a human merge the PR.
- `.issues/` is the issue-store worktree (orphan `issues-data` branch) and
  `.opencode/` is the gitignored agent-deck checkout — never stage or commit
  either from this repo.
- This file's own bar: every command and path here is verified against the
  repo's actual build config — re-verify before relying on it, and drop
  anything that stops being true.
