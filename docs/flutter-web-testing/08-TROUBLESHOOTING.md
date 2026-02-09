# Troubleshooting Guide

> **Common issues and solutions for Flutter Web testing with Maestro**

---

## Quick Diagnostics

### Check Maestro Installation

```bash
maestro --version
# Expected: Maestro CLI version: 2.x.x
```

### Check Java Version

```bash
java -version
# Required: 17 or higher
```

### View Element Hierarchy

```bash
# See what Maestro can detect on current screen
maestro hierarchy
```

### Enable Debug Output

```bash
maestro test --debug flow.yaml
```

---

## Common Issues

### Issue: "Chrome not found"

<details>
<summary><strong>❌ Error: Cannot find Chrome/Chromium browser</strong></summary>

**Symptoms:**
- Test fails to start
- "Chrome not found" or "No browser available" error

**Cause:**
Chrome or Chromium is not installed or not in PATH.

**Solution:**

```bash
# macOS
brew install --cask google-chrome

# Ubuntu/Debian
sudo apt install chromium-browser

# Verify installation
which chromium || which google-chrome
```

</details>

---

### Issue: "Connection refused"

<details>
<summary><strong>❌ Error: Connection refused to localhost:XXXX</strong></summary>

**Symptoms:**
- "Connection refused" error
- Browser opens but shows error page

**Cause:**
Flutter Web app is not running at the specified URL.

**Solution:**

```bash
# Terminal 1: Start Flutter app first
cd your-flutter-project
flutter run -d chrome --web-port 4446

# Wait for "Running on http://localhost:4446"

# Terminal 2: Then run Maestro
maestro test flow.yaml
```

**Verify app is running:**
```bash
curl -I http://localhost:4446
# Should return HTTP 200
```

</details>

---

### Issue: "Element not found"

<details>
<summary><strong>❌ Error: Unable to find element</strong></summary>

**Symptoms:**
- "Unable to find element" after timeout
- Test hangs then fails

**Causes & Solutions:**

**1. Text doesn't match exactly**
```bash
# Check actual text in hierarchy
maestro hierarchy | grep -i "login"
```

```yaml
# Wrong - case mismatch
- tapOn: "login"

# Correct
- tapOn: "Login"
```

**2. Element not visible yet**
```yaml
# Add wait for animation
- waitForAnimationToEnd:
    timeout: 3000
- tapOn: "Target Element"
```

**3. Element is inside loading state**
```yaml
# Wait for loading to complete
- assertNotVisible: "Loading..."
- tapOn: "Target Element"
```

**4. Using wrong selector**
```yaml
# If text doesn't work, try ID
- tapOn:
    id: "element-id"

# Or use Flutter ID
- tapOn:
    flutterId: element_id
```

</details>

---

### Issue: Custom Flutter IDs not working

<details>
<summary><strong>❌ Error: flutterId selector not matching</strong></summary>

**Symptoms:**
- `flutterId: xxx` not finding elements
- Works with text but not custom ID

**Cause:**
`selectorAliases` not configured or not matching.

**Solution:**

**1. Check config.yaml exists:**
```yaml
# maestro/config.yaml
platform:
  web:
    selectorAliases:
      flt-semantics-identifier: flutterId
      aria-label: ariaLabel
```

**2. Verify Flutter widget has identifier:**
```dart
Semantics(
  identifier: 'submit_button',  // This becomes flt-semantics-identifier
  child: ElevatedButton(...)
)
```

**3. Check hierarchy for attribute:**
```bash
maestro hierarchy | grep "flt-semantics"
# Should show: flt-semantics-identifier: submit_button
```

**4. Run from correct directory:**
```bash
# config.yaml must be in workspace root
cd maestro/
maestro test flows/test.yaml
```

</details>

---

### Issue: Tests pass locally, fail in CI

<details>
<summary><strong>❌ Tests work locally but fail in CI/CD</strong></summary>

**Symptoms:**
- Green locally, red in GitHub Actions/Jenkins
- Timeout errors in CI

**Causes & Solutions:**

**1. Headless mode differences**
```bash
# Test locally in headless mode
maestro test --headless flow.yaml
```

**2. Timing issues**
```yaml
# Add more generous timeouts for CI
- assertVisible:
    text: "Dashboard"
    timeout: 30000  # 30 seconds
```

**3. Screen size differences**
```yaml
# CI might have different viewport
# Ensure Flutter app handles responsive design
```

**4. Missing Chrome in CI**
```yaml
# GitHub Actions example
- name: Install Chrome
  uses: browser-actions/setup-chrome@latest
  
- name: Run Maestro Tests
  run: maestro test --headless flows/
```

</details>

---

### Issue: Scroll not working

<details>
<summary><strong>❌ Scroll commands don't move the page</strong></summary>

**Symptoms:**
- `scroll` command runs but page doesn't move
- `scrollUntilVisible` times out

**Cause:**
Flutter Web handles scrolling differently than regular web pages.

**Solution:**

```yaml
# Use swipe instead of scroll for Flutter
- swipe:
    direction: UP
    duration: 500

# Or scrollUntilVisible with longer timeout
- scrollUntilVisible:
    element: "Target Element"
    direction: DOWN
    timeout: 30000
    speed: 50
```

**For horizontal scrolling:**
```yaml
- swipe:
    direction: LEFT
    duration: 500
```

</details>

---

### Issue: inputText not typing

<details>
<summary><strong>❌ Input text command doesn't enter text</strong></summary>

