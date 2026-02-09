# Commands Reference

> **Complete reference for all Maestro commands applicable to Flutter Web testing**

---

## Command Syntax

Commands can be written in two forms:

```yaml
# Simple form (string value)
- commandName: "value"

# Extended form (object with properties)
- commandName:
    property1: value1
    property2: value2
```

---

## App Control Commands

### launchApp

Launches the browser and navigates to the URL specified in the flow header.

```yaml
url: http://localhost:4446
---
- launchApp
```

With options:

```yaml
- launchApp:
    clearState: true    # Clear cookies/storage
```

### stopApp

Stops/closes the current browser tab.

```yaml
- stopApp
```

### clearState

Clears browser storage (cookies, localStorage, sessionStorage).

```yaml
- clearState
```

### openLink

Opens a URL in the browser.

```yaml
# Simple
- openLink: "https://example.com"

# Full form
- openLink:
    link: "https://example.com/page"
```

### back

Navigates back in browser history.

```yaml
- back
```

### switchTab

Switches to a browser tab by index (0-based).

```yaml
# Simple form - switch to first tab
- switchTab: 0

# Switch to second tab
- switchTab: 1

# Full form with options
- switchTab:
    index: 0
    label: "Switch to first tab"
    optional: true
```

**Note**: Tab indexes are 0-based. The first tab is index 0, second tab is index 1, etc.

---

## Tap/Click Commands

### tapOn

Taps on an element. The most commonly used command.

```yaml
# By text
- tapOn: "Button Text"

# By text (regex)
- tapOn: "Item [0-9]+"

# By ID
- tapOn:
    id: "submit_button"

# By custom Flutter ID
- tapOn:
    flutterId: submit_button

# By point (percentage)
- tapOn:
    point: "50%,50%"

# By point (pixels)
- tapOn:
    point: "200,300"

# With index (when multiple matches)
- tapOn:
    text: "Item"
    index: 0          # First match (0-based)

# With retry disabled
- tapOn:
    text: "Button"
    retryTapIfNoChange: false

# Optional (won't fail if not found)
- tapOn:
    text: "Optional Button"
    optional: true
```

#### Relative Position Selectors

```yaml
# Below another element
- tapOn:
    text: "Value"
    below: "Label"

# Above another element
- tapOn:
    text: "Edit"
    above: "Delete"

# Left of another element
- tapOn:
    text: "Cancel"
    leftOf: "Submit"

# Right of another element
- tapOn:
    text: "Next"
    rightOf: "Previous"
```

### doubleTapOn

Double-taps on an element.

```yaml
- doubleTapOn: "Editable Text"

- doubleTapOn:
    id: "zoom_area"
```

### longPressOn

Long-presses on an element (typically 1-2 seconds).

```yaml
- longPressOn: "Menu Item"

- longPressOn:
    id: "context_menu_trigger"
```

### tapOn with Repeat

Tap multiple times in succession.

```yaml
- tapOn:
    text: "Increment"
    repeat: 5
    delay: 100        # ms between taps
```

---

## Text Input Commands

### inputText

Types text into the currently focused element.

```yaml
# Simple
- inputText: "Hello World"

# With special characters
- inputText: "user@example.com"

# With variable
- inputText: "${USERNAME}"
```

**Note:** An input field must be focused first (usually via `tapOn`).

```yaml
- tapOn: "Email field"
- inputText: "user@example.com"
```

### eraseText

Deletes characters from the current input.

```yaml
# Erase specific number of characters
- eraseText: 10

# Erase all (large number)
- eraseText: 100
```

### inputRandomText

Inputs randomly generated text.

```yaml
- inputRandomText

# With type
- inputRandomText:
    type: email       # email, name, number, text

# With length
- inputRandomText:
    length: 10
```

Available types:
- `text` - Random text
- `number` - Random number
- `email` - Random email address
- `name` - Random person name

### pressKey

Presses a keyboard key.

