#!/bin/bash

# itch-push.sh — push already-built Syllabary artifacts to itch.io via butler.
#
# Prerequisites (all one-time, developer-side):
#   1. butler installed — either on PATH, or vendored at .tools/butler/butler.
#      To vendor into the repo (gitignored), run one of:
#        curl -L https://broth.itch.zone/butler/linux-amd64/LATEST/archive/default -o butler.zip
#        mkdir -p .tools/butler && unzip butler.zip -d .tools/butler && chmod +x .tools/butler/butler && rm butler.zip
#   2. `butler login` run once (opens a browser; writes butler's own credential store).
#   3. itch.conf filled with the real itch.io target (ITCH_USER, ITCH_GAME).

set -euo pipefail

cd "$(dirname "$0")"

# --- Load the itch.io target ---------------------------------------------
if [ ! -f itch.conf ]; then
    echo "ERROR: itch.conf not found — run: cp itch.conf.example itch.conf," >&2
    echo "  then fill ITCH_USER and ITCH_GAME with the real target." >&2
    exit 1
fi

# shellcheck disable=SC1091
source itch.conf

: "${ITCH_USER:?ERROR: itch.conf must define ITCH_USER}"
: "${ITCH_GAME:?ERROR: itch.conf must define ITCH_GAME}"

if [ "${ITCH_USER}" = "REPLACE_ME" ] || [ "${ITCH_GAME}" = "REPLACE_ME" ]; then
    echo "ERROR: itch.conf still carries REPLACE_ME placeholders — fill in the real itch.io target." >&2
    exit 1
fi

# --- Resolve butler: PATH first, then the vendored copy -------------------
if ! command -v butler >/dev/null 2>&1; then
    if [ -x .tools/butler/butler ]; then
        BUTLER=".tools/butler/butler"
    else
        echo "ERROR: butler not found on PATH and .tools/butler/butler is missing." >&2
        echo "  Vendor it (one-time):" >&2
        echo "    curl -L https://broth.itch.zone/butler/linux-amd64/LATEST/archive/default -o butler.zip" >&2
        echo "  Then authenticate (one-time): butler login" >&2
        exit 1
    fi
else
    BUTLER="butler"
fi

# --- Locate the newest artifacts -----------------------------------------
jar="$(ls -t desktop/build/libs/Syllabary-*.jar 2>/dev/null | head -n1 || true)"
apk="$(ls -t android/build/outputs/apk/release/*-release.apk 2>/dev/null | head -n1 || true)"

if [ -z "${jar}" ]; then
    echo "ERROR: no desktop jar found in desktop/build/libs/ (Syllabary-*.jar)." >&2
    echo "  Build it first:  ./gradlew desktop:dist" >&2
    exit 1
fi

if [ -z "${apk}" ]; then
    echo "ERROR: no release APK found in android/build/outputs/apk/release/ (*-release.apk)." >&2
    echo "  Build it first:  ./gradlew android:assembleRelease" >&2
    exit 1
fi

JAR_CHANNEL="${ITCH_USER}/${ITCH_GAME}:win-linux-mac"
APK_CHANNEL="${ITCH_USER}/${ITCH_GAME}:android"

# --- Push (idempotent patch pushes; first push creates the download) ------
echo "==> Pushing desktop: ${jar}"
"${BUTLER}" push "${jar}" "${JAR_CHANNEL}"

echo "==> Pushing android: ${apk}"
"${BUTLER}" push "${apk}" "${APK_CHANNEL}"

# --- Receipts -------------------------------------------------------------
echo "==> butler receipt:"
"${BUTLER}" status "${JAR_CHANNEL}"
"${BUTLER}" status "${APK_CHANNEL}"

echo "Done."
