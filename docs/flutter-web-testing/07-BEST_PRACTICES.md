# Best Practices

> **Patterns, conventions, and tips for maintainable Flutter Web tests**

---

## Project Structure

### Recommended Layout

```
your-flutter-project/
├── lib/                              # Flutter source
├── web/                              # Web assets
├── maestro/                          # Test directory
│   ├── config.yaml                   # Workspace config
│   ├── flows/                        # Test flows by feature
│   │   ├── auth/
│   │   │   ├── login.yaml
│   │   │   ├── logout.yaml
│   │   │   └── password_reset.yaml
│   │   ├── dashboard/
│   │   │   ├── dashboard_load.yaml
│   │   │   └── widgets_test.yaml
│   │   └── smoke_test.yaml
│   ├── subflows/                     # Reusable components
│   │   ├── common_login.yaml
│   │   ├── common_logout.yaml
│   │   └── common_navigation.yaml
│   └── scripts/                      # JavaScript helpers
│       ├── generate_data.js
│       └── api_setup.js
└── test_results/                     # Output directory
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Flow files | `feature_action.yaml` | `login_success.yaml` |
| Subflows | `common_action.yaml` | `common_login.yaml` |
| Screenshots | `step_description` | `after_login_click` |
| Variables | `SCREAMING_SNAKE_CASE` | `USER_EMAIL` |
| Flutter IDs | `snake_case` | `submit_button` |

---

## Writing Maintainable Tests

### DO: Use Descriptive Names

```yaml
# ✅ Good - Clear purpose
name: User Login with Valid Credentials
tags:
  - smoke
  - authentication
  - P1
```

```yaml
# ❌ Bad - Unclear
name: test1
```

### DO: Add Labels to Complex Commands

```yaml
# ✅ Good - Self-documenting
- tapOn:
    flutterId: submit_button
    label: "Submit the login form"

- waitForAnimationToEnd:
    timeout: 3000
    label: "Wait for dashboard to load"
```

### DO: Use Subflows for Repeated Steps

```yaml
# ✅ Good - Reusable login
# subflows/common_login.yaml
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

```yaml
# flows/dashboard_test.yaml
- runFlow: subflows/common_login.yaml
- assertVisible: "Dashboard"
```

### DON'T: Duplicate Login Steps Everywhere

```yaml
# ❌ Bad - Duplicated across files
- tapOn: "Enter your User ID"
- inputText: "testuser"
- tapOn: "Enter your password"  
- inputText: "password123"
# ... repeated in every test file
```

---

## Selector Best Practices

### Priority Order

```
1. Visible Text      →  Most readable
2. Flutter ID        →  Most stable
3. Aria Label        →  Accessibility-friendly
4. Relative Position →  When needed
```

### DO: Prefer Text for Simple Cases

```yaml
# ✅ Good - Human readable
- tapOn: "Login"
- tapOn: "Submit Order"
- assertVisible: "Order Confirmed"
```

### DO: Use IDs for Critical Elements

```yaml
# ✅ Good - Stable, won't break if text changes
- tapOn:
    flutterId: checkout_button

- tapOn:
    flutterId: payment_submit
```

### DON'T: Use Brittle Selectors

```yaml
# ❌ Bad - Positional, fragile
- tapOn:
    point: "234,567"
```

### DO: Add Comments for Complex Selectors

```yaml
# Select the Edit button in the third row
- tapOn:
    text: "Edit"
    index: 2
    below: "Items List"
```

---

## Handling Dynamic Content

### Wait for Loading States

```yaml
# ✅ Good - Wait for loading to complete
- tapOn: "Load Data"
- assertNotVisible: "Loading..."
- assertVisible: "Data Loaded"
```

### Use Regex for Dynamic Text

```yaml
# ✅ Good - Handles dynamic values
- assertVisible: "Order #[A-Z0-9]+"
- assertVisible: "Total: \\$[0-9]+\\.[0-9]{2}"
```

### Handle Optional Elements

```yaml
# ✅ Good - Don't fail on optional popups
- tapOn:
    text: "Dismiss"
    optional: true

- runFlow:
    file: dismiss_cookie_banner.yaml
    when:
      visible: "Accept Cookies"
```

---

## Test Independence

### Each Test Should Stand Alone

```yaml
# ✅ Good - Complete flow, no dependencies
url: http://localhost:4446
---
- launchApp
- runFlow: subflows/common_login.yaml   # Sets up state
- tapOn: "Dashboard"
- assertVisible: "Welcome"
- runFlow: subflows/common_logout.yaml  # Cleans up
```

### DON'T: Rely on Test Order

```yaml
# ❌ Bad - Assumes previous test logged in
---
- tapOn: "Dashboard"   # Will fail if run alone
```

### Use Fresh Data

```yaml
# ✅ Good - Generate unique data
- runScript: scripts/generate_user.js
- tapOn: "Email"
- inputText: "${output.email}"   # Unique per run
```

---

## Screenshot Strategy

### Key Checkpoints

```yaml
# ✅ Good - Screenshot at important states
- launchApp
- takeScreenshot: 01_initial_load

- tapOn: "Login"
- takeScreenshot: 02_login_form

- inputText: "${USERNAME}"
- tapOn: "Submit"
- takeScreenshot: 03_after_submit

- assertVisible: "Dashboard"
- takeScreenshot: 04_dashboard_loaded
```