```yaml
- pressKey: Enter
- pressKey: Backspace
- pressKey: Tab
```

Supported keys:
- `Enter`
- `Backspace`
- `Tab` (limited support)

### hideKeyboard

Hides the on-screen keyboard (if visible).

```yaml
- hideKeyboard
```

---

## Assertion Commands

### assertVisible

Asserts that an element is visible on screen.

```yaml
# By text
- assertVisible: "Welcome"

# By text (regex)
- assertVisible: "Order #[0-9]+"

# By ID
- assertVisible:
    id: "success_message"

# By custom ID
- assertVisible:
    flutterId: dashboard_header

# With timeout
- assertVisible:
    text: "Loaded"
    timeout: 10000    # 10 seconds

# Optional (won't fail test)
- assertVisible:
    text: "Optional Element"
    optional: true

# Check enabled state
- assertVisible:
    text: "Submit"
    enabled: true

# Check selected state
- assertVisible:
    text: "Option A"
    selected: true
```

### assertNotVisible

Asserts that an element is NOT visible.

```yaml
# By text
- assertNotVisible: "Error"

# By ID
- assertNotVisible:
    id: "loading_spinner"

# With timeout
- assertNotVisible:
    text: "Loading..."
    timeout: 5000
```

### assertTrue

Asserts that a JavaScript expression evaluates to true.

```yaml
- assertTrue: "${output.count > 0}"
- assertTrue: "${output.status == 'success'}"
```

---

## Scroll Commands

### scroll

Scrolls the page vertically.

```yaml
# Scroll down
- scroll
```

### scrollUntilVisible

Scrolls until an element becomes visible.

```yaml
# Basic
- scrollUntilVisible:
    element: "Target Element"
    direction: DOWN

# With options
- scrollUntilVisible:
    element:
      text: "Load More"
    direction: DOWN
    timeout: 20000         # Max time to scroll
    speed: 50              # 0-100, higher = faster
    visibilityPercentage: 80  # Element visibility threshold
```

Direction options:
- `UP`
- `DOWN`
- `LEFT`
- `RIGHT`

### swipe

Performs a swipe gesture.

```yaml
# By direction
- swipe:
    direction: UP
    duration: 500     # ms

# By coordinates
- swipe:
    start: "50%,80%"
    end: "50%,20%"
    duration: 500

# On specific element
- swipe:
    direction: LEFT
    element: "Carousel"
```

---

## Wait Commands

### waitForAnimationToEnd

Waits for screen animations to complete.

```yaml
- waitForAnimationToEnd

# With timeout
- waitForAnimationToEnd:
    timeout: 3000
```

### extendedWaitUntil

Waits until a condition is met.

```yaml
# Wait for element to appear
- extendedWaitUntil:
    visible: "Data Loaded"
    timeout: 30000

# Wait for element to disappear
- extendedWaitUntil:
    notVisible: "Loading..."
    timeout: 10000
```

---

## Screenshot & Recording Commands

### takeScreenshot

Captures a screenshot.

```yaml
- takeScreenshot: screenshot_name
```

Screenshots are saved to the test output directory.

### startRecording

Starts video recording.

```yaml
- startRecording: my_recording
```

### stopRecording

Stops video recording.

```yaml
- stopRecording
```

---

## Flow Control Commands

### runFlow

Runs another flow file (subflow).

```yaml
# Simple
- runFlow: subflows/login.yaml

# With condition
- runFlow:
    file: subflows/login.yaml
    when:
      visible: "Login"

# With environment variables
- runFlow:
    file: subflows/login.yaml
    env:
      USER: admin
```

### runScript

Runs a JavaScript file.

```yaml
- runScript: scripts/prepare_data.js

# With environment variables
- runScript:
    file: scripts/setup.js
    env:
      API_URL: http://localhost:3000
```

### evalScript

Evaluates JavaScript inline and stores result.

