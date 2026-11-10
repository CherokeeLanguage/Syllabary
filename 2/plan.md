# Plan — itch.io deploy-target conversion (CherokeeLanguage/Syllabary#2)

Pattern source: `../BoundPronouns/` (`itch-push.sh`, `itch.conf.example`, `.gitignore`, README URL line), adapted to this repo's build outputs; Animals' operator-config variant (`ITCH_JAR`/`ITCH_APK` paths) is not adopted — this repo builds its own artifacts at known paths. Deliverables target SC-1..SC-4, SC-6; SC-5's kept-true clause (AGENTS.md) is item 3. No itch push is performed (out of scope — first push happens at the repo's next release).

## Item 1 — Deployment tooling (SC-1, SC-3, SC-6, SC-4)

- **Deliverable:** `itch-push.sh` (repo root) + `itch.conf.example`; `.gitignore` gains `/itch.conf` and `/.tools/butler/`.
- **RED:** repo has no `itch-push.sh`/`itch.conf.example`; `.gitignore` lacks both entries (verified by file inspection).
- **GREEN:** `itch-push.sh` mirrors BoundPronouns: `set -euo pipefail`; source `itch.conf` with REPLACE_ME guard; butler resolution PATH → vendored `.tools/butler/butler`; locate newest desktop jar (`desktop/build/libs/Syllabary-*.jar`, built by `desktop:dist`) and release APK (`android/build/outputs/apk/release/*-release.apk`, built by `android:assembleRelease`); push `${ITCH_USER}/${ITCH_GAME}:win-linux-mac` and `:android`; butler status receipts; **no `--userversion`** (SC-4, butler auto-numbering). `itch.conf.example` carries `ITCH_USER=cherokee-language`, `ITCH_GAME=cherokee-syllabary` (SC-6 target values), plus `ITCH_URL=https://cherokee-language.itch.io/cherokee-syllabary` (SC-2 config half), no secrets (butler keeps its own store).
- **Instrument:** shell checks — `grep -c win-linux-mac\|android` channels in script; `! grep -- "--userversion" itch-push.sh`; config values match; `grep -E "/itch.conf|/.tools/butler/" .gitignore`; `bash -n itch-push.sh` syntax pass; missing-artifact path exits non-zero with build instructions (dry-run observed).

## Item 2 — README (SC-2)

- **Deliverable:** root `README.md` (new file) stating the itch.io landing URL `https://cherokee-language.itch.io/cherokee-syllabary` and the itch push workflow pointer.
- **RED:** no README exists (verified).
- **GREEN:** minimal README: app one-liner, itch.io availability line with the URL (string-matching SC-2), how-to-play pointer, itch publishing note referencing `itch.conf.example`/`itch-push.sh`.
- **Instrument:** `grep -F "https://cherokee-language.itch.io/cherokee-syllabary" README.md`.

## Item 3 — AGENTS.md kept true (SC-5)

- **Deliverable:** AGENTS.md updated after item 2: the "no README exists" state line removed (README now exists); the itch deployment flow documented under Build & run; state statements true of the repo as it then stands.
- **RED:** AGENTS.md line states "No test suite, no CI, and no README exist in this repo today" — false once item 2 lands; no itch flow documented.
- **GREEN:** edit the state line (drop README, keep test-suite/CI claims if still true); add itch-push usage bullet (build artifacts, `cp itch.conf.example itch.conf`, butler login once, `./itch-push.sh`) with the landing URL.
- **Instrument:** re-run the SC-5 cross-check — every stated command/path vs build scripts; state statements vs repo listing.

Dependency order: 1 → 2 → 3 (AGENTS.md edits depend on the repo state items 1–2 produce).

🤖 Co-authored with AI: OpenCode (huggingface/zai-org/GLM-5.3-Flash)
