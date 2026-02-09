# Getting Started with Maestro for Flutter Web

> **Install Maestro and run your first Flutter Web test in under 5 minutes**

---

## Prerequisites

Before you begin, ensure you have:

- [ ] **Java 17 or higher** installed
- [ ] **Chrome or Chromium** browser installed
- [ ] A **Flutter Web application** to test (local or hosted)

### Verify Java Version

```bash
java -version
# Should output: openjdk version "17.x.x" or higher
```

If you need to install Java:
- **macOS**: `brew install openjdk@17`
- **Ubuntu/Debian**: `sudo apt install openjdk-17-jdk`
- **Windows**: Download from [Adoptium](https://adoptium.net/)

---

## Step 1: Install Maestro

### macOS / Linux / Windows (WSL)

```bash
curl -fsSL "https://get.maestro.mobile.dev" | bash
```

### Windows (Native)

Download the installer from [maestro.dev](https://maestro.dev/) or use WSL for the best experience.

### Verify Installation

```bash
maestro --version
# Output: Maestro CLI version: 2.x.x
```

---

## Step 2: Create Your First Test

Create a file called `hello_flutter.yaml`:

```yaml
# hello_flutter.yaml
# Your first Flutter Web test

url: http://localhost:4446
---
# Launch the browser and navigate to the URL
- launchApp

# Verify the app loaded
- assertVisible: "Welcome"

# Take a screenshot for verification
- takeScreenshot: first_test_screenshot
```

---

## Step 3: Run the Test

### Start Your Flutter Web App

```bash
# In your Flutter project directory
flutter run -d chrome --web-port 4446
```

### Run Maestro Test

```bash
# In the directory with your test file
maestro test hello_flutter.yaml
```

### Expected Output

```
 ║
 ║  > Flow: hello_flutter.yaml
 ║
 ║    ✅ Launch app
 ║    ✅ Assert visible: "Welcome"
 ║    ✅ Take screenshot: first_test_screenshot
 ║
```

---

## Step 4: Understanding the Test Structure

Every Maestro test file has two parts:

```yaml
# ═══════════════════════════════════════════════════════════════
# PART 1: HEADER (Configuration)
# ═══════════════════════════════════════════════════════════════
url: http://localhost:4446          # Required for web tests
name: My Test Name                   # Optional: test name
tags:                                # Optional: for filtering
  - smoke
  - login

# ═══════════════════════════════════════════════════════════════
# PART 2: COMMANDS (separated by ---)
# ═══════════════════════════════════════════════════════════════
---
- launchApp
- tapOn: "Button Text"
- inputText: "Hello World"
- assertVisible: "Success"
```

---

## Step 5: A Real-World Example

Here's a complete login test for a Flutter Web app:

```yaml
# login_test.yaml
url: http://localhost:4446
name: User Login Flow
tags:
  - smoke
  - authentication

---
# Step 1: Launch and verify login screen
- launchApp
- assertVisible: "Login To Application"

# Step 2: Enter credentials
- tapOn: "Enter your User ID"
- inputText: "testuser@example.com"

- tapOn: "Enter your password"
- inputText: "SecurePassword123"

# Step 3: Submit login
- tapOn: "Login"

# Step 4: Wait for navigation
- waitForAnimationToEnd:
    timeout: 3000

# Step 5: Verify successful login
- assertVisible: "Dashboard"

# Step 6: Capture evidence
- takeScreenshot: login_success
```

---

## Running Multiple Tests

### Run All Tests in a Directory

```bash
maestro test maestro/
```

### Run Tests with Specific Tags

```bash
maestro test --include-tags=smoke maestro/
```

### Run in Headless Mode

```bash
maestro test --headless maestro/
```

### Run with Custom Screen Size

```bash
# Desktop (Full HD)
maestro test --headless --screen-size 1920x1080 maestro/

# Mobile viewport
maestro test --headless --screen-size 390x844 maestro/
```

---

## Test Output

By default, Maestro saves test results in the current directory. You can configure this:

```yaml
# config.yaml
testOutputDir: test_results
```

Output includes:
- **Screenshots**: PNG files for each `takeScreenshot` command
- **Videos**: If recording is enabled
- **Reports**: HTML/JUnit reports for CI integration

---

## Continuous Mode (Development)

While developing tests, use continuous mode to auto-run on file changes:

```bash
maestro test --continuous hello_flutter.yaml
```

Maestro will watch for changes and re-run automatically!

---

## Common First-Time Issues

### Issue: "Chrome not found"

**Solution**: Ensure Chrome/Chromium is installed and in your PATH.

```bash
# macOS
brew install --cask google-chrome

# Ubuntu
sudo apt install chromium-browser
```

### Issue: "Connection refused"

**Solution**: Ensure your Flutter app is running before starting the test.

```bash
# Terminal 1: Start Flutter app
flutter run -d chrome --web-port 4446

# Terminal 2: Run Maestro test
maestro test hello_flutter.yaml
```

### Issue: "Element not found"

**Solution**: The element text might be different. Use `maestro hierarchy` to inspect:

```bash
# View the current element hierarchy
maestro hierarchy
```

---

## Next Steps

| Topic | Document |
|-------|----------|
| Selenium comparison | [Selenium to Maestro →](./02-SELENIUM_TO_MAESTRO.md) |
| Flutter-specific setup | [Flutter Web Setup →](./03-FLUTTER_WEB_SETUP.md) |
| All commands | [Commands Reference →](./04-COMMANDS_REFERENCE.md) |

---

## Quick Reference Card

```yaml
# ─────────────────────────────────────────────────
# ESSENTIAL COMMANDS CHEAT SHEET
# ─────────────────────────────────────────────────

# Launch app (required first command)
- launchApp

# Tap on element by text
- tapOn: "Button Text"

# Tap on element by ID
- tapOn:
    id: "button_id"

# Input text (element must be focused)
- inputText: "Hello World"

# Assert element is visible
- assertVisible: "Expected Text"

# Assert element is NOT visible
- assertNotVisible: "Error Message"

# Wait for animations
- waitForAnimationToEnd:
    timeout: 2000

# Take screenshot
- takeScreenshot: screenshot_name

# Scroll down
- scroll

# Navigate back
- back
```

---

**Previous:** [← README](./README.md) | **Next:** [Selenium to Maestro →](./02-SELENIUM_TO_MAESTRO.md)
