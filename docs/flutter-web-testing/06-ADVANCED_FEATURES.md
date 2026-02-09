# Advanced Features

> **Loops, conditions, JavaScript, hooks, and more**

---

## Subflows (Reusable Components)

### Creating a Subflow

```yaml
# subflows/login.yaml
---
- tapOn: "Enter your User ID"
- inputText: "${USERNAME}"
- tapOn: "Enter your password"
- inputText: "${PASSWORD}"
- tapOn:
    flutterId: submit_button
- waitForAnimationToEnd:
    timeout: 2000
```

### Using a Subflow

```yaml
# tests/dashboard_test.yaml
url: http://localhost:4446
env:
  USERNAME: testuser
  PASSWORD: secret123
---
- launchApp
- runFlow: subflows/login.yaml
- assertVisible: "Dashboard"
```

### Subflow with Condition

```yaml
# Only run if not already logged in
- runFlow:
    file: subflows/login.yaml
    when:
      visible: "Login"
```

### Passing Variables to Subflows

```yaml
# tests/admin_test.yaml
- runFlow:
    file: subflows/login.yaml
    env:
      USERNAME: admin
      PASSWORD: adminpass
```

---

## Loops (repeat)

### Fixed Iterations

```yaml
# Add 5 items to cart
- repeat:
    times: 5
    commands:
      - tapOn: "Add to Cart"
      - waitForAnimationToEnd
```

### Conditional Loop

```yaml
# Keep clicking "Next" while it's visible
- repeat:
    while:
      visible: "Next"
    commands:
      - tapOn: "Next"
      - waitForAnimationToEnd:
          timeout: 1000
```

### Loop with Counter

```yaml
# Use JavaScript to track iterations
- evalScript: ${counter = 0}
- repeat:
    times: 3
    commands:
      - evalScript: ${counter = counter + 1}
      - tapOn: "Item ${counter}"
```

---

## Conditions

### Run Flow When Visible

```yaml
- runFlow:
    file: dismiss_popup.yaml
    when:
      visible: "Cookie Consent"
```

### Run Flow When NOT Visible

```yaml
- runFlow:
    file: login.yaml
    when:
      notVisible: "Dashboard"
```

### JavaScript Condition

```yaml
# Based on environment variable
- runFlow:
    file: admin_tests.yaml
    when:
      true: "${USER_ROLE == 'admin'}"

# Based on computed value
- runFlow:
    file: handle_error.yaml
    when:
      true: "${output.status != 'success'}"
```

### Platform Condition

```yaml
# Only run on web (useful for cross-platform projects)
- runFlow:
    file: web_specific.yaml
    when:
      platform: Web
```

---

## JavaScript Integration

### Inline JavaScript (evalScript)

```yaml
# Simple calculation
- evalScript: ${output.total = 10 * 5}

# String manipulation
- evalScript: ${output.greeting = "Hello " + output.username}

# Date/time
- evalScript: ${output.timestamp = new Date().toISOString()}

# Conditional logic
- evalScript: |
    ${
      if (output.count > 10) {
        output.message = "High volume"
      } else {
        output.message = "Normal"
      }
    }
```

### External JavaScript Files (runScript)

Create a JavaScript file:

```javascript
// scripts/generate_data.js

// Access environment variables
const baseUrl = process.env.BASE_URL || 'http://localhost';

// Generate random data
const randomEmail = `user_${Date.now()}@test.com`;
const randomId = Math.random().toString(36).substring(7);

// Set output variables (available in YAML as ${output.X})
output.email = randomEmail;
output.userId = randomId;
output.timestamp = new Date().toISOString();

// Console log for debugging
console.log('Generated email:', randomEmail);
```

Use in YAML:

```yaml
- runScript: scripts/generate_data.js

- tapOn: "Email Field"
- inputText: "${output.email}"
```

### DataFaker (Random Data Generation)

Maestro includes DataFaker for generating realistic test data:

```javascript
// scripts/faker_example.js

// DataFaker is available globally
const faker = new Faker();

output.name = faker.name().fullName();
output.email = faker.internet().emailAddress();
output.phone = faker.phoneNumber().cellPhone();
output.address = faker.address().streetAddress();
output.company = faker.company().name();
```

```yaml
- runScript: scripts/faker_example.js
- inputText: "${output.name}"
```

---

## Retry on Failure

### Basic Retry

```yaml
- retry:
    maxRetries: 3
    commands:
      - tapOn: "Flaky Button"
      - assertVisible: "Success"
```

### Retry Subflow

```yaml
- retry:
    maxRetries: 2
    file: subflows/submit_form.yaml
```

---

## Hooks: onFlowStart and onFlowComplete

Define commands that run before/after every flow in a workspace.

### Workspace Config

```yaml
# config.yaml
onFlowStart:
  - runScript: scripts/setup.js
  - takeScreenshot: flow_start

onFlowComplete:
  - takeScreenshot: flow_end
  - runScript: scripts/cleanup.js
```

