# Flutter Web Scroll Test

## Overview

This test verifies that Maestro can properly scroll Flutter web applications using native browser wheel events.

## Problem Solved

Flutter web applications don't respond to standard `window.scroll()` JavaScript commands because Flutter manages its own rendering and event handling. Instead, Flutter listens for browser `WheelEvent` events and processes them through its engine.

## Solution

Maestro now automatically detects Flutter web applications and uses the proper wheel event dispatch sequence:

1. Detects Flutter by checking for `flutter-view`, `flt-glass-pane`, or `flt-renderer` elements
2. Dispatches `mouseover` and `mousemove` events to position the cursor
3. Dispatches a `WheelEvent` with proper parameters (`deltaY`, `deltaMode`, `clientX`, `clientY`)
4. Flutter's engine processes these events and updates the scroll position

## Test Files

- `test_flutter_scroll.html` - A simulated Flutter web page with scrollable content
- `test_flutter_scroll.yaml` - Maestro test that verifies scrolling works

## Running the Test

### Prerequisites

1. Start a local web server serving the test files:

```bash
# From the e2e/workspaces/flutter_web_identifier directory
python3 -m http.server 8086
```

Or use any other web server on port 8086.

### Run the test

```bash
# From the project root
maestro test e2e/workspaces/flutter_web_identifier/test_flutter_scroll.yaml
```

## What the Test Does

1. **Loads the Flutter web page** - Opens the test page with scrollable content
2. **Verifies initial position** - Checks that top content is visible
3. **Scrolls down** - Uses `scroll` command to scroll the page
4. **Verifies scroll worked** - Checks that previously hidden content is now visible
5. **Tests multiple scrolls** - Scrolls multiple times to reach different sections
6. **Tests scroll up** - Uses swipe gestures to scroll back up
7. **Returns to top** - Verifies it can scroll back to the initial position

## Technical Details

### JavaScript Implementation (`maestro-web.js`)

```javascript
maestro.isFlutterApp() // Detects Flutter apps
maestro.scrollFlutter(deltaY, deltaMode) // Scrolls Flutter apps
```

### Kotlin Implementation

**WebDriver.kt** and **CdpWebDriver.kt** both check for Flutter apps before scrolling:

```kotlin
override fun scrollVertical() {
    val isFlutter = executeJS("return window.maestro.isFlutterApp()") as? Boolean ?: false
    
    if (isFlutter) {
        executeJS("window.maestro.scrollFlutter(5, 1)")
    } else {
        scroll("window.scrollY + Math.round(window.innerHeight / 2)", "window.scrollX")
    }
}
```

### Delta Modes

- `0` - PIXEL mode: Scroll by exact pixels
- `1` - LINE mode: Scroll by text lines (default, ~16px per line)
- `2` - PAGE mode: Scroll by viewport pages

## Expected Results

✅ All assertions should pass
✅ Content should become visible as scrolling progresses
✅ Should be able to scroll both down and up
✅ No errors in the console

## Troubleshooting

If the test fails:

1. **Check web server is running** on port 8086
2. **Check browser console** for JavaScript errors
3. **Verify Maestro build** includes the updated files
4. **Check Flutter detection** - Look for "Flutter view element" in console

## References

- `scroll_doc/FLUTTER_SCROLL_GUIDE.md` - Detailed explanation of the solution
- `scroll_doc/NATIVE_BROWSER_SCROLL.md` - Technical implementation details
- Flutter Engine: `lib/web_ui/lib/src/engine/pointer_binding.dart` - How Flutter handles wheel events

