# Flutter Web Scroll Test - Troubleshooting Guide

## Quick Start

### Running with Full Logging

1. **Start the web server:**
   ```bash
   cd /Users/vivekyadav/Documents/Projects/externals/Maestro-main/e2e/workspaces/flutter_web_identifier
   python3 -m http.server 8088
   ```

2. **Open browser console manually to see logs:**
   ```bash
   # Open in browser to see console logs
   open http://localhost:8088/test_flutter_scroll.html
   ```
   Then open DevTools (F12 or Cmd+Option+I) and look at Console tab

3. **Run the debug test:**
   ```bash
   maestro test test_flutter_scroll_debug.yaml
   ```

## Log Prefixes

All logs are prefixed to help identify the source:

- `[Maestro]` - Logs from maestro-web.js functions
- `[Test Page]` - Logs from the test HTML page
- `[Maestro DEBUG]` - Logs from the test YAML file

## What to Look For

### 1. Page Load Issues

**Expected logs:**
```
[Test Page] Script loading...
[Test Page] Elements found: { flutterView: true, content: true, ... }
[Test Page] ==================================
[Test Page] Flutter web scroll test page loaded
[Test Page] Ready for testing!
[Test Page] ==================================
```

**If missing:**
- ❌ Server not running on port 8088
- ❌ Wrong URL in test file
- ❌ HTML file syntax error

### 2. Flutter Detection

**Expected logs:**
```
[Maestro] Flutter detection: {
  isFlutter: true,
  hasFlutterView: true,
  hasGlassPane: true,
  hasFltRenderer: true
}
```

**If `isFlutter: false`:**
- ❌ Page not loaded yet (check timing)
- ❌ Flutter elements not created properly
- ❌ JavaScript not executed

### 3. Scroll Execution

**Expected logs when scroll happens:**

**From Maestro:**
```
[Maestro] scrollFlutter called: { deltaY: 5, deltaMode: 1 }
[Maestro] Target element found: FLUTTER-VIEW
[Maestro] Viewport center: { x: 640, y: 384, width: 1280, height: 768 }
[Maestro] Mouseover event dispatched: true
[Maestro] Mousemove event dispatched: true
[Maestro] Wheel event dispatched: true
```

**From Test Page:**
```
[Test Page] Mouse over Flutter view at 640 384
[Test Page] ===== WHEEL EVENT RECEIVED =====
[Test Page] Event details: { deltaY: 5, deltaMode: 1, ... }
[Test Page] LINE mode: converted 5 lines to 80 pixels
[Test Page] Scroll update: { oldPosition: 0, newPosition: 80, ... }
[Test Page] Transform applied: translateY(-80px)
[Test Page] ===== WHEEL EVENT COMPLETE =====
```

### 4. Common Error Patterns

#### Error: "Flutter root element not found"

**Logs you'll see:**
```
[Maestro] Flutter root element not found!
[Maestro] Available elements: { body: true, allElements: 50 }
```

**Cause:** Flutter detection returned true, but elements not found when scrolling

**Solutions:**
- Wait longer after page load (add delay)
- Check if elements are removed after page load
- Verify HTML structure

#### Error: No wheel events received

**Symptoms:**
- No `[Test Page] ===== WHEEL EVENT RECEIVED =====` logs
- Events dispatched but not received

**Logs to check:**
```
[Maestro] Wheel event dispatched: true  ← Should be true
```

**Possible causes:**
- Event listener not attached yet
- Event not bubbling correctly
- Target element incorrect

**Solutions:**
```yaml
# Add delay before scroll
- evalScript: |
    new Promise(resolve => setTimeout(resolve, 1000));
- scroll
```

#### Error: Content not scrolling

**Symptoms:**
- Wheel events received
- Transform applied
- But assertions fail

**Check:**
```
[Test Page] Transform applied: translateY(-80px)  ← Is this happening?
```

**If transform NOT applied:**
- CSS might be overriding
- Content element not found
- JavaScript error

**If transform IS applied but assertion fails:**
- Not scrolling enough (increase deltaY)
- Timing issue (content not visible yet)
- Wrong content to assert on

### 5. Assertion Failures

#### "Section 2" not visible after scroll

**Debug steps:**

1. Check how much was scrolled:
   ```
   [Test Page] Scroll update: { newPosition: 80, ... }
   ```
   80px might not be enough to show Section 2

2. Check viewport and content:
   ```
   [Test Page] Elements found: { contentHeight: 1536, windowHeight: 768 }
   ```

3. Increase scroll distance:
   ```javascript
   // In maestro-web.js, change:
   executeJS("window.maestro.scrollFlutter(10, 1)")  // Instead of 5
   ```