### Use Cases

**onFlowStart:**
- Set up test data
- Clear previous state
- Log test start

**onFlowComplete:**
- Capture final screenshot
- Clean up test data
- Log results

---

## Environment Variables

### Define in Flow Header

```yaml
url: http://localhost:4446
env:
  USERNAME: testuser
  PASSWORD: secret123
  TIMEOUT: 5000
---
- inputText: "${USERNAME}"
```

### Define in config.yaml

```yaml
# config.yaml
env:
  BASE_URL: http://localhost:4446
  DEFAULT_USER: testuser
```

### Pass at Runtime

```bash
maestro test -e USERNAME=admin -e PASSWORD=adminpass flow.yaml
```

### Access in JavaScript

```javascript
// scripts/example.js
const username = process.env.USERNAME || 'default';
const baseUrl = process.env.BASE_URL;
```

### Variable Precedence

1. CLI arguments (`-e`) - highest priority
2. Flow-level `env:`
3. config.yaml `env:`
4. System environment variables

---

## Copying Text from Elements

### Copy and Use

```yaml
# Copy text from element
- copyTextFrom:
    id: "order-number"

# Text is available in maestro.copiedText
- assertTrue: "${maestro.copiedText != ''}"

# Or assign to custom variable
- evalScript: ${output.orderNumber = maestro.copiedText}
```

### Practical Example

```yaml
# Copy confirmation code
- copyTextFrom: "Confirmation: [A-Z0-9]+"

# Use it later
- tapOn: "Enter Code"
- inputText: "${maestro.copiedText}"
```

---

## Tags for Test Organization

### Define Tags

```yaml
url: http://localhost:4446
name: Login Test
tags:
  - smoke
  - authentication
  - P1
---
```

### Run by Tags

```bash
# Include specific tags
maestro test --include-tags=smoke flows/

# Exclude tags
maestro test --exclude-tags=wip,flaky flows/

# Multiple include tags (OR logic)
maestro test --include-tags=smoke,regression flows/
```

### Tag Strategy

| Tag | Purpose |
|-----|---------|
| `smoke` | Quick sanity tests |
| `regression` | Full test suite |
| `P1`, `P2`, `P3` | Priority levels |
| `wip` | Work in progress |
| `flaky` | Known flaky tests |
| `auth` | Authentication tests |
| `feature-x` | Feature-specific |

---

## Test Output Configuration

### Configure Output Directory

```yaml
# config.yaml
testOutputDir: test_results
```

### CLI Flag

```bash
maestro test --output=./results flows/
```

### Output Contents

```
test_results/
├── 2024-01-15_143022/
│   ├── flow_login_test/
│   │   ├── screenshots/
│   │   │   ├── screenshot_1.png
│   │   │   └── screenshot_2.png
│   │   └── recording.mp4
│   ├── report.html
│   └── junit.xml
```

---

## Parallel Test Execution

### Run in Parallel (CLI)

```bash
# Run with multiple workers
maestro test --parallel 4 flows/
```

### Considerations

- Each worker runs a separate browser instance
- Tests should be independent (no shared state)
- Use unique test data per flow

---

## Advanced Selectors with JavaScript

### Dynamic Selector

```yaml
- evalScript: ${output.buttonText = "Submit " + output.formName}
- tapOn: "${output.buttonText}"
```

### Computed Values

```yaml
- evalScript: ${output.index = Math.floor(Math.random() * 5)}
- tapOn:
    text: "Item"
    index: ${output.index}
```

---

## Error Handling

### Optional Commands

```yaml
# Won't fail if element not found
- tapOn:
    text: "Dismiss"
    optional: true
```

### Continue on Failure

```yaml
- runFlow:
    file: optional_step.yaml
    optional: true
```

### Retry Pattern

```yaml
- retry:
    maxRetries: 3
    commands:
      - tapOn: "Submit"
      - assertVisible: "Success"
```

---

## Best Practices

### 1. Organize with Subflows

```
maestro/
├── flows/
│   ├── smoke_test.yaml
│   └── full_regression.yaml
├── subflows/
│   ├── login.yaml
│   ├── logout.yaml
│   └── navigate_to_settings.yaml
└── scripts/
    └── generate_test_data.js
```

### 2. Use Meaningful Variable Names

```yaml
env:
  TEST_USER_EMAIL: qa_tester@example.com
  TEST_USER_PASSWORD: SecurePass123
  EXPECTED_DASHBOARD_TITLE: "User Dashboard"
```

### 3. Add Labels for Clarity

```yaml
- tapOn:
    flutterId: submit_button
    label: "Submit the registration form"
```

### 4. Use Tags Consistently

```yaml
tags:
  - smoke           # Test type
  - authentication  # Feature area
  - P1              # Priority
```

---

**Previous:** [← Element Selection](./05-ELEMENT_SELECTION.md) | **Next:** [Best Practices →](./07-BEST_PRACTICES.md)