**Symptoms:**
- `inputText` runs but field stays empty
- No error, just no text

**Cause:**
Input field is not focused.

**Solution:**

```yaml
# Always tap to focus first
- tapOn: "Enter your email"    # Focus the field
- inputText: "user@example.com" # Then type

# Or tap on placeholder text
- tapOn: "Email"
- inputText: "user@example.com"
```

**If still not working:**
```yaml
# Try tapping on the field by ID
- tapOn:
    flutterId: email_input
- inputText: "user@example.com"
```

</details>

---

### Issue: Test runs too slowly

<details>
<summary><strong>❌ Tests take very long to complete</strong></summary>

**Symptoms:**
- Tests run much slower than expected
- Timeouts everywhere

**Causes & Solutions:**

**1. Remove unnecessary waits**
```yaml
# Bad - explicit waits slow things down
- tapOn: "Button"
- waitForAnimationToEnd:
    timeout: 5000
- assertVisible: "Result"

# Good - Maestro auto-waits
- tapOn: "Button"
- assertVisible: "Result"
```

**2. Use headless mode**
```bash
maestro test --headless flows/
```

**3. Reduce screenshot frequency**
```yaml
# Only at key checkpoints
- takeScreenshot: login_complete  # Keep
# - takeScreenshot: every_step    # Remove
```

**4. Run in parallel**
```bash
maestro test --parallel 4 flows/
```

</details>

---

### Issue: "YAML syntax error"

<details>
<summary><strong>❌ Error: Invalid YAML syntax</strong></summary>

**Symptoms:**
- Parse error on flow file
- Unexpected character errors

**Common mistakes:**

**1. Incorrect indentation**
```yaml
# Wrong
- tapOn:
  id: button   # Should be 4 spaces, not 2

# Correct
- tapOn:
    id: button
```

**2. Missing quotes for special characters**
```yaml
# Wrong
- assertVisible: Total: $50.00

# Correct  
- assertVisible: "Total: $50.00"
```

**3. Tabs instead of spaces**
```yaml
# Wrong (tabs)
- tapOn:
	id: button

# Correct (spaces)
- tapOn:
    id: button
```

**Validate YAML:**
```bash
# Use online validator or
python -c "import yaml; yaml.safe_load(open('flow.yaml'))"
```

</details>

---

### Issue: Variable not substituted

<details>
<summary><strong>❌ ${VARIABLE} appears literally in output</strong></summary>

**Symptoms:**
- Variable shows as `${USERNAME}` instead of value
- "Element not found: ${USERNAME}"

**Causes & Solutions:**

**1. Variable not defined**
```yaml
# Define in header
env:
  USERNAME: testuser
---
- inputText: "${USERNAME}"
```

**2. Wrong syntax**
```yaml
# Wrong
- inputText: "$USERNAME"
- inputText: "{{USERNAME}}"

# Correct
- inputText: "${USERNAME}"
```

**3. Not passed via CLI**
```bash
# Pass with -e flag
maestro test -e USERNAME=testuser flow.yaml
```

</details>

---

## Debug Techniques

### 1. Add Screenshots for Debugging

```yaml
- takeScreenshot: debug_step_1
- tapOn: "Something"
- takeScreenshot: debug_step_2
```

### 2. Use Continuous Mode

```bash
# Auto-reruns on file change
maestro test --continuous flow.yaml
```

### 3. Check Hierarchy Interactively

```bash
# In one terminal, run your Flutter app
flutter run -d chrome --web-port 4446

# In another, inspect hierarchy
maestro hierarchy
```

### 4. Verbose Output

```bash
maestro test --debug flow.yaml 2>&1 | tee debug.log
```

### 5. Test Single Command

Create a minimal test file:
```yaml
url: http://localhost:4446
---
- launchApp
- takeScreenshot: debug_screen
```

---

## Error Messages Reference

| Error | Meaning | Solution |
|-------|---------|----------|
| `Unable to find element` | Element not on screen | Check text, add wait |
| `Connection refused` | App not running | Start Flutter app first |
| `Timeout waiting for...` | Element didn't appear in time | Increase timeout |
| `Invalid YAML` | Syntax error | Check indentation |
| `Chrome not found` | Browser missing | Install Chrome |
| `Java version...` | Wrong Java version | Install Java 17+ |

---

## Getting Help

### 1. Check Official Docs
- [docs.maestro.dev](https://docs.maestro.dev/)

### 2. Search Issues
- [GitHub Issues](https://github.com/mobile-dev-inc/maestro/issues)

### 3. Community Support
- [Slack Channel](https://maestrodev.typeform.com/to/FelIEe8A)

### 4. File a Bug Report
Include:
- Maestro version (`maestro --version`)
- Java version (`java -version`)
- OS and version
- Flow file (sanitized)
- Error output
- Steps to reproduce

---

## Quick Fixes Cheat Sheet

| Problem | Quick Fix |
|---------|-----------|
| Element not found | `maestro hierarchy` to check text |
| Timing issues | Add `waitForAnimationToEnd` |
| ID not working | Verify `config.yaml` selectorAliases |
| Slow tests | Use `--headless` mode |
| CI failures | Test with `--headless` locally |
| Scroll not working | Use `swipe` command |
| Input not typing | Add `tapOn` before `inputText` |

---

**Previous:** [← Best Practices](./07-BEST_PRACTICES.md) | **Back to:** [README](./README.md)
