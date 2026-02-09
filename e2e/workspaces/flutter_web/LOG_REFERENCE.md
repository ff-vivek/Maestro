# Flutter Scroll Test - Log Reference Card

## Quick Command

```bash
# Terminal 1: Start server
cd e2e/workspaces/flutter_web_identifier
python3 -m http.server 8088

# Terminal 2: Run test with browser open to see console
maestro test test_flutter_scroll_debug.yaml
```

## Expected Log Sequence

### 1️⃣ Page Load (First 1-2 seconds)

```
[Test Page] Script loading...
[Test Page] Elements found: {
  flutterView: true,
  content: true,
  contentHeight: 1536,
  windowHeight: 768
}
[Test Page] ==================================
[Test Page] Flutter web scroll test page loaded
[Test Page] Flutter view element: <flutter-view>
[Test Page] Has flt-glass-pane: true
[Test Page] Has flt-renderer attr: true
[Test Page] Ready for testing!
[Test Page] ==================================
```

**✅ What this means:** Page loaded successfully, Flutter elements detected

---

### 2️⃣ Flutter Detection Check

```
[Maestro] Flutter detection: {
  isFlutter: true,
  hasFlutterView: true,
  hasGlassPane: true,
  hasFltRenderer: true
}
```

**✅ What this means:** Maestro successfully identified this as a Flutter app

---

### 3️⃣ Scroll Command Execution

#### Maestro Side:
```
[Maestro] scrollFlutter called: { deltaY: 5, deltaMode: 1 }
[Maestro] Target element found: FLUTTER-VIEW
[Maestro] Viewport center: { x: 640, y: 384, width: 1280, height: 768 }
[Maestro] Mouseover event dispatched: true
[Maestro] Mousemove event dispatched: true
[Maestro] Wheel event dispatched: true
[Maestro] Wheel event details: {
  deltaY: 5,
  deltaMode: 1,
  clientX: 640,
  clientY: 384,
  bubbles: true,
  cancelable: true
}
```

**✅ What this means:** Maestro dispatched all three events successfully

#### Test Page Side:
```
[Test Page] Mouse over Flutter view at 640 384
[Test Page] ===== WHEEL EVENT RECEIVED =====
[Test Page] Event details: {
  deltaY: 5,
  deltaMode: 1,
  clientX: 640,
  clientY: 384,
  bubbles: true,
  cancelable: true,
  defaultPrevented: false
}
[Test Page] LINE mode: converted 5 lines to 80 pixels
[Test Page] Scroll update: {
  oldPosition: 0,
  newPosition: 80,
  maxScroll: 768,
  deltaApplied: 80
}
[Test Page] Transform applied: translateY(-80px)
[Test Page] ===== WHEEL EVENT COMPLETE =====
```

**✅ What this means:** 
- Page received the wheel event
- Converted 5 lines to 80 pixels
- Applied scroll transform
- Content moved 80px up

---

### 4️⃣ Periodic Status Updates

```
[Test Page] Current scroll position: 80
[Test Page] Current scroll position: 80
[Test Page] Current scroll position: 160  ← After 2nd scroll
[Test Page] Current scroll position: 240  ← After 3rd scroll
```

**✅ What this means:** Shows current scroll position every 2 seconds

---

## Error Patterns

### ❌ Flutter Not Detected

```
[Maestro] Flutter detection: {
  isFlutter: false,  ← Problem here
  hasFlutterView: false,
  hasGlassPane: false,
  hasFltRenderer: false
}
```

**🔧 Fix:** 
- Page not loaded yet
- Add wait/delay before test
- Check HTML has `<flutter-view>` element

---

### ❌ Element Not Found

```
[Maestro] scrollFlutter called: { deltaY: 5, deltaMode: 1 }
[Maestro] Flutter root element not found!  ← Problem here
[Maestro] Available elements: { body: true, allElements: 10 }
Error: Flutter root element not found
```

**🔧 Fix:**
- Elements removed after detection
- Race condition in page loading
- Add delay before scroll

---

### ❌ Events Not Received

```
[Maestro] Mouseover event dispatched: true
[Maestro] Mousemove event dispatched: true
[Maestro] Wheel event dispatched: true
← But no [Test Page] logs
```

**🔧 Fix:**
- Event listeners not attached
- Wrong target element
- Event not bubbling

---

### ❌ No Transform Applied

```
[Test Page] ===== WHEEL EVENT RECEIVED =====
[Test Page] LINE mode: converted 5 lines to 80 pixels
[Test Page] Scroll update: { ... }
[Test Page] Transform applied: none  ← Problem here
```

