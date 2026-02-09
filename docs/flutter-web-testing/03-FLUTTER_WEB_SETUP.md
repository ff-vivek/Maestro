# Flutter Web Setup

> **Configure Maestro for optimal Flutter Web testing**

---

## Flutter Web Detection

Maestro automatically detects Flutter Web applications by looking for:
- `<flutter-view>` element
- `<flt-glass-pane>` element
- `[flt-renderer]` attribute

No special configuration is needed for basic Flutter Web testing.

---

## Project Structure

Recommended structure for Flutter Web projects with Maestro:

```
your-flutter-project/
├── lib/                          # Flutter source code
├── web/                          # Web-specific files
├── maestro/                      # Maestro test directory
│   ├── config.yaml               # Workspace configuration
│   ├── flows/                    # Test flows
│   │   ├── auth/
│   │   │   ├── login.yaml
│   │   │   └── logout.yaml
│   │   ├── dashboard/
│   │   │   └── dashboard_test.yaml
│   │   └── smoke_test.yaml
│   └── subflows/                 # Reusable components
│       ├── common_login.yaml
│       └── common_navigation.yaml
├── test_results/                 # Test output
└── pubspec.yaml
```

---

## Workspace Configuration

Create `maestro/config.yaml` for workspace-wide settings:

```yaml
# maestro/config.yaml

# Custom test output directory
testOutputDir: ../test_results

# Platform-specific configuration
platform:
  web:
    # Map Flutter semantic attributes to friendly names
    selectorAliases:
      flt-semantics-identifier: flutterId
      aria-label: ariaLabel
      data-testid: testId
```

---

## Browser Window Size

Maestro supports setting custom browser window sizes for web testing.

### Default Sizes

| Mode | Default Size |
|------|--------------|
| Headless | 1024 × 768 |
| Non-headless | Chrome's default |

### Setting Screen Size via CLI

Use the `--screen-size` option to set custom dimensions:

```bash
# Set custom screen size (format: WIDTHxHEIGHT)
maestro test --headless --screen-size 1920x1080 my_test.yaml

# Mobile viewport simulation
maestro test --headless --screen-size 375x812 my_test.yaml

# Tablet viewport
maestro test --headless --screen-size 1024x768 my_test.yaml
```

### Examples

```bash
# Full HD Desktop
maestro test --headless --screen-size 1920x1080 flows/

# HD Desktop  
maestro test --headless --screen-size 1366x768 flows/

# iPad
maestro test --headless --screen-size 1024x768 flows/

# iPhone 14 Pro
maestro test --headless --screen-size 393x852 flows/

# iPhone SE
maestro test --headless --screen-size 375x667 flows/
```

### Common Screen Sizes Reference

| Device | Size | Command |
|--------|------|---------|
| Desktop (Full HD) | 1920×1080 | `--screen-size 1920x1080` |
| Desktop (HD) | 1366×768 | `--screen-size 1366x768` |
| Laptop | 1440×900 | `--screen-size 1440x900` |
| Tablet (iPad Pro) | 1024×1366 | `--screen-size 1024x1366` |
| Tablet (iPad) | 1024×768 | `--screen-size 1024x768` |
| Mobile (iPhone 14 Pro) | 393×852 | `--screen-size 393x852` |
| Mobile (iPhone 14) | 390×844 | `--screen-size 390x844` |
| Mobile (iPhone SE) | 375×667 | `--screen-size 375x667` |
| Mobile (Android) | 412×915 | `--screen-size 412x915` |

### Responsive Testing Script

Create a shell script to test multiple screen sizes:

```bash
#!/bin/bash
# test_responsive.sh

SIZES=("1920x1080" "1366x768" "1024x768" "390x844" "375x667")
FLOW="flows/responsive_test.yaml"

for size in "${SIZES[@]}"; do
    echo "Testing at $size..."
    maestro test --headless --screen-size "$size" "$FLOW"
done
```

