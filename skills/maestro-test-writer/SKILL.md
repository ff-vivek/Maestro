---
name: maestro-test-writer
description: Author, refactor, and validate Maestro YAML flows, workspace configs, and helper scripts whenever Codex needs to build Android, iOS, or web end-to-end tests in this repository.
---

# Maestro Test Writer

## Overview

Use this skill to produce resilient Maestro flows that match the repo's conventions (`e2e/workspaces`, `recipes`, and `maestro-test/src/test/resources`).
It walks through scoping a scenario, shaping the workspace, composing YAML commands, and validating the suite locally or in CI/Cloud.

## Authoring Workflow

### 1. Frame the scenario
- Capture the user journey, platform (Android/iOS/web), required data, and pass/fail oracle up front.
- Decide whether the flow belongs next to an existing workspace (e.g., `e2e/workspaces/wikipedia`) or inside the consumer app under test.
- When reproducing a regression, restate the bug ID and observable failure inside flow comments or tags so `maestro test --include-tags=bug-123` can target it later.

### 2. Prepare or update the workspace
- Mirror the canonical layout: root `flow.yaml` files + `subflows/`, `scripts/`, and optional `apps/`. See `references/flow-templates.md` plus `e2e/workspaces/wikipedia` for a complete example.
- Add binaries/configs only when needed; keep the repo lean by pointing to `install_apps` scripts instead of checking in artifacts.
- Introduce `config.yaml` once execution ordering, tag filtering, or cross-platform flows matter (sample configs live under `maestro-cli/src/test/resources/workspaces/test_command_test`).

### 3. Outline the flow(s)
- Sketch the states, assertions, and data dependencies before writing YAML so you know which subflows/scripts should be reusable.
- Prefer short, named subflows for onboarding, authentication, or teardown. Link to them with `runFlow` to avoid duplication (`e2e/workspaces/wikipedia/subflows/onboarding-android.yaml`).
- Identify where hooks (`onFlowStart`, `onFlowComplete`) or JS helpers supply deterministic data (see `maestro-test/src/test/resources/103_on_flow_start_complete_hooks.yaml`).

### 4. Implement with Maestro commands
- Start from the templates in `references/flow-templates.md` and enrich them with the command catalogue in `references/command-catalog.md`.
- Tag each flow with platform + intent (e.g., `android`, `passing`, `smoke`, `profile-edit`) so `maestro test --include-tags` behaves predictably.
- Use resilient selectors: favor `id` or `containsText` before relying on absolute coordinates. When optional UI may appear, set `optional: true` (see `e2e/workspaces/wikipedia/android-advanced-flow.yaml`).

### 5. Validate locally (and in CI)
- Run `maestro test <path/to/workspace>` early; add `--include-tags/--exclude-tags`, `--env KEY=VALUE`, `--format junit`, or `--debug-output` as needed (full option list: `maestro-cli/src/main/java/maestro/cli/command/TestCommand.kt`).
- For deterministic suites, tag intentionally failing flows with `failing` so our e2e harness (`e2e/README.md`) can assert the expected exit code.
- Capture flaky evidence with `takeScreenshot`/`screenRecording` flows plus CLI `--debug-output` to speed up triage.

### 6. Harden and share
- Re-run after any refactor touching shared subflows or scripts and update references to changed element IDs/text.
- Document required env vars or secrets directly inside the workspace README or `config.yaml.env` example file so future contributors can run the flows without guesswork.
- Keep flows idempotent: always reset navigation state (e.g., `launchApp: { clearState: true }`) or assert the expected screen before continuing.

## Flow Patterns and Templates
- `references/flow-templates.md` includes:
  - Workspace tree overview, a fully annotated flow skeleton, example `config.yaml`, hook/subflow patterns, and sample JS helpers.
  - Copy the relevant template, then replace IDs/text with the target app's selectors.
- When adding scripts, reuse the simple pattern from `e2e/workspaces/wikipedia/scripts/getSearchQuery.js`—pure functions that populate `output.*` fields.
- For multi-device or shard runs, describe the plan in `config.yaml` and point to the CLI `--shard-split` / `--shard-all` flags so the orchestrator can parallelize safely.

## Command & Feature Map
- `references/command-catalog.md` links each major Maestro command to an in-repo sample flow (mostly `maestro-test/src/test/resources` files) and clarifies when to use it.
- Use it as a checklist while implementing: assertions, gestures, device controls, control-flow primitives, JS/data helpers, and browser-specific steps.
- When a command's behavior changes, update both the reference note and (if needed) the sample file noted there so future authors see the authoritative version.

## Workspace & Execution Notes
- Keep workspace-level metadata next to the flows: `config.yaml` for filtering/execution order, `.env` files for default values, and `README.md` for app install/run instructions.
- Reference the helper scripts under `e2e` for emulator/simulator provisioning (`download_apps`, `install_apps`, `run_tests`, `run_tests_web`).
- When targeting Maestro Cloud, ensure flows avoid local-only commands and add notes about required device pools or browsers.

## Quality & Troubleshooting Checklist
- Assertions first: every flow should end with at least one `assertVisible`/`assertTrue` so regressions don't slip through silent exits.
- Handle animations and transitions via `waitForAnimationToEnd`, `retryTapIfNoChange`, or scoped `extendedWaitUntil` (see `maestro-test/src/test/resources/117_scroll_until_visible_speed.yaml`).
- Prefer data-driven flows—seed inputs via env/JS instead of hardcoding secrets, and clean them up with `eraseText`, `clearKeychain`, or `stopApp` to keep devices reusable.
- If a flake only happens remotely, run with `maestro test --debug-output <dir> --flatten-debug-output` to collect artifacts the same way CI does.