**🔧 Fix:**
- CSS issue
- Content element not found
- JavaScript error in transform application

---

### ❌ Assertion Failure (Content Not Visible)

```
[Test Page] Transform applied: translateY(-80px)  ← Scroll worked
✗ Assert visible: "Section 2"  ← But assertion failed
```

**🔧 Fix:**
- Didn't scroll far enough (80px not enough)
- Increase deltaY from 5 to 10+
- Or increase number of scrolls
- Check section positions in HTML

---

## Minimal Test Output

If everything works, you should see:

```
✓ Launch app
[Test Page] Flutter web scroll test page loaded
[Maestro] Flutter detection: { isFlutter: true, ... }

✓ Assert visible: "TOP OF PAGE - Start Scrolling Test"
✓ Assert visible: "Section 1"

[Maestro] scrollFlutter called: { deltaY: 5, deltaMode: 1 }
[Test Page] ===== WHEEL EVENT RECEIVED =====
[Test Page] Transform applied: translateY(-80px)

✓ Scroll
✓ Assert visible: "Section 2"

[Maestro] scrollFlutter called: { deltaY: 5, deltaMode: 1 }
[Test Page] Transform applied: translateY(-160px)

✓ Scroll
✓ Assert visible: "Section 3 - Middle"

... (continues for all scrolls)

✓ Assert visible: "BOTTOM OF PAGE - Scroll Test Complete!"
✓ Test passed
```

---

## Key Metrics

| Metric | Expected Value | What It Means |
|--------|---------------|---------------|
| `contentHeight` | ~1536px | Total scrollable height |
| `windowHeight` | 768px (varies) | Visible viewport |
| `maxScroll` | 768px | Max scroll distance |
| `deltaY` (LINE) | 5 | Number of lines to scroll |
| `deltaY` (pixels) | 80px | 5 lines × 16px/line |
| Events dispatched | 3 (mouseover, mousemove, wheel) | All should be `true` |
| Transform after 1st scroll | `translateY(-80px)` | Negative = scrolled down |
| Transform after 2nd scroll | `translateY(-160px)` | Double the first |

---

## Log Markers to Search For

Use these to quickly find issues:

🔍 **Find detection result:**
```bash
grep "Flutter detection" [log-file]
```

🔍 **Find scroll attempts:**
```bash
grep "scrollFlutter called" [log-file]
```

🔍 **Find events received:**
```bash
grep "WHEEL EVENT RECEIVED" [log-file]
```

🔍 **Find transforms applied:**
```bash
grep "Transform applied" [log-file]
```

🔍 **Find errors:**
```bash
grep -i "error\|failed\|not found" [log-file]
```

---

## Browser Console Tips

### Open browser for manual testing:
```bash
open http://localhost:8088/test_flutter_scroll.html
# Then press F12 or Cmd+Option+I
```

### Test manually in console:
```javascript
// 1. Check if loaded
window.maestro

// 2. Check Flutter detection
window.maestro.isFlutterApp()

// 3. Try scrolling
window.maestro.scrollFlutter(5, 1)

// 4. Check result
document.getElementById('flutter-content').style.transform
```

### Clear and retry:
```javascript
// Reset scroll
document.getElementById('flutter-content').style.transform = 'translateY(0px)';

// Try again
window.maestro.scrollFlutter(5, 1);
```

---

## Success Checklist

Use this to verify everything is working:

- [ ] Server running on port 8088
- [ ] Page loads (see "Ready for testing!" message)
- [ ] Flutter detected (isFlutter: true)
- [ ] Elements found (flutter-view, flt-glass-pane)
- [ ] Scroll command runs without errors
- [ ] Events dispatched (all three show true)
- [ ] Events received (see "WHEEL EVENT RECEIVED")
- [ ] Transform applied (see translateY value)
- [ ] Transform value changes with each scroll
- [ ] Assertions pass (all ✓ marks)

If all items are checked, the feature is working! 🎉

---

## One-Line Health Check

Paste this in browser console after page loads:

```javascript
console.log('Health Check:', {
  maestroLoaded: !!window.maestro,
  isFlutter: window.maestro?.isFlutterApp?.(),
  flutterView: !!document.querySelector('flutter-view'),
  content: !!document.getElementById('flutter-content'),
  canScroll: (() => { try { window.maestro?.scrollFlutter?.(1, 1); return true; } catch(e) { return e.message; } })()
});
```

Expected output:
```javascript
Health Check: {
  maestroLoaded: true,
  isFlutter: true,
  flutterView: true,
  content: true,
  canScroll: true
}
```

