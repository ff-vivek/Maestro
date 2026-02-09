# Flow & Workspace Templates

Use these snippets while bootstrapping Maestro workspaces inside this repo. Replace placeholder IDs/text with selectors from the target app.

## Workspace Layout (clone of `e2e/workspaces/wikipedia`)
```
my-workspace/
├── android-flow.yaml           # primary platform flow(s)
├── ios-flow.yaml               # sibling flow, same tags naming
├── subflows/
│   ├── onboarding-android.yaml # reusable auth/setup steps
│   └── teardown.yaml
├── scripts/
│   └── getSearchQuery.js       # outputs values via `output.*`
├── config.yaml                 # optional filters/execution order
└── README.md                   # install + env requirements
```
- Keep APK/IPA artifacts out of git; instruct devs to run `e2e/install_apps` or fetch from CI instead.
- Tag each flow with platform + intent; e.g. `tags: [android, passing, smoke]` mirrors `e2e/workspaces/wikipedia/android-advanced-flow.yaml`.

## Base Flow Skeleton
```
appId: com.example.app
config:
  launchDelay: 500              # optional default waits
onFlowStart:
  - runScript: scripts/bootstrap.js
onFlowComplete:
  - stopApp
  - takeScreenshot: artifacts/${MAESTRO_DEVICE_NAME}.png
---
- launchApp:
    clearState: true
- runFlow: subflows/login.yaml
- tapOn:
    id: "com.example:id/profile"
- inputText: ${env.USERNAME}
- assertVisible:
    text: "Welcome ${env.USERNAME}"
```
Key points:
- Reset app state unless the scenario depends on persisted data.
- Reference env vars like `${env.USERNAME}` for credentials; fall back to `config.env` files if needed.
- Keep assertions close to the action to pinpoint failures.

## Config Template (see `maestro-cli/src/test/resources/workspaces/test_command_test/03_mixed_with_config_execution_order/config.yaml`)
```
flows:
  - "subflows/*"
  - "android/*.yaml"
includeTags: [smoke]
excludeTags: [flaky]
executionOrder:
  continueOnFailure: false
  flowsOrder:
    - android/login.yaml
    - android/settings.yaml
```
- Use globs under `flows:` to scope CLI discovery.
- `executionOrder.flowsOrder` prevents regressions when flows depend on preceding state.
- Pair with CLI flags such as `maestro test workspace --include-tags smoke --format junit`.

## Subflow Pattern
```
# subflows/login.yaml
appId: com.example.app
---
- tapOn:
    id: "com.example:id/email"
- inputText: ${env.USER_EMAIL}
- tapOn:
    id: "com.example:id/password"
- inputText: ${env.USER_PASSWORD}
- tapOn:
    text: "Sign in"
```
- Keep subflows stateless; they should assume the surrounding flow positioned the screen.
- Reuse them across platforms by parameterizing selectors via env vars or script output.

## JS Helper Pattern (`e2e/workspaces/wikipedia/scripts/getSearchQuery.js`)
```js
// scripts/bootstrap.js
output.searchQuery = `release-${Date.now()}`;
output.user = {
  email: env.USER_EMAIL ?? "qa@example.com",
  password: env.USER_PASSWORD ?? "Passw0rd!"
};
```
- Always write to the `output` object; downstream steps can access `${output.searchQuery}`.
- Avoid asynchronous APIs; Maestro runs Rhino/Graal JS synchronously.

## Tagging & Metadata Checklist
- `tags:` required for e2e correctness (e.g., `passing`, `failing` for `e2e/README.md` harness enforcement).
- Encode bug IDs (`bug-1234`) or suites (`checkout-smoke`) to filter via CLI `--include-tags`/`--exclude-tags`.
- Document quirks (feature flags, data seeding) in the workspace README so contributors understand prerequisites quickly.

## Validation Loop
1. `maestro test e2e/workspaces/your_app` locally.
2. Capture artifacts with `--debug-output tmp/artifacts --flatten-debug-output` when diagnosing flakes.
3. For CI, surface `--format junit --output build/reports/maestro` so failures appear in standard test dashboards.
4. If targeting Maestro Cloud, ensure flows avoid local shell-side effects and read the desired device/browser config from env.
```
