# Cherokee Syllabary

A Cherokee-syllabary drill game (libGDX) by the Cherokee Language project.

Available on itch.io: https://cherokee-language.itch.io/cherokee-syllabary

## Building

Java/Gradle multi-project (`core`, `desktop`, `android`, `ios`); see
`AGENTS.md` for the build commands and toolchain constraints.

## Publishing to itch.io (manual)

Build the desktop jar and release APK, copy `itch.conf.example` to
`itch.conf`, authenticate once with `butler login`, then run
`./itch-push.sh` — see the script header for the one-time setup.