## Debug Test Features

The `test_flutter_scroll_debug.yaml` includes:

### 1. Pre-scroll checks
```yaml
- evalScript: |
    console.log('=== MAESTRO DEBUG: About to scroll ===');
```

### 2. Post-scroll verification
```yaml
- evalScript: |
    const content = document.getElementById('flutter-content');
    const transform = content.style.transform;
    console.log('Content transform after scroll:', transform);
```

### 3. Timing delays
```yaml
- evalScript: |
    new Promise(resolve => setTimeout(resolve, 500));
```

## Manual Testing

### Test in Browser Console

1. Open http://localhost:8088/test_flutter_scroll.html
2. Open DevTools Console (F12)
3. Run manually:

```javascript
// Check detection
window.maestro.isFlutterApp()
// Should return: true

// Try scrolling
window.maestro.scrollFlutter(5, 1)
// Should see: [Maestro] logs and [Test Page] logs

// Check transform
document.getElementById('flutter-content').style.transform
// Should show: translateY(-XXXpx)
```

### Visual Verification

The page has colored sections:
- 🟢 Green background = TOP
- 🟣 Purple gradient = Sections 1-4
- 🔴 Red background = BOTTOM

Watch the browser window while running commands to see if content moves.

## Common Solutions

### Solution 1: Add wait time before scroll

```yaml
- launchApp
- evalScript: |
    new Promise(resolve => setTimeout(resolve, 1000));
- scroll
```

### Solution 2: Increase scroll amount

In `WebDriver.kt` and `CdpWebDriver.kt`:
```kotlin
executeJS("window.maestro.scrollFlutter(10, 1)")  // Changed from 5 to 10
```

### Solution 3: Use pixel mode for more control

```kotlin
executeJS("window.maestro.scrollFlutter(200, 0)")  // 200 pixels, mode 0
```

### Solution 4: Check if Maestro script is injected

```yaml
- evalScript: |
    console.log('Maestro object exists:', !!window.maestro);
    console.log('isFlutterApp exists:', typeof window.maestro?.isFlutterApp);
    console.log('scrollFlutter exists:', typeof window.maestro?.scrollFlutter);
```

## Verifying the Fix

### Success Indicators

✅ All these should appear in logs:

1. **Flutter detected:**
   ```
   [Maestro] Flutter detection: { isFlutter: true, ... }
   ```

2. **Events dispatched:**
   ```
   [Maestro] Mouseover event dispatched: true
   [Maestro] Mousemove event dispatched: true
   [Maestro] Wheel event dispatched: true
   ```

3. **Events received:**
   ```
   [Test Page] ===== WHEEL EVENT RECEIVED =====
   ```

4. **Content scrolled:**
   ```
   [Test Page] Transform applied: translateY(-XXXpx)
   ```

5. **Assertions pass:**
   ```
   ✓ Assert visible: "Section 2"
   ✓ Assert visible: "Section 3 - Middle"
   ✓ Assert visible: "BOTTOM OF PAGE"
   ```

## Getting More Help

### Collect this information:

1. **Full console log** from browser DevTools
2. **Maestro output** from terminal
3. **Browser and version** (Chrome, Firefox, etc.)
4. **Operating system**
5. **Screen/viewport size**

### Share logs showing:

- Flutter detection result
- First scroll attempt (all events)
- Transform values before/after scroll
- Any JavaScript errors

### Test with minimal example:

```yaml
url: http://localhost:8088/test_flutter_scroll.html
---
- launchApp
- evalScript: |
    console.log('=== MINIMAL TEST ===');
    console.log('1. Is Flutter?', window.maestro.isFlutterApp());
    console.log('2. Scrolling...');
    const result = window.maestro.scrollFlutter(5, 1);
    console.log('3. Result:', result);
    const transform = document.getElementById('flutter-content').style.transform;
    console.log('4. Transform:', transform);
- assertVisible: "TOP OF PAGE"
```

## Advanced Debugging

### Enable Selenium logs

In `WebDriver.kt`, you could add more logging in the `scrollVertical()` method.

### Check event listeners

```javascript
// In browser console
getEventListeners(document.querySelector('flutter-view'))
// Should show wheel, mouseover, mousemove listeners
```

### Monitor all events

```javascript
// Add to test page temporarily
window.addEventListener('wheel', (e) => {
    console.log('[Window] Wheel event at window level', e);
}, { capture: true });
```

---

**Remember:** The key is to follow the event flow:
1. Maestro detects Flutter ✓
2. Maestro dispatches events ✓
3. Test page receives events ✓
4. Test page applies transform ✓
5. Content becomes visible ✓
6. Assertions pass ✓

Find where the chain breaks and fix that step!

