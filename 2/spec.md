# Spec — Convert the Syllabary repo's deploy target to itch.io (standard itch.io deployment)

Paired spec: **CherokeeLessons.com#23** — the itch.io page for Syllabary and the initial deployment of the currently-published artifact. This spec owns the Syllabary repo's own release-pipeline conversion; the pair splits the app's migration across the two repos that own its halves.

## Problem

The Syllabary repo's releases have deployed through app stores and a site-hosted jar. The migration makes itch.io the deploy target for the repo's future releases: the paired cherokeelessons.com spec establishes the itch page and deploys the currently-published artifact from that repo's copy; this repo's own release pipeline must follow suit, using the standard itch.io deployment the BoundPronouns repo established.

## Scope

The Syllabary repo (`CherokeeLanguage/Syllabary`): repo-local itch deployment tooling per the BoundPronouns pattern, the push-target binding, and itch landing-page URL tracking, and the root `AGENTS.md` agent-instruction file (R-4 — directed into this spec by the developer's request to track the repo's agent-instructions work as a success criterion here; the repo then had no existing README or agent-instruction file). Out of scope: the first production push (happens at the repo's next release).

## Requirements

- **R-1 — Deployment tooling.** Repo-local `itch-push.sh` plus `itch.conf.example` mirroring the BoundPronouns pattern (`butler` resolution PATH→vendored `.tools/butler/`, `REPLACE_ME` config guard, per-channel pushes; no secrets in the repo — butler credentials live in butler's own store), with `itch.conf` and the vendored `.tools/butler/` gitignored. The script's artifact/channel section maps the repo's libgdx build outputs: desktop jar (`desktop:dist` → `desktop/build/libs/`) → `win-linux-mac`; release APK (`android/build/outputs/apk/release/`) → `android` — the same output layout and channel shape the BoundPronouns pattern uses. The paired itch page launches desktop-only; its `android` channel appears when the first release push creates it.
- **R-2 — Target and landing-page URL tracking.** The push target binds to the paired itch project `cherokee-language/cherokee-syllabary`; the repo tracks its itch.io published landing page URL — `https://cherokee-language.itch.io/cherokee-syllabary` — in the itch config (`ITCH_URL` in `itch.conf.example`) and in the README.
- **R-3 — No version claims.** Pushes carry no `--userversion`; butler's auto-numbering stands (metadata is not trusted — no version label is verified or asserted).
- **R-4 — Agent instructions.** The repo carries a root `AGENTS.md` giving future agent sessions verified, repo-specific guidance: module layout (`core`/`desktop`/`android`/`ios`), build/run commands matching the repo's actual Gradle config, release-artifact outputs, and the toolchain constraints an agent would otherwise trip on. When R-1's itch tooling lands, `AGENTS.md` documents its use.

## Success criteria

| ID | Criterion | Class | Instrument | Traces to |
|---|---|---|---|---|
| SC-1 | `itch-push.sh` + `itch.conf.example` exist carrying the BoundPronouns-pattern guards (REPLACE_ME guard, butler resolution PATH→vendored, per-channel pushes); `itch.conf` and the vendored `.tools/butler/` are gitignored | structural | Read the script and config; check `.gitignore` | R-1 |
| SC-2 | The repo tracks its itch.io published landing page URL: `ITCH_URL` in `itch.conf.example` and the URL stated in the README, both matching `https://cherokee-language.itch.io/cherokee-syllabary` | content | Read both files; string match against the paired page URL | R-2 |
| SC-3 | The script's channel section encodes `win-linux-mac` (desktop jar) and `android` (release APK) | structural | Read the script | R-1 |
| SC-4 | Pushes carry no version claims | structural | Script read (`--userversion` absent) | R-3 |
| SC-5 | Root `AGENTS.md` exists, states the module layout (`core`/`desktop`/`android`/`ios`) and the desktop/android release-artifact output locations, names the verified build-blocking toolchain constraints, and — once R-1's tooling exists — documents the itch deployment flow; every command and path it states matches the repo's actual build config | content | Read `AGENTS.md`; check each mandated element present; cross-check each stated command, path, and constraint against `settings.gradle`, the build scripts, and executed toolchain evidence | R-4 |
| SC-6 | The script's push target binds `cherokee-language/cherokee-syllabary` | structural | Read the script and config; the config's target values match `cherokee-language/cherokee-syllabary` | R-2 |

## Approach

Mirror the BoundPronouns repo's `itch-push.sh`/`itch.conf(.example)` pattern, adapted to this repo's build-output paths. No production push is performed in this work: the paired cherokeelessons.com spec owns the initial deployment of the published artifact, and a conversion-verification push to a live channel would replace the published channel artifact — the first real push happens at the repo's next release.

## Out of scope

- The paired cherokeelessons.com work (itch page creation, initial artifact deployment) — CherokeeLessons.com#23.
- The first production push — happens at the repo's next release.

## Impact

Repo-local tooling files, the root `AGENTS.md` doc, a root README (created by this work — SC-2, the repo has none today), and the root `.gitignore` (gains the `itch.conf` and vendored `.tools/butler/` entries — SC-1); no runtime behavior change; the paired itch page gains no new state from this work (the `android` channel appears only at a future release push). The repo's future releases gain a repeatable path to itch.io.

🤖 Co-authored with AI: OpenCode (huggingface/zai-org/GLM-5.3-Flash)