### Alternative: Runtime Resize with JavaScript

For non-headless mode, you can also resize dynamically within a test:

```yaml
url: https://your-flutter-app.com
---
- launchApp

# Resize browser window at runtime
- evalScript: ${resizeTo(1920, 1080)}
- waitForAnimationToEnd:
    timeout: 500
```

> **Note:** `resizeTo()` works best in non-headless mode. For headless testing, use `--screen-size` CLI option.

---

## Custom Identifiers (selectorAliases)

### The Problem

Flutter Web renders widgets with semantic attributes like:
- `flt-semantics-identifier` (from `Semantics` widget)
- `aria-label` (from `Semantics(label: ...)`)

These are verbose to use in tests without aliases.

### The Solution

Map these attributes to short, friendly names:

```yaml
# config.yaml
platform:
  web:
    selectorAliases:
      flt-semantics-identifier: flutterId
      aria-label: ariaLabel
```

Now use them naturally:

```yaml
# With aliases (clean)
- tapOn:
    flutterId: submit_button
    
- assertVisible:
    ariaLabel: "Welcome message"
```

---

## Adding Semantics to Flutter Widgets

For elements to be testable, add semantic identifiers in your Flutter code:

### Option 1: Semantics Widget

```dart
Semantics(
  identifier: 'submit_button',  // becomes flt-semantics-identifier
  label: 'Submit Form',         // becomes aria-label
  child: ElevatedButton(
    onPressed: _submit,
    child: Text('Submit'),
  ),
)
```

### Option 2: Key Property

```dart
ElevatedButton(
  key: Key('submit_button'),
  onPressed: _submit,
  child: Text('Submit'),
)
```

### Option 3: Semantic Label

```dart
ElevatedButton(
  onPressed: _submit,
  child: Semantics(
    label: 'Submit the form',
    child: Text('Submit'),
  ),
)
```

### Recommended Approach

Use `Semantics` widget with `identifier` for interactive elements:

```dart
// lib/widgets/custom_button.dart
class TestableButton extends StatelessWidget {
  final String testId;
  final String label;
  final VoidCallback onPressed;
  
  const TestableButton({
    required this.testId,
    required this.label,
    required this.onPressed,
  });
  
  @override
  Widget build(BuildContext context) {
    return Semantics(
      identifier: testId,
      label: label,
      button: true,
      child: ElevatedButton(
        onPressed: onPressed,
        child: Text(label),
      ),
    );
  }
}

// Usage
TestableButton(
  testId: 'login_submit',
  label: 'Login',
  onPressed: _handleLogin,
)
```

---

## Web Flow Configuration

### Basic Web Flow

```yaml
# flows/smoke_test.yaml
url: http://localhost:4446
name: Smoke Test
tags:
  - smoke
  - web
---
- launchApp
- assertVisible: "Welcome"
```

### Flow with Environment Variables

```yaml
# flows/login_test.yaml
url: http://localhost:4446
name: Login Test
env:
  USERNAME: testuser@example.com
  PASSWORD: TestPassword123
---
- launchApp
- tapOn: "Enter your User ID"
- inputText: "${USERNAME}"
- tapOn: "Enter your password"
- inputText: "${PASSWORD}"
- tapOn:
    flutterId: submit_button
```

### Passing Environment at Runtime

```bash
# Override environment variables
maestro test flows/login_test.yaml \
  -e USERNAME=admin@example.com \
  -e PASSWORD=AdminPass456
```

---

## Running Flutter Web with Maestro

### Development Mode

```bash
# Terminal 1: Start Flutter Web
cd your-flutter-project
flutter run -d chrome --web-port 4446

# Terminal 2: Run Maestro tests
cd your-flutter-project/maestro
maestro test flows/
```

### Against Built Web App

```bash
# Build the web app
flutter build web

# Serve it (using any HTTP server)
cd build/web
python -m http.server 4446

# Run tests
maestro test -e URL=http://localhost:4446 flows/
```

