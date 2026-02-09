# Maestro Command Catalog (Repo References)
Each entry links a Maestro command to the in-repo example you can open for quick copy/paste. Use it as a checklist while composing flows.

## Navigation & App Lifecycle
- `launchApp` / `clearState` – `maestro-test/src/test/resources/013_launch_app.yaml`
- `stopApp` – `maestro-test/src/test/resources/116_kill_app.yaml`
- `launchApp.clearState + permissions` – `maestro-test/src/test/resources/086_launchApp_sets_all_permissions_to_allow.yaml`
- `backPress` – `maestro-test/src/test/resources/011_back_press.yaml`
- `setOrientation` – `maestro-test/src/test/resources/126_set_orientation.yaml`
- `setLocation` – `maestro-test/src/test/resources/051_set_location.yaml`
- `setPermissions` – `maestro-test/src/test/resources/131_setPermissions.yaml`
- `addMediaDevice` – `maestro-test/src/test/resources/110_add_media_device.yaml`

## Interaction & Gestures
- `tapOn` (text/id) – `maestro-test/src/test/resources/008_tap_on_element.yaml`
- `tapOn` (coordinates) – `maestro-test/src/test/resources/014_tap_on_point.yaml`
- `tapOnRelativePoint` – `maestro-test/src/test/resources/071_tapOnRelativePoint.yaml`
- `tapOn` retries – `maestro-test/src/test/resources/120_tap_on_element_retryTapIfNoChange.yaml`
- `doubleTapOn` – `maestro-test/src/test/resources/101_doubleTapOn.yaml`
- `longPressOn` – `maestro-test/src/test/resources/029_long_press_on_element.yaml`
- `swipe` – `maestro-test/src/test/resources/017_swipe.yaml`
- `scrollUntilVisible` – `maestro-test/src/test/resources/079_scroll_until_visible.yaml`
- `scroll` (basic) – `maestro-test/src/test/resources/010_scroll.yaml`
- `eraseText` / `eraseAllText` – `maestro-test/src/test/resources/036_erase_text.yaml` and `068_erase_all_text.yaml`
- `inputText` – `maestro-test/src/test/resources/012_input_text.yaml`
- `copyText` / `pasteText` – `maestro-test/src/test/resources/062_copy_paste_text.yaml`
- `pressKey` – `maestro-test/src/test/resources/034_press_key.yaml`

## Assertions & Validation
- `assertVisible` variations – files `001` through `008` under `maestro-test/src/test/resources`
- `assertNotVisible` – `maestro-test/src/test/resources/026_assert_not_visible.yaml`
- `assertVisible` by index/size – `maestro-test/src/test/resources/091_assert_visible_by_index.yaml`
- `assertTrue` with JS – `maestro-test/src/test/resources/067_assertTrue_pass.yaml`
- `assertProperties` – `maestro-test/src/test/resources/083_assert_properties.yaml`
- `compareRegex` – `maestro-test/src/test/resources/055_compare_regex.yaml`
- `assertVisible` depth-first search – `maestro-test/src/test/resources/072_searchDepthFirst.yaml`

## Control Flow & Reuse
- `runFlow` / nested subflows – `maestro-test/src/test/resources/046_run_flow.yaml` and `047_run_flow_nested.yaml`
- Conditional subflows (`when`) – `maestro-test/src/test/resources/065_when_true.yaml`
- `runFlow` with env overrides – `maestro-test/src/test/resources/057_runFlow_env.yaml`
- `repeat` / `repeatWhile` – `maestro-test/src/test/resources/075_repeat_while.yaml`
- `ignoreError` – `maestro-test/src/test/resources/056_ignore_error.yaml`
- `runUntilTrue` via scripts – `maestro-test/src/test/resources/098_runscript_conditionals.yaml`
- `runScript` hooks for flow start/complete – `maestro-test/src/test/resources/103_on_flow_start_complete_hooks.yaml`
- `hooks` on failure – `maestro-test/src/test/resources/109_failed_complete_hook.yaml`
- `env` scoping (`058_inline_env.yaml`, `060_pass_env_to_env.yaml`)

## Web & Browser Commands
- `openLink` – `maestro-test/src/test/resources/027_open_link.yaml`
- `openBrowser` + auto verification – `maestro-test/src/test/resources/085_open_link_auto_verify.yaml`
- `assert by CSS` – `maestro-test/src/test/resources/125_assert_by_css.yaml`
- `switchTab` – `maestro-test/src/test/resources/switch_tab_test.yaml`

## Media & Artifacts
- `takeScreenshot` – `maestro-test/src/test/resources/041_take_screenshot.yaml`
- `screenRecording` – `maestro-test/src/test/resources/099_screen_recording.yaml`
- Recording with custom path – `maestro-test/src/test/resources/135_screen_recording_with_path.yaml`
- `addMedia` (camera roll injection) – `maestro-test/src/test/resources/111_add_multiple_media.yaml`

## Device & System Controls
- `airplaneMode` – `maestro-test/src/test/resources/115_airplane_mode.yaml`
- `setClipboard` – `maestro-test/src/test/resources/133_setClipboard.yaml`
- `setOrientation` – `maestro-test/src/test/resources/126_set_orientation.yaml`
- `travel` (location sequences) – `maestro-test/src/test/resources/090_travel.yaml`
- `killApp` – `maestro-test/src/test/resources/116_kill_app.yaml`

## Data & JS Utilities
- `runScript` basic – `maestro-test/src/test/resources/064_js_files.yaml`
- JS outputs to env – `maestro-test/src/test/resources/066_copyText_jsVar.yaml`
- HTTP requests via JS – `maestro-test/src/test/resources/136_js_http_multi_part_requests.yaml`
- `evalScript` inline – `maestro-test/src/test/resources/070_evalScript.yaml`
- Graal JS flows – `maestro-test/src/test/resources/102_graaljs.yaml`

## Browser/Desktop Extras
- `openBrowser` headless, CLI flags `--headless`/`--screen-size` (`maestro-cli/src/main/java/maestro/cli/command/TestCommand.kt` for the full option list).

## Tagging & Expected Failures
- Flow tags for passing/failing – see every file under `e2e/workspaces/*` (notably `e2e/workspaces/wikipedia/android-advanced-flow.yaml`).
- Test harness that enforces labels – `e2e/README.md` explains why flows must declare `passing`/`failing` and how CLI exit codes are validated.

> Tip: When you introduce a new command usage, mirror it in `maestro-test/src/test/resources` so regression tests cover it. Then link that file here.
