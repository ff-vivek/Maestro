# Maestro Testing Guide for Flutter Web Applications

## Table of Contents
1. [Introduction](#1-introduction)
2. [Maestro Element Selectors Overview](#2-maestro-element-selectors-overview)
3. [Flutter Web Accessibility Tags Reference](#3-flutter-web-accessibility-tags-reference)
4. [Mapping Maestro Selectors to Flutter Elements](#4-mapping-maestro-selectors-to-flutter-elements)
5. [Practical Testing Examples](#5-practical-testing-examples)
6. [Best Practices](#6-best-practices)
7. [Known Limitations](#7-known-limitations)
8. [Complete Reference Tables](#8-complete-reference-tables)

---

## 1. Introduction

This guide explains how to use [Maestro](https://maestro.mobile.dev/) to test Flutter web applications by understanding the accessibility tags and HTML elements that Flutter generates, and how Maestro can interact with them.

### What is Maestro?

Maestro is a mobile UI testing framework that works across iOS, Android, and Web platforms. While primarily designed for mobile, it has **limited web testing support** through browser automation.

### Flutter Web + Maestro

When Flutter apps run in the browser, they generate semantic HTML elements with ARIA attributes. Maestro can interact with these elements using various selectors based on:
- Text content
- Accessibility IDs
- Element states (enabled, checked, focused, selected)
- Relative positioning

---

## 2. Maestro Element Selectors Overview

### 2.1 Core Selectors

| Selector | Type | Description | Example |
|----------|------|-------------|---------|
| `text` | String/Regex | Matches visible text content | `text: "Submit"` |
| `id` | String/Regex | Matches accessibility ID | `id: "login_button"` |
| `index` | Number | Selects nth matching element (0-based) | `index: 0` |
| `point` | String | Taps at specific coordinates | `point: "50%,50%"` |

### 2.2 State-Based Selectors

| Selector | Type | Description | Flutter Mapping |
|----------|------|-------------|-----------------|
| `enabled` | Boolean | Element is interactive | `aria-disabled` attribute |
| `checked` | Boolean | Element is checked | `aria-checked` attribute |
| `focused` | Boolean | Element has focus | Browser focus state |
| `selected` | Boolean | Element is selected | `aria-selected` attribute |

### 2.3 Relative Position Selectors

| Selector | Description | Example |
|----------|-------------|---------|
| `below` | Element below another | `below: { text: "Username" }` |
| `above` | Element above another | `above: { text: "Password" }` |
| `leftOf` | Element to the left | `leftOf: { text: "Cancel" }` |
| `rightOf` | Element to the right | `rightOf: { text: "OK" }` |
| `childOf` | Direct child element | `childOf: { id: "form" }` |
| `containsChild` | Contains specific child | `containsChild: { text: "Icon" }` |

### 2.4 Dimension Selectors

| Selector | Type | Description |
|----------|------|-------------|
| `width` | Number | Match by width |
| `height` | Number | Match by height |
| `tolerance` | Number | Tolerance for dimension matching |

---

## 3. Flutter Web Accessibility Tags Reference

### 3.1 Base Structure

All Flutter semantic elements have this base structure:

```html
<flt-semantics
  id="flt-semantic-node-{id}"
  style="position: absolute; ..."
  [aria-* attributes]
  [role attribute]>
  [content]
</flt-semantics>
```

### 3.2 Common Flutter-Generated Tags

#### Button Elements

**Flutter:**
```dart
Semantics(
  button: true,
  label: 'Submit',
  enabled: true,
  child: ElevatedButton(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  role="button"
  id="flt-semantic-node-5"
  flt-tappable>
  Submit
</flt-semantics>
```

**Maestro Selectors:**
- `text: "Submit"`
- `id: "flt-semantic-node-5"`
- `enabled: true`

#### Checkbox Elements

**Flutter:**
```dart
Semantics(
  checked: true,
  label: 'I agree to terms',
  child: Checkbox(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  role="checkbox"
  aria-checked="true"
  aria-label="I agree to terms"
  flt-tappable>
</flt-semantics>
```

**Maestro Selectors:**
- `text: "I agree to terms"`
- `checked: true`

#### Radio Button Elements

**Flutter:**
```dart
Semantics(
  inMutuallyExclusiveGroup: true,
  checked: false,
  label: 'Option 1',
  child: Radio(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  role="radio"
  aria-checked="false"
  aria-label="Option 1"
  flt-tappable>
</flt-semantics>
```

**Maestro Selectors:**
- `text: "Option 1"`
- `checked: false`

#### Switch Elements

**Flutter:**
```dart
Semantics(
  toggled: true,
  label: 'Enable notifications',
  child: Switch(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  role="switch"
  aria-checked="true"
  aria-label="Enable notifications">
</flt-semantics>
```

**Maestro Selectors:**
- `text: "Enable notifications"`
- `checked: true`

#### Text Field Elements

**Flutter:**
```dart
Semantics(
  textField: true,
  label: 'Email address',
  value: 'user@example.com',
  child: TextField(...),
)
```

**Generated HTML:**
```html
<input
  type="text"
  id="flt-semantic-node-10"
  aria-label="Email address"
  value="user@example.com">
```

**Maestro Selectors:**
- `text: "Email address"` (for the label)
- `id: "flt-semantic-node-10"`
- Can use `inputText` command directly

#### Heading Elements

**Flutter:**
```dart
Semantics(
  header: true,
  headingLevel: 2,
  label: 'User Profile',
  child: Text('User Profile'),
)
```

**Generated HTML:**
```html
<h2 id="flt-semantic-node-15"
    style="margin: 0; padding: 0; ...">
  User Profile
</h2>
```

**Maestro Selectors:**
- `text: "User Profile"`
- `id: "flt-semantic-node-15"`

#### Link Elements

**Flutter:**
```dart
Semantics(
  link: true,
  linkUrl: 'https://flutter.dev',
  label: 'Learn more',
  child: TextButton(...),
)
```

**Generated HTML:**
```html
<a href="https://flutter.dev"
   id="flt-semantic-node-20"
   style="display: block; ...">
  Learn more
</a>
```

**Maestro Selectors:**
- `text: "Learn more"`
- `id: "flt-semantic-node-20"`

#### Image Elements

**Flutter:**
```dart
Semantics(
  image: true,
  label: 'Profile picture',
  child: Image.network(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  role="img"
  aria-label="Profile picture">
</flt-semantics>
```

**Maestro Selectors:**
- `text: "Profile picture"`
- Can match by `id`

#### Slider Elements

**Flutter:**
```dart
Semantics(
  slider: true,
  value: '50',
  child: Slider(...),
)
```

**Generated HTML:**
```html
<input
  type="range"
  role="slider"
  aria-valuenow="50"
  aria-valuemin="0"
  aria-valuemax="100">
</flt-semantics>
```

**Maestro Selectors:**
- Can match by state or position
- Limited direct slider interaction support

---

## 4. Mapping Maestro Selectors to Flutter Elements

### 4.1 Text-Based Matching

**Use Case:** Most common and reliable selector for Flutter web apps.

**Flutter Semantics:**
```dart
Semantics(
  label: 'Login',
  button: true,
  child: ElevatedButton(...),
)
```

**Maestro Test:**
```yaml
- tapOn: "Login"
```

or with more specificity:

```yaml
- tapOn:
    text: "Login"
    enabled: true
```

**Why it works:** Flutter renders semantic labels as either:
- `aria-label` attributes
- DOM text content
- Both

Maestro can read all of these.

### 4.2 Identifier-Based Matching

**Use Case:** When you need **stable, reliable** element identification across app rebuilds and test runs.

#### Understanding Flutter's Two ID Systems

Flutter web has **two types of identifiers**:

1. **Auto-generated `id`**: `flt-semantic-node-{number}` (e.g., `flt-semantic-node-42`)
   - ❌ **Unstable** - Changes between runs, rebuilds, and semantic tree updates
   - Automatically assigned by the web engine
   - Not reliable for testing

2. **Developer-defined `identifier`**: Custom string set via `Semantics(identifier: '...')`
   - ✅ **Stable** - Remains constant across runs and rebuilds
   - Mapped to `flt-semantics-identifier` attribute
   - Ideal for testing and cross-element relationships

#### Using the `identifier` Property

**Flutter Semantics:**
```dart
Semantics(
  identifier: 'submit_form_button',  // Stable identifier
  button: true,
  label: 'Submit',
  child: ElevatedButton(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  id="flt-semantic-node-100"                    <!-- Unstable -->
  flt-semantics-identifier="submit_form_button" <!-- Stable ✅ -->
  role="button">
  Submit
</flt-semantics>
```

#### Testing with Identifiers

**✅ Maestro Now Supports `flt-semantics-identifier`!** Maestro's `id` selector now checks the `flt-semantics-identifier` attribute first, then falls back to the `id` attribute.

**Recommended Approach:**

1. **Use the `id` selector with Flutter identifiers** ✅ **NEW**
```yaml
# Now you can use stable identifiers directly!
- tapOn:
    id: "submit_form_button"
```

2. **Use Text-Based Selectors** (still reliable)
```yaml
# Still works great for user-facing text
- tapOn: "Submit"
```

3. **Combine Identifier with Other Properties**
```dart
// Set both for best results
Semantics(
  identifier: 'submit_form_button',  // For stable id selector
  label: 'Submit Registration Form', // For text selector and accessibility
  button: true,
  child: ElevatedButton(...),
)
```

**Testing Strategy:**
```yaml
# Option 1: Use stable identifier (recommended for complex UIs)
- tapOn:
    id: "submit_form_button"

# Option 2: Use text (recommended for simple, text-based elements)
- tapOn: "Submit Registration Form"

# Option 3: Combine both for maximum reliability
- tapOn:
    id: "submit_form_button"
    text: "Submit"
```

#### Why Use Identifiers?

The `identifier` property is valuable for multiple reasons:

1. **Code Documentation**: Makes your semantic tree more readable
```dart
Semantics(
  identifier: 'user_profile_avatar',  // Self-documenting
  image: true,
  label: 'Profile Picture',
  child: CircleAvatar(...),
)
```

2. **Cross-Element Relationships**: Enable `aria-controls` via `controlsNodes`
```dart
Semantics(
  identifier: 'accordion_panel',
  child: Panel(...),
)

Semantics(
  button: true,
  controlsNodes: ['accordion_panel'],  // References by identifier
  label: 'Expand',
  child: IconButton(...),
)
```

3. **Stable Test Selectors**: Maestro now supports `flt-semantics-identifier`, so your tests won't break when the semantic tree is rebuilt

4. **Other Testing Tools**: Many web testing tools can query by custom attributes

### 4.3 State-Based Matching

#### Example: Enabled/Disabled Buttons

**Flutter Semantics:**
```dart
Semantics(
  button: true,
  enabled: false,  // Disabled state
  label: 'Submit',
  child: ElevatedButton(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  role="button"
  aria-disabled="true">
  Submit
</flt-semantics>
```

**Maestro Test:**
```yaml
# Assert button is disabled
- assertVisible:
    text: "Submit"
    enabled: false

# Wait until button becomes enabled
- extendedWaitUntil:
    visible:
      text: "Submit"
      enabled: true
    timeout: 5000
```

#### Example: Checked State

**Flutter Semantics:**
```dart
Semantics(
  checked: true,
  label: 'Remember me',
  child: Checkbox(...),
)
```

**Maestro Test:**
```yaml
# Verify checkbox is checked
- assertVisible:
    text: "Remember me"
    checked: true

# Tap to uncheck
- tapOn: "Remember me"

# Verify now unchecked
- assertVisible:
    text: "Remember me"
    checked: false
```

#### Example: Selected State (Tabs)

**Flutter Semantics:**
```dart
Semantics(
  role: SemanticsRole.tab,
  selected: true,
  label: 'Home',
  child: Tab(...),
)
```

**Generated HTML:**
```html
<flt-semantics
  role="tab"
  aria-selected="true">
  Home
</flt-semantics>
```

**Maestro Test:**
```yaml
- assertVisible:
    text: "Home"
    selected: true

- tapOn: "Profile"

- assertVisible:
    text: "Profile"
    selected: true
```

### 4.4 Relative Position Matching

**Use Case:** When multiple elements have similar text/properties, use spatial relationships.

**Flutter UI:**
```dart
Column(
  children: [
    Semantics(label: 'Username', child: Text('Username')),
    Semantics(
      textField: true,
      label: 'Enter username',
      child: TextField(...),
    ),
    Semantics(label: 'Password', child: Text('Password')),
    Semantics(
      textField: true,
      label: 'Enter password',
      child: TextField(...),
    ),
  ],
)
```

**Maestro Test:**
```yaml
# Tap on the text field below "Username" label
- tapOn:
    text: "Enter username"
    below:
      text: "Username"

# Input text in that field
- inputText: "john_doe"

# Tap on password field
- tapOn:
    text: "Enter password"
    below:
      text: "Password"

- inputText: "password123"
```

### 4.5 Hierarchical Matching

**Flutter UI:**
```dart
Semantics(
  container: true,
  identifier: 'login_form',
  child: Column(children: [
    Semantics(label: 'Submit', button: true, child: ElevatedButton(...)),
    Semantics(label: 'Cancel', button: true, child: TextButton(...)),
  ]),
)
```

**Maestro Test:**
```yaml
# Tap Submit button that's inside the login form
- tapOn:
    text: "Submit"
    childOf:
      id: "flt-semantic-node-50"  # Assuming this is the form container
```

---

## 5. Practical Testing Examples

### 5.1 Complete Login Flow

**Flutter App:**
```dart
Semantics(
  container: true,
  child: Column(
    children: [
      Semantics(
        header: true,
        headingLevel: 1,
        label: 'Login',
        child: Text('Login'),
      ),
      Semantics(
        textField: true,
        label: 'Email',
        child: TextField(...),
      ),
      Semantics(
        textField: true,
        label: 'Password',
        child: TextField(...),
      ),
      Semantics(
        checked: false,
        label: 'Remember me',
        child: Checkbox(...),
      ),
      Semantics(
        button: true,
        label: 'Sign In',
        enabled: true,
        child: ElevatedButton(...),
      ),
    ],
  ),
)
```

**Maestro Test:**
```yaml
appId: com.example.flutterwebapp
---
# Wait for login screen to appear
- assertVisible:
    text: "Login"

# Enter email
- tapOn:
    text: "Email"
    enabled: true

- inputText: "user@example.com"

# Enter password
- tapOn:
    text: "Password"
    below:
      text: "Email"

- inputText: "SecurePassword123"

# Check "Remember me"
- tapOn:
    text: "Remember me"
    checked: false

- assertVisible:
    text: "Remember me"
    checked: true

# Tap Sign In button
- tapOn:
    text: "Sign In"
    enabled: true

# Verify successful login
- assertVisible: "Welcome"
```

### 5.2 Testing Form Validation

**Flutter App:**
```dart
// Initial state - Submit button is disabled
Semantics(
  button: true,
  enabled: false,
  label: 'Submit',
  child: ElevatedButton(...),
)

// After valid input - Submit button is enabled
Semantics(
  button: true,
  enabled: true,
  label: 'Submit',
  child: ElevatedButton(...),
)
```

**Maestro Test:**
```yaml
# Verify submit is initially disabled
- assertVisible:
    text: "Submit"
    enabled: false

# Fill in form fields
- tapOn: "Name"
- inputText: "John Doe"

- tapOn: "Email"
- inputText: "john@example.com"

# Wait for button to become enabled
- extendedWaitUntil:
    visible:
      text: "Submit"
      enabled: true
    timeout: 3000

# Now tap to submit
- tapOn:
    text: "Submit"
    enabled: true

# Verify success message
- assertVisible: "Form submitted successfully"
```

### 5.3 Testing Radio Button Groups

**Flutter App:**
```dart
Semantics(
  role: SemanticsRole.radioGroup,
  label: 'Select payment method',
  child: Column(children: [
    Semantics(
      inMutuallyExclusiveGroup: true,
      checked: true,
      label: 'Credit Card',
      child: Radio(...),
    ),
    Semantics(
      inMutuallyExclusiveGroup: true,
      checked: false,
      label: 'PayPal',
      child: Radio(...),
    ),
    Semantics(
      inMutuallyExclusiveGroup: true,
      checked: false,
      label: 'Bank Transfer',
      child: Radio(...),
    ),
  ]),
)
```

**Maestro Test:**
```yaml
# Verify Credit Card is selected by default
- assertVisible:
    text: "Credit Card"
    checked: true

# Select PayPal
- tapOn: "PayPal"

# Verify PayPal is now selected
- assertVisible:
    text: "PayPal"
    checked: true

# Verify Credit Card is now unselected
- assertVisible:
    text: "Credit Card"
    checked: false

# Select Bank Transfer
- tapOn: "Bank Transfer"

- assertVisible:
    text: "Bank Transfer"
    checked: true
```

### 5.4 Testing Tabs and Navigation

**Flutter App:**
```dart
Semantics(
  role: SemanticsRole.tabBar,
  child: Row(children: [
    Semantics(
      role: SemanticsRole.tab,
      selected: true,
      label: 'Home',
      child: Tab(...),
    ),
    Semantics(
      role: SemanticsRole.tab,
      selected: false,
      label: 'Profile',
      child: Tab(...),
    ),
    Semantics(
      role: SemanticsRole.tab,
      selected: false,
      label: 'Settings',
      child: Tab(...),
    ),
  ]),
)
```

**Maestro Test:**
```yaml
# Verify Home tab is selected
- assertVisible:
    text: "Home"
    selected: true

# Tap on Profile tab
- tapOn: "Profile"

# Verify Profile tab is now selected
- assertVisible:
    text: "Profile"
    selected: true

# Verify Home tab is no longer selected
- assertVisible:
    text: "Home"
    selected: false

# Wait for profile content to load
- assertVisible: "User Profile"

# Navigate to Settings
- tapOn: "Settings"

- assertVisible:
    text: "Settings"
    selected: true
```

### 5.5 Testing Lists and Scrolling

**Flutter App:**
```dart
Semantics(
  role: SemanticsRole.list,
  child: ListView(children: [
    Semantics(
      role: SemanticsRole.listItem,
      label: 'Item 1',
      child: ListTile(...),
    ),
    // ... more items
    Semantics(
      role: SemanticsRole.listItem,
      label: 'Item 50',
      child: ListTile(...),
    ),
  ]),
)
```

**Maestro Test:**
```yaml
# Verify first item is visible
- assertVisible: "Item 1"

# Scroll until target item is visible
- scrollUntilVisible:
    element: "Item 50"
    direction: DOWN
    timeout: 10000
    speed: 50

# Tap on the item
- tapOn: "Item 50"

# Verify item details
- assertVisible: "Item 50 Details"
```

### 5.6 Testing Dialogs

**Flutter App:**
```dart
// Dialog appears
Semantics(
  role: SemanticsRole.dialog,
  scopesRoute: true,
  namesRoute: true,
  label: 'Confirm Deletion',
  child: AlertDialog(
    title: Semantics(
      header: true,
      headingLevel: 2,
      label: 'Delete Item?',
      child: Text('Delete Item?'),
    ),
    actions: [
      Semantics(
        button: true,
        label: 'Cancel',
        child: TextButton(...),
      ),
      Semantics(
        button: true,
        label: 'Delete',
        child: ElevatedButton(...),
      ),
    ],
  ),
)
```

**Maestro Test:**
```yaml
# Trigger dialog
- tapOn: "Delete"

# Verify dialog appeared
- assertVisible:
    text: "Delete Item?"

# Verify both buttons are present
- assertVisible: "Cancel"
- assertVisible: "Delete"

# Tap Cancel
- tapOn: "Cancel"

# Verify dialog is dismissed
- assertNotVisible: "Delete Item?"

# Trigger dialog again
- tapOn: "Delete"

# This time, confirm deletion
- tapOn:
    text: "Delete"
    below:
      text: "Delete Item?"

# Verify success message
- assertVisible: "Item deleted successfully"
```

### 5.7 Testing Data Tables

**Flutter App:**
```dart
Semantics(
  role: SemanticsRole.table,
  label: 'User List',
  child: Table(children: [
    TableRow(children: [
      Semantics(role: SemanticsRole.columnHeader, label: 'Name', ...),
      Semantics(role: SemanticsRole.columnHeader, label: 'Email', ...),
      Semantics(role: SemanticsRole.columnHeader, label: 'Status', ...),
    ]),
    TableRow(children: [
      Semantics(role: SemanticsRole.cell, label: 'John Doe', ...),
      Semantics(role: SemanticsRole.cell, label: 'john@example.com', ...),
      Semantics(role: SemanticsRole.cell, label: 'Active', ...),
    ]),
  ]),
)
```

**Maestro Test:**
```yaml
# Verify table headers
- assertVisible: "Name"
- assertVisible: "Email"
- assertVisible: "Status"

# Verify first row data
- assertVisible:
    text: "John Doe"
    below:
      text: "Name"

- assertVisible:
    text: "john@example.com"
    below:
      text: "Email"

- assertVisible:
    text: "Active"
    below:
      text: "Status"

# Tap on a specific cell
- tapOn:
    text: "john@example.com"
```

---

## 6. Best Practices

### 6.1 Always Use Semantic Labels and Identifiers

❌ **Bad:**
```dart
ElevatedButton(
  onPressed: () {},
  child: Text('Submit'),
)
```

✅ **Good:**
```dart
Semantics(
  identifier: 'submit_button',  // Stable reference
  button: true,
  label: 'Submit',
  enabled: true,
  child: ElevatedButton(
    onPressed: () {},
    child: Text('Submit'),
  ),
)
```

✅✅ **Best:**
```dart
Semantics(
  identifier: 'submit_registration_form',  // Self-documenting ID
  button: true,
  label: 'Submit Registration Form',  // Unique, descriptive label
  enabled: true,
  child: ElevatedButton(
    onPressed: () {},
    child: Text('Submit'),
  ),
)
```

**Why:**
- Without explicit Semantics, Flutter may not generate accessible HTML, making Maestro testing unreliable
- Identifiers provide stable references for code organization and potential future testing
- Descriptive labels make tests readable and maintainable

### 6.2 Use Descriptive, Unique Labels with Meaningful Identifiers

❌ **Bad:**
```dart
Semantics(label: 'Button', button: true, ...)  // Generic
Semantics(label: 'Click here', button: true, ...)  // Not descriptive
Semantics(identifier: 'btn1', label: 'Submit', ...)  // Unclear identifier
```

✅ **Good:**
```dart
Semantics(
  identifier: 'submit_registration_button',
  label: 'Submit registration form',
  button: true,
  ...
)

Semantics(
  identifier: 'add_contact_button',
  label: 'Add new contact',
  button: true,
  ...
)
```

**Naming Conventions:**
- **Labels**: Natural language, user-facing, describes what the user sees
- **Identifiers**: snake_case, code-friendly, describes the element's purpose

**Why:**
- Unique labels make Maestro tests more reliable and easier to write
- Clear identifiers improve code maintainability and enable relationships
- Consistent naming makes the codebase easier to navigate

### 6.3 Leverage State Attributes

✅ **Good Practice:**
```yaml
# Wait for button to become enabled before tapping
- extendedWaitUntil:
    visible:
      text: "Submit"
      enabled: true
    timeout: 5000

- tapOn:
    text: "Submit"
    enabled: true
```

**Why:** State checks prevent flaky tests by ensuring elements are in the expected state before interaction.

### 6.4 Use Relative Positioning for Ambiguous Elements

When you have multiple similar elements:

```yaml
# Instead of just:
- tapOn: "Edit"

# Use:
- tapOn:
    text: "Edit"
    below:
      text: "John Doe"
```

### 6.5 Group Related Semantics in Containers with Identifiers

✅ **Good:**
```dart
Semantics(
  identifier: 'user_profile_section',
  container: true,
  label: 'User profile section',
  child: Column(children: [
    Semantics(
      identifier: 'profile_name',
      label: 'User Name',
      child: Text('John Doe'),
    ),
    Semantics(
      identifier: 'profile_email',
      label: 'Email',
      child: Text('john@example.com'),
    ),
  ]),
)
```

**Why:**
- Containers help screen readers navigate logical groups
- Identifiers on containers enable hierarchical organization
- Tests can verify content within specific sections

### 6.6 Test Accessibility Announcements

**Flutter:**
```dart
SemanticsService.announce('Form submitted', TextDirection.ltr);
```

**Maestro:**
```yaml
# Wait for success indication
- assertVisible: "Success"
```

**Why:** While Maestro can't directly verify ARIA-live announcements, you can test for visual confirmation of the same events.

### 6.7 Handle Dynamic Content

```yaml
# Wait for loading to complete
- assertNotVisible: "Loading..."

# Then verify content
- assertVisible: "User Dashboard"
```

### 6.8 Use Index for Multiple Similar Elements

```yaml
# Tap on the second "Delete" button
- tapOn:
    text: "Delete"
    index: 1  # 0-based index
```

---

## 7. Known Limitations

### 7.1 Limited Web Support in Maestro

⚠️ **Important:** Maestro's web testing capabilities are **experimental and limited**. The tool is primarily designed for mobile apps (iOS/Android).

**Limitations:**
- Some mobile-specific commands don't work on web
- Web element detection may be less reliable than mobile
- Browser automation is less mature than mobile

### 7.2 Flutter Web ID Stability

Flutter's web engine generates two types of IDs:

#### Auto-Generated IDs (Unstable ❌)
Flutter assigns sequential IDs like `flt-semantic-node-42` that **may change** between:
- App restarts
- Different build versions
- Semantic tree restructuring
- Widget additions/removals earlier in the tree

**Example:**
```html
<!-- Run 1 -->
<flt-semantics id="flt-semantic-node-42" role="button">Submit</flt-semantics>

<!-- Run 2 (after adding widgets) -->
<flt-semantics id="flt-semantic-node-55" role="button">Submit</flt-semantics>
```

#### Developer-Defined Identifiers (Stable ✅)
The `identifier` property creates stable `flt-semantics-identifier` attributes:

**Flutter:**
```dart
Semantics(
  identifier: 'submit_button',  // Always 'submit_button'
  button: true,
  label: 'Submit',
  child: ElevatedButton(...),
)
```

**HTML:**
```html
<!-- Run 1 -->
<flt-semantics
  id="flt-semantic-node-42"
  flt-semantics-identifier="submit_button">Submit</flt-semantics>

<!-- Run 2 (stable identifier!) -->
<flt-semantics
  id="flt-semantic-node-55"
  flt-semantics-identifier="submit_button">Submit</flt-semantics>
```

#### Testing Strategy

**❌ Don't rely on auto-generated IDs:**
```yaml
# Bad - will break
- tapOn:
    id: "flt-semantic-node-42"
```

**✅ Use developer-defined identifiers (RECOMMENDED):**
```yaml
# Best - stable and semantic
- tapOn:
    id: "submit_button"
```

**✅ Or use text-based selectors:**
```yaml
# Good - stable for text-based elements
- tapOn: "Submit"
```

**✅ Set both identifier and label:**
```dart
// Best practice - enables both strategies
Semantics(
  identifier: 'submit_button',  // For id selector ⭐
  label: 'Submit',              // For text selector and accessibility
  button: true,
  ...
)
```

### 7.3 No Direct Access to ARIA Roles

Maestro can't directly query by ARIA role. You can't write:
```yaml
# ❌ Not supported
- tapOn:
    role: "button"
```

**Solution:** Use text, state, or position-based selectors instead.

### 7.4 Flutter Custom Elements Not Standard HTML

Flutter uses custom elements like `<flt-semantics>` instead of standard HTML buttons, inputs, etc. (except for specific cases like text fields).

**Impact:** Some web testing tools that expect standard HTML may not work as expected. Maestro handles this through its accessibility-focused selectors.

### 7.5 Slider and Range Input Limitations

Direct manipulation of sliders is limited in Maestro for web:

```yaml
# Limited support for setting slider values
- tapOn:
    point: "75%,50%"  # Approximate position
```

### 7.6 No Direct Support for ARIA Attributes

You can't directly query elements by custom ARIA attributes like:
- `aria-describedby`
- `aria-controls`
- `aria-owns`

**Workaround:** Use the visible text or labels that correspond to these relationships.

### 7.7 Keyboard Navigation Testing

Limited support for testing keyboard-only navigation:

```yaml
# Limited key support
- pressKey: "Enter"
- pressKey: "Tab"
```

**Note:** Web keyboard navigation support in Maestro is basic compared to mobile.

---

## 8. Complete Reference Tables

### 8.1 The `identifier` Property Quick Reference

| Aspect | Details |
|--------|---------|
| **Flutter Property** | `Semantics(identifier: 'my_button', ...)` |
| **Generated Attribute** | `flt-semantics-identifier="my_button"` |
| **Stability** | ✅ Stable across runs, rebuilds, and updates |
| **Auto-Generated ID** | `id="flt-semantic-node-42"` (❌ unstable, changes) |
| **Maestro Support** | ⚠️ Limited - Can't directly query by identifier attribute |
| **Testing Strategy** | Use descriptive `label` for Maestro, `identifier` for code organization |
| **Primary Use Cases** | 1. Code documentation<br>2. `controlsNodes` relationships<br>3. Future-proofing<br>4. Non-Maestro testing tools |
| **Naming Convention** | snake_case (e.g., `'submit_registration_form'`) |
| **Best Practice** | Always set both `identifier` and `label` |

**Example:**
```dart
Semantics(
  identifier: 'submit_registration_form',  // Stable, for code
  label: 'Submit Registration Form',        // For Maestro & screen readers
  button: true,
  enabled: true,
  child: ElevatedButton(...),
)
```

### 8.2 Flutter Semantics → HTML Tags Mapping

| Flutter Semantics | Generated HTML | ARIA Role | Custom Attribute | Maestro Text Selector | Maestro State Selector |
|-------------------|----------------|-----------|------------------|----------------------|------------------------|
| `identifier: 'btn'` + `button: true` | `<flt-semantics>` | `role="button"` | `flt-semantics-identifier="btn"` | `text: "label"` | `enabled: true/false` |
| `checked: true` (checkbox) | `<flt-semantics>` | `role="checkbox"` | `text: "label"` | `checked: true/false` |
| `inMutuallyExclusiveGroup: true` | `<flt-semantics>` | `role="radio"` | `text: "label"` | `checked: true/false` |
| `toggled: true` | `<flt-semantics>` | `role="switch"` | `text: "label"` | `checked: true/false` |
| `textField: true` | `<input>` or `<textarea>` | (native) | `text: "label"` | `enabled: true/false` |
| `image: true` | `<flt-semantics>` | `role="img"` | `text: "label"` | N/A |
| `link: true` | `<a>` | (native) | `text: "label"` | `enabled: true/false` |
| `header: true, headingLevel: N` | `<hN>` | (native) | `text: "label"` | N/A |
| `slider: true` | `<input type="range">` | `role="slider"` | Limited | Limited |
| `role: SemanticsRole.tab` | `<flt-semantics>` | `role="tab"` | `text: "label"` | `selected: true/false` |
| `role: SemanticsRole.tabBar` | `<flt-semantics>` | `role="tablist"` | `text: "label"` | N/A |
| `role: SemanticsRole.dialog` | `<flt-semantics>` | `role="dialog"` | `text: "label"` | N/A |
| `role: SemanticsRole.table` | `<flt-semantics>` | `role="table"` | `text: "label"` | N/A |
| `role: SemanticsRole.list` | `<flt-semantics>` | `role="list"` | `text: "label"` | N/A |
| `role: SemanticsRole.listItem` | `<flt-semantics>` | `role="listitem"` | `text: "label"` | N/A |
| `role: SemanticsRole.menu` | `<flt-semantics>` | `role="menu"` | `text: "label"` | N/A |
| `role: SemanticsRole.menuItem` | `<flt-semantics>` | `role="menuitem"` | `text: "label"` | N/A |
| `scopesRoute: true` | `<flt-semantics>` | (container) | `text: "label"` | N/A |

### 8.3 Maestro Commands for Flutter Web Testing

| Maestro Command | Description | Flutter Use Case | Example |
|-----------------|-------------|------------------|---------|
| `launchApp` | Open web app | Start testing | `- launchApp: com.example.app` |
| `tapOn` | Click element | Buttons, links, checkboxes | `- tapOn: "Submit"` |
| `inputText` | Type into field | Text fields | `- inputText: "user@example.com"` |
| `assertVisible` | Verify element exists | Check UI state | `- assertVisible: "Welcome"` |
| `assertNotVisible` | Verify element gone | Check dismissals | `- assertNotVisible: "Loading..."` |
| `extendedWaitUntil` | Wait for condition | Dynamic content | `- extendedWaitUntil: { visible: "Done" }` |
| `scroll` | Scroll view | Long lists | `- scroll: { direction: DOWN }` |
| `scrollUntilVisible` | Scroll to element | Find off-screen items | `- scrollUntilVisible: { element: "Item 50" }` |
| `pressKey` | Keyboard input | Submit forms | `- pressKey: "Enter"` |
| `swipe` | Swipe gesture | Carousels, tabs | `- swipe: { direction: LEFT }` |

### 8.4 Common Flutter Web Testing Patterns

| Pattern | Flutter Code | Maestro Test |
|---------|--------------|--------------|
| **Button Tap** | `Semantics(identifier: 'ok_button', button: true, label: 'OK', ...)` | `- tapOn: "OK"` |
| **Form Fill** | `Semantics(textField: true, label: 'Email', ...)` | `- tapOn: "Email"`<br>`- inputText: "test@example.com"` |
| **Checkbox Toggle** | `Semantics(checked: false, label: 'Agree', ...)` | `- tapOn: "Agree"`<br>`- assertVisible: { text: "Agree", checked: true }` |
| **Tab Selection** | `Semantics(role: SemanticsRole.tab, selected: false, ...)` | `- tapOn: "Profile"`<br>`- assertVisible: { text: "Profile", selected: true }` |
| **Wait for Load** | Dynamic content with loading indicator | `- assertNotVisible: "Loading"`<br>`- assertVisible: "Content"` |
| **Verify State** | `Semantics(enabled: false, ...)` | `- assertVisible: { text: "Submit", enabled: false }` |
| **Conditional Flow** | Different paths based on state | `- runFlow:`<br>`    when: { visible: "Login" }`<br>`    commands: [...]` |

---

## Conclusion

Testing Flutter web applications with Maestro requires understanding both:
1. How Flutter generates accessible HTML/ARIA tags
2. What selectors Maestro provides

**Key Takeaways:**
- ✅ Always use `Semantics` widgets in Flutter for testability
- ✅ Always set both `identifier` (for code) and `label` (for tests) properties
- ✅ Prefer text-based and state-based selectors (most reliable)
- ✅ Use relative positioning for ambiguous elements
- ⚠️ Avoid relying on auto-generated `flt-semantic-node-*` IDs (they change)
- ✅ Use `identifier` property for stable references and documentation
- ⚠️ Be aware of Maestro's limited web support
- ✅ Test core user flows with simple, descriptive selectors

By following this guide, you can create maintainable, reliable end-to-end tests for your Flutter web applications using Maestro.

---

## Additional Resources

- **Maestro Documentation:** https://maestro.mobile.dev/
- **Flutter Web Semantics:** https://docs.flutter.dev/development/accessibility-and-localization/accessibility
- **ARIA Specification:** https://www.w3.org/WAI/ARIA/
- **Flutter Web Rendering:** https://docs.flutter.dev/development/platform-integration/web