### Headless Mode (CI/CD)

```bash
maestro test --headless flows/
```

---

## Configuration Reference

### Complete config.yaml Example

```yaml
# maestro/config.yaml

# Output directory for test results
testOutputDir: ../test_results

# Flow execution settings
flows:
  # Include specific flows
  includeTags:
    - smoke
    - regression
  # Exclude flows with these tags
  excludeTags:
    - wip
    - flaky

# Platform configuration
platform:
  web:
    # Custom attribute mappings
    selectorAliases:
      # Flutter semantics
      flt-semantics-identifier: flutterId
      aria-label: ariaLabel
      # Custom data attributes
      data-testid: testId
      data-cy: cypressId

# Global environment variables
env:
  BASE_URL: http://localhost:4446
  DEFAULT_TIMEOUT: 5000
```

### Flow Header Options

```yaml
# Complete flow header example
url: http://localhost:4446           # Required: Base URL
name: My Test Flow                    # Optional: Flow name
tags:                                 # Optional: For filtering
  - smoke
  - authentication
env:                                  # Optional: Flow-level variables
  USER: testuser
  TIMEOUT: 3000
---
# Commands start here
- launchApp
```

---

## Selector Priority

When Maestro looks for elements, it checks in this order:

1. **Text content** - Visible text in the element
2. **resource-id** - Maps from `id`, `aria-label`, `name`, `title`, `data-testid`
3. **Custom identifiers** - From `selectorAliases` configuration

### Selector Examples

```yaml
# 1. By visible text (most common)
- tapOn: "Login"

# 2. By ID (resource-id)
- tapOn:
    id: "login_button"

# 3. By custom Flutter identifier
- tapOn:
    flutterId: submit_button

# 4. By aria-label
- tapOn:
    ariaLabel: "Submit form"

# 5. Combined selectors
- tapOn:
    text: "Submit"
    flutterId: submit_button
```

---

## Flutter Web Best Practices

### 1. Use Semantic Identifiers

```dart
// Good: Has testable identifier
Semantics(
  identifier: 'user_profile_button',
  child: IconButton(...)
)

// Bad: No identifier, relies on icon/position
IconButton(
  icon: Icon(Icons.person),
)
```

### 2. Consistent Naming Convention

```yaml
# config.yaml - Use a convention
platform:
  web:
    selectorAliases:
      flt-semantics-identifier: flutterId

# Flutter code - Use snake_case
Semantics(identifier: 'login_submit_button')
Semantics(identifier: 'user_email_input')
Semantics(identifier: 'dashboard_menu')
```

### 3. Wait for Animations

Flutter has many animations. Wait for them:

```yaml
- tapOn: "Navigate"
- waitForAnimationToEnd:
    timeout: 2000
- assertVisible: "New Screen"
```

### 4. Handle Loading States

```yaml
# Wait for loading to complete
- tapOn: "Load Data"
- assertNotVisible: "Loading..."
- assertVisible: "Data Loaded"
```

---

## Debugging Flutter Web Tests

### View Element Hierarchy

```bash
# See all elements on the current screen
maestro hierarchy
```

### Check What Maestro Sees

The hierarchy output shows:
- Element text
- Bounds (position/size)
- resource-id
- Custom attributes (from selectorAliases)

### Common Issues

| Issue | Solution |
|-------|----------|
| Element not found | Check text matches exactly, use `maestro hierarchy` |
| Custom ID not working | Verify `selectorAliases` in config.yaml |
| Clicks not registering | Add `waitForAnimationToEnd` before interaction |
| Test passes locally, fails in CI | Use `--headless` flag locally to match CI |

---

**Previous:** [← Selenium to Maestro](./02-SELENIUM_TO_MAESTRO.md) | **Next:** [Commands Reference →](./04-COMMANDS_REFERENCE.md)