```yaml
# Store calculation result
- evalScript: ${output.total = 5 + 3}
- assertTrue: "${output.total == 8}"

# Get current viewport dimensions
- evalScript: ${output.width = window.innerWidth}
- evalScript: ${output.height = window.innerHeight}

# Resize browser window (non-headless mode only)
# For headless, use CLI: maestro test --headless --screen-size 1920x1080
- evalScript: ${resizeTo(1920, 1080)}
```

### repeat

Repeats commands multiple times.

```yaml
# Fixed number of times
- repeat:
    times: 3
    commands:
      - tapOn: "Add Item"

# While condition is true
- repeat:
    while:
      visible: "Next Page"
    commands:
      - tapOn: "Next Page"
      - waitForAnimationToEnd
```

### retry

Retries commands on failure.

```yaml
- retry:
    maxRetries: 3
    commands:
      - tapOn: "Flaky Button"
      - assertVisible: "Success"
```

### doIf / Conditional Execution

Execute commands conditionally.

```yaml
# If element is visible
- runFlow:
    file: handle_popup.yaml
    when:
      visible: "Accept Cookies"

# If element is NOT visible
- runFlow:
    file: login.yaml
    when:
      notVisible: "Dashboard"

# Using JavaScript condition
- runFlow:
    file: admin_flow.yaml
    when:
      true: "${USER_ROLE == 'admin'}"
```

---

## Variable Commands

### Environment Variables

Define variables in flow header:

```yaml
url: http://localhost:4446
env:
  USERNAME: testuser
  PASSWORD: secret123
---
- inputText: "${USERNAME}"
```

Override at runtime:

```bash
maestro test -e USERNAME=admin flow.yaml
```

### copyTextFrom

Copies text from an element to a variable.

```yaml
- copyTextFrom:
    id: "order_number"
    
- assertTrue: "${maestro.copiedText != ''}"
```

---

## Labels & Documentation

Add labels to commands for better test output:

```yaml
- tapOn:
    text: "Submit"
    label: "Submit the registration form"
```

---

## Common Command Arguments

These arguments work with most commands:

| Argument | Type | Description |
|----------|------|-------------|
| `optional` | boolean | Don't fail if command fails |
| `label` | string | Custom label for test output |
| `timeout` | number | Override default timeout (ms) |
| `waitToSettleTimeoutMs` | number | Wait for UI to settle |

Example:

```yaml
- tapOn:
    text: "Optional Feature"
    optional: true
    label: "Try to tap optional feature"
    timeout: 5000
```

---

## Command Quick Reference

| Category | Command | Description |
|----------|---------|-------------|
| **App** | `launchApp` | Start browser, navigate to URL |
| | `stopApp` | Close browser |
| | `clearState` | Clear cookies/storage |
| | `openLink` | Navigate to URL |
| | `back` | Browser back |
| **Tap** | `tapOn` | Tap element |
| | `doubleTapOn` | Double tap |
| | `longPressOn` | Long press |
| **Input** | `inputText` | Type text |
| | `eraseText` | Delete text |
| | `pressKey` | Press key |
| | `hideKeyboard` | Hide keyboard |
| **Assert** | `assertVisible` | Assert element visible |
| | `assertNotVisible` | Assert element not visible |
| | `assertTrue` | Assert JS expression |
| **Scroll** | `scroll` | Scroll page |
| | `scrollUntilVisible` | Scroll to element |
| | `swipe` | Swipe gesture |
| **Wait** | `waitForAnimationToEnd` | Wait for animations |
| | `extendedWaitUntil` | Wait for condition |
| **Media** | `takeScreenshot` | Capture screenshot |
| | `startRecording` | Start video |
| | `stopRecording` | Stop video |
| **Flow** | `runFlow` | Run subflow |
| | `runScript` | Run JS file |
| | `repeat` | Loop commands |
| | `retry` | Retry on failure |

---

**Previous:** [← Flutter Web Setup](./03-FLUTTER_WEB_SETUP.md) | **Next:** [Element Selection →](./05-ELEMENT_SELECTION.md)
