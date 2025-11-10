# Flutter Identifier System - Quick Summary

## The Three Key Concepts

### 1. `flutter-id` (Maestro Test Selector)

**Used in:** Maestro YAML test files  
**Purpose:** Select elements by their Flutter identifier  
**Naming:** Kebab-case (`flutter-id`) in YAML, camelCase (`flutterId`) in Kotlin  
**Example:**
```yaml
- tapOn:
    flutter-id: "submit_button"
```

**Why kebab-case?** Matches HTML/web conventions like `flt-semantics-identifier`, `resource-id`, and `data-testid`. This is the only multi-word Maestro selector using kebab-case.

---

### 2. `Semantics(identifier: value)` (Flutter Widget Property)

**Used in:** Flutter/Dart code  
**Purpose:** Set a stable identifier for semantic elements  
**Example:**
```dart
Semantics(
  identifier: 'submit_button',
  button: true,
  label: 'Submit Form',
  child: ElevatedButton(...),
)
```

---

### 3. `flt-semantics-identifier` (HTML DOM Attribute)

**Used in:** Generated HTML by Flutter web engine  
**Purpose:** Bridge between Flutter code and browser DOM  
**Example:**
```html
<flt-semantics
  id="flt-semantic-node-42"
  flt-semantics-identifier="submit_button"
  role="button">
  Submit Form
</flt-semantics>
```

---

## How They Connect

```
Flutter Code                 HTML DOM                    Maestro Test
────────────                 ────────                    ────────────

Semantics(               →   <flt-semantics          →   - tapOn:
  identifier:                  flt-semantics-              flutter-id:
    'submit_button'            identifier=                   "submit_button"
)                              "submit_button">
```

---

## Implementation Files

### 1. JavaScript Extraction (DOM → Attributes)
**File:** `maestro-client/src/main/resources/maestro-web.js` (lines 96-100)
```javascript
const fltSemanticsId = node.getAttribute('flt-semantics-identifier')
if (!!fltSemanticsId) {
  attributes['flutter-id'] = fltSemanticsId
}
```
**Purpose:** Extracts `flt-semantics-identifier` HTML attribute and stores as `flutter-id`

---

### 2. Kotlin Filter (Element Matching)
**File:** `maestro-client/src/main/java/maestro/Filters.kt` (lines 124-130)
```kotlin
fun flutterIdMatches(flutterId: String): ElementFilter {
    return { nodes ->
        nodes.filter { it.attributes["flutter-id"] == flutterId }
    }
}
```
**Purpose:** Filters elements by matching `flutter-id` attribute

---

### 3. WebDriver Parsers (DOM Parsing)

#### Selenium WebDriver
**File:** `maestro-client/src/main/java/maestro/drivers/WebDriver.kt` (lines 228-230)
```kotlin
if (attrs.containsKey("flutter-id") && attrs["flutter-id"] != null) {
    attributes["flutter-id"] = attrs["flutter-id"] as String
}
```

#### CDP WebDriver
**File:** `maestro-client/src/main/java/maestro/drivers/CdpWebDriver.kt` (lines 272-274)
```kotlin
if (attrs.containsKey("flutter-id") && attrs["flutter-id"] != null) {
    attributes["flutter-id"] = attrs["flutter-id"] as String
}
```
**Purpose:** Both drivers parse `flutter-id` from JavaScript and preserve in TreeNode

**Note:** Maestro has two web driver implementations:
- `WebDriver` - Standard Selenium WebDriver (broader compatibility)
- `CdpWebDriver` - Chrome DevTools Protocol (better performance)

---

### 4. YAML Selector Definition (Test Syntax)
**File:** `maestro-orchestra/src/main/java/maestro/orchestra/yaml/YamlElementSelector.kt` (line 29)
```kotlin
@JsonProperty("flutter-id") val flutterId: String? = null
```
**Purpose:** Maps YAML `flutter-id` (kebab-case) to Kotlin `flutterId` (camelCase)

---

### 5. Element Selector Model (Data Model)
**File:** `maestro-orchestra-models/src/main/java/maestro/orchestra/ElementSelector.kt` (lines 28, 82-84)
```kotlin
data class ElementSelector(
    val flutterId: String? = null,
    // ...
) {
    fun description(): String {
        flutterId?.let { descriptions.add("flutter-id: $it") }
    }
}
```
**Purpose:** Stores flutter-id value and provides user-facing descriptions

---

## Quick Example

### Flutter Code
```dart
Semantics(
  identifier: 'login_button',
  button: true,
  label: 'Log In',
  enabled: true,
  child: ElevatedButton(
    onPressed: () => login(),
    child: Text('Log In'),
  ),
)
```

### Generated HTML
```html
<flt-semantics
  id="flt-semantic-node-50"
  flt-semantics-identifier="login_button"
  role="button"
  flt-tappable>
  Log In
</flt-semantics>
```

### Maestro Test
```yaml
- tapOn:
    flutter-id: "login_button"

# Or combine with other selectors
- tapOn:
    flutter-id: "login_button"
    enabled: true
```

---

## Key Benefits

✅ **Stable** - Identifiers don't change across rebuilds  
✅ **Readable** - Use meaningful names like `submit_button` instead of `flt-semantic-node-42`  
✅ **Reliable** - Independent of auto-generated IDs  
✅ **Flexible** - Can combine with other selectors (`enabled`, `selected`, etc.)  
✅ **Backward Compatible** - Existing tests continue to work  

---

## Best Practice Pattern

```dart
// Always set BOTH identifier and label
Semantics(
  identifier: 'submit_registration_form',  // For flutter-id selector
  label: 'Submit Registration Form',       // For text selector + accessibility
  button: true,
  enabled: _isFormValid,
  child: ElevatedButton(
    onPressed: _isFormValid ? _submit : null,
    child: Text('Submit'),
  ),
)
```

```yaml
# Test can use either selector
- tapOn:
    flutter-id: "submit_registration_form"  # Stable ID

# Or
- tapOn: "Submit Registration Form"  # Text-based

# Or combine both for maximum reliability
- tapOn:
    flutter-id: "submit_registration_form"
    text: "Submit"
    enabled: true
```

---

## Complete Documentation

For comprehensive documentation with all code examples, see:
- **[FLUTTER_IDENTIFIER_DOCUMENTATION.md](FLUTTER_IDENTIFIER_DOCUMENTATION.md)** - Complete guide
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Implementation details
- **[e2e/workspaces/flutter_web_identifier/README.md](e2e/workspaces/flutter_web_identifier/README.md)** - Test examples
- **[research/flutter_web_testing.md](research/flutter_web_testing.md)** - Flutter web testing guide
- **[research/identifier.md](research/identifier.md)** - Identifier property details

---

## Test Examples

See complete test suite at:
- **[e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml](e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml)**
- **[e2e/workspaces/flutter_web_identifier/test_app.html](e2e/workspaces/flutter_web_identifier/test_app.html)**