### Naming Convention

```yaml
# Use numbered prefixes for ordering
- takeScreenshot: 01_login_screen
- takeScreenshot: 02_credentials_entered
- takeScreenshot: 03_dashboard_visible
```

### Failure Evidence

```yaml
# Capture state before critical actions
- takeScreenshot: before_checkout
- tapOn: "Complete Purchase"
- takeScreenshot: after_checkout
```

---

## Error Handling

### Use Retry for Flaky Operations

```yaml
- retry:
    maxRetries: 3
    commands:
      - tapOn: "Submit"
      - assertVisible: "Success"
```

### Make Non-Critical Steps Optional

```yaml
# Won't fail the test
- tapOn:
    text: "Close Promo"
    optional: true
```

### Add Timeouts for Slow Operations

```yaml
- assertVisible:
    text: "Data Loaded"
    timeout: 30000    # 30 seconds for slow API
```

---

## Environment Management

### Use Variables for Configurable Values

```yaml
# config.yaml
env:
  BASE_URL: http://localhost:4446
  DEFAULT_TIMEOUT: 5000
  TEST_USER: qa_tester@example.com
```

### Separate Configs per Environment

```yaml
# config.yaml (default - local)
env:
  BASE_URL: http://localhost:4446

# config.staging.yaml
env:
  BASE_URL: https://staging.myapp.com
```

```bash
# Run with different config
maestro test --config=config.staging.yaml flows/
```

### Secure Sensitive Data

```bash
# Pass secrets via CLI, not in files
maestro test \
  -e API_KEY=$API_KEY \
  -e PASSWORD=$TEST_PASSWORD \
  flows/
```

---

## Tag Organization

### Consistent Tag Strategy

```yaml
# Feature tag
tags:
  - authentication
  
# Test type tags  
tags:
  - smoke        # Quick sanity check
  - regression   # Full test suite
  - e2e          # End-to-end scenarios

# Priority tags
tags:
  - P1   # Critical path
  - P2   # Important
  - P3   # Nice to have

# Status tags
tags:
  - wip    # Work in progress
  - flaky  # Known flaky
```

### Running Tag Combinations

```bash
# Smoke tests only
maestro test --include-tags=smoke flows/

# P1 and P2, exclude flaky
maestro test --include-tags=P1,P2 --exclude-tags=flaky flows/
```

---

## Performance Tips

### Minimize Unnecessary Waits

```yaml
# ✅ Good - Maestro auto-waits
- tapOn: "Load"
- assertVisible: "Loaded"

# ❌ Bad - Unnecessary explicit wait
- tapOn: "Load"
- waitForAnimationToEnd:
    timeout: 5000    # Often not needed
- assertVisible: "Loaded"
```

### Use Headless Mode in CI

```bash
# Faster execution
maestro test --headless flows/
```

### Parallel Execution

```bash
# Run multiple workers
maestro test --parallel 4 flows/
```

---

## Code Review Checklist

### Before Committing Tests

- [ ] Test has a descriptive `name`
- [ ] Appropriate `tags` are assigned
- [ ] No hardcoded passwords (use env vars)
- [ ] Uses subflows for repeated steps
- [ ] Screenshots at key checkpoints
- [ ] Labels on complex commands
- [ ] Test runs independently
- [ ] No unnecessary waits
- [ ] Selectors are stable (prefer IDs over positional)

---

## Anti-Patterns to Avoid

### ❌ Hardcoded Credentials

```yaml
# Bad
- inputText: "admin@company.com"
- inputText: "SuperSecret123!"
```

```yaml
# Good
- inputText: "${ADMIN_EMAIL}"
- inputText: "${ADMIN_PASSWORD}"
```

### ❌ Positional Selectors

```yaml
# Bad - breaks on layout changes
- tapOn:
    point: "100,200"
```

```yaml
# Good - stable identifier
- tapOn:
    flutterId: submit_button
```

### ❌ Excessive Nesting

```yaml
# Bad - hard to read
- repeat:
    times: 3
    commands:
      - runFlow:
          file: nested.yaml
          when:
            visible: "Something"
```

```yaml
# Good - flatten with subflows
- runFlow: setup.yaml
- runFlow: test_action.yaml
- runFlow: verify.yaml
```

### ❌ Magic Numbers

```yaml
# Bad
- tapOn:
    text: "Item"
    index: 3
```

```yaml
# Good - add context
# Select the "Item" button in the payment methods section (4th item)
- tapOn:
    text: "Item"
    index: 3
    below: "Payment Methods"
```

---

## Summary

| Category | Do | Don't |
|----------|------|-------|
| **Structure** | Use subflows | Duplicate code |
| **Selectors** | Prefer text/ID | Use fragile selectors |
| **Data** | Use env vars | Hardcode secrets |
| **Waits** | Trust auto-wait | Add sleep everywhere |
| **Tests** | Keep independent | Rely on test order |
| **Screenshots** | At key points | After every command |
| **Tags** | Use consistently | Skip tagging |

---

**Previous:** [← Advanced Features](./06-ADVANCED_FEATURES.md) | **Next:** [Troubleshooting →](./08-TROUBLESHOOTING.md)
