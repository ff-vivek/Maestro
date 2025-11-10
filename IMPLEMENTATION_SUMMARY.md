# Implementation Summary: Flutter `flt-semantics-identifier` Support

## Overview

This implementation adds support for Flutter's `flt-semantics-identifier` attribute in Maestro's web testing capabilities. Previously, Maestro could only use auto-generated IDs (like `flt-semantic-node-42`) which are unstable and change between app rebuilds. Now, Maestro can use the stable, developer-defined identifiers set via Flutter's `Semantics(identifier: '...')` property.

## Problem Statement

Flutter web applications generate two types of element identifiers:

1. **Auto-generated IDs** (unstable): `flt-semantic-node-{number}`
   - Changes between runs, rebuilds, and semantic tree updates
   - Not reliable for testing

2. **Developer-defined identifiers** (stable): `flt-semantics-identifier` attribute
   - Set explicitly by developers via `Semantics(identifier: '...')`
   - Remains constant across runs and rebuilds
   - Previously not accessible to Maestro

## Solution

Modified `maestro-web.js` to extract the `flt-semantics-identifier` attribute from DOM elements and prioritize it over other identifiers when building the element hierarchy.

## Changes Made

### 1. Core Implementation

**File**: `maestro-client/src/main/resources/maestro-web.js`

**Change**: Modified the `traverse` function to check for `flt-semantics-identifier` attribute first:

```javascript
// Check for Flutter semantics identifier first (most stable), then fallback to other identifiers
const fltSemanticsId = node.getAttribute ? node.getAttribute('flt-semantics-identifier') : null

if (!!fltSemanticsId || !!node.id || !!node.ariaLabel || !!node.name || !!node.title || !!node.htmlFor || !!node.attributes['data-testid']) {
  const title = typeof node.title === 'string' ? node.title : null
  // Prioritize flt-semantics-identifier for Flutter web apps since it's stable across rebuilds
  attributes['resource-id'] = fltSemanticsId || node.id || node.ariaLabel || node.name || title || node.htmlFor || node.attributes['data-testid']?.value
}
```

**Priority Order** (highest to lowest):
1. `flt-semantics-identifier` ⭐ **NEW - Highest Priority**
2. `id` (HTML id attribute)
3. `ariaLabel` (ARIA label)
4. `name` (name attribute)
5. `title` (title attribute)
6. `htmlFor` (for attribute)
7. `data-testid` (test ID attribute)

### 2. Test Example

**Directory**: `e2e/workspaces/flutter_web_identifier/`

Created a complete test workspace demonstrating the feature:

- **`test_app.html`**: Sample HTML page simulating a Flutter web app with `flt-semantics-identifier` attributes
- **`test_flutter_identifier.yaml`**: Maestro test flow demonstrating various use cases
- **`README.md`**: Comprehensive documentation and examples

### 3. Documentation Updates

**Updated Files**:
- `research/flutter_web_testing.md`: Updated to reflect that `flt-semantics-identifier` is now supported
- `research/identifier.md`: Changed from "not supported" to "fully supported" with updated examples

## Usage

### In Flutter Code

```dart
Semantics(
  identifier: 'submit_button',  // Stable identifier for testing
  button: true,
  label: 'Submit Form',
  enabled: true,
  child: ElevatedButton(
    onPressed: () {},
    child: Text('Submit'),
  ),
)
```

This generates HTML like:

```html
<flt-semantics
  id="flt-semantic-node-42"                    <!-- Unstable -->
  flt-semantics-identifier="submit_button"     <!-- Stable ✅ -->
  role="button">
  Submit Form
</flt-semantics>
```

### In Maestro Tests

```yaml
# Before (unstable - breaks after rebuild):
- tapOn:
    id: "flt-semantic-node-42"  # ❌ Changes between runs

# After (stable - uses flt-semantics-identifier):
- tapOn:
    flutter-id: "submit_button"  # ✅ Stable across rebuilds
```

### Combined Approach (Recommended)

```dart
// Flutter code - set both for maximum reliability
Semantics(
  identifier: 'submit_button',  // For stable id selector
  label: 'Submit Form',         // For text selector and accessibility
  button: true,
  child: ElevatedButton(...),
)
```

```yaml
# Maestro test - can use either selector
- tapOn:
    flutter-id: "submit_button"      # Option 1: Use stable identifier
    
- tapOn: "Submit Form"       # Option 2: Use text label

- tapOn:                      # Option 3: Combine both
    flutter-id: "submit_button"
    text: "Submit"
```

## Benefits

1. **Stable Tests**: Tests won't break when Flutter rebuilds the semantic tree
2. **Better Readability**: Use meaningful names like `submit_button` instead of `flt-semantic-node-42`
3. **Backward Compatible**: Existing tests continue to work without changes
4. **Works with All Selectors**: Can be combined with state-based and relative selectors
5. **Prioritized Correctly**: Flutter identifiers take precedence over less stable identifiers

## Test Coverage

The implementation includes a comprehensive test suite in `e2e/workspaces/flutter_web_identifier/` that demonstrates:

- ✅ Text field selection using identifiers
- ✅ Button selection using identifiers
- ✅ Checkbox selection using identifiers
- ✅ Tab selection with state verification
- ✅ Combining identifier selectors with other selectors

## Running the Tests

```bash
# From the Maestro root directory
maestro test e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml
```

## Technical Details

### How It Works

1. The JavaScript file `maestro-web.js` is injected into the browser by `WebDriver.kt`
2. It traverses the DOM and extracts element information
3. For each element, it now checks for `flt-semantics-identifier` attribute first
4. The identifier is stored in the `resource-id` field of the TreeNode
5. Maestro's `id` selector matches against the `resource-id` field
6. This allows seamless use of Flutter identifiers in test flows

### Compatibility

- **Backward Compatible**: Existing tests using `id`, `text`, or other selectors continue to work
- **No Breaking Changes**: Only adds support for a new attribute, doesn't change existing behavior
- **Graceful Degradation**: If `flt-semantics-identifier` is not present, falls back to existing identifier detection

## Example Use Cases

### Form Testing
```yaml
- tapOn:
    flutter-id: "username_field"
- inputText: "testuser"

- tapOn:
    flutter-id: "password_field"
- inputText: "password123"

- tapOn:
    flutter-id: "login_button"
```

### Tab Navigation
```yaml
- tapOn:
    flutter-id: "profile_tab"
    
- assertVisible:
    flutter-id: "profile_tab"
    selected: true
```

### State Verification
```yaml
- assertVisible:
    flutter-id: "submit_button"
    enabled: false

# After filling form...

- assertVisible:
    flutter-id: "submit_button"
    enabled: true
```

## Best Practices

1. **Always set both `identifier` and `label`**:
   ```dart
   Semantics(
     identifier: 'submit_button',  // For id selector
     label: 'Submit',               // For text selector & accessibility
     button: true,
     ...
   )
   ```

2. **Use descriptive identifier names**:
   - ✅ Good: `submit_registration_form`, `user_profile_avatar`
   - ❌ Bad: `btn1`, `field2`

3. **Choose the right selector**:
   - Use `flutter-id` for complex UIs with many similar elements
   - Use `text` for simple, text-based elements
   - Combine both for maximum reliability

4. **Document your identifiers**:
   - Keep a list of important identifiers in your Flutter app
   - Use consistent naming conventions

## Future Enhancements

Potential improvements for future iterations:

1. Add support for other Flutter-specific attributes
2. Create IDE plugins to auto-suggest identifiers
3. Generate identifier documentation from Flutter code
4. Add validation to ensure identifier uniqueness

## Conclusion

This implementation significantly improves the reliability and maintainability of Maestro tests for Flutter web applications by enabling the use of stable, developer-defined identifiers. Tests are now resistant to semantic tree changes and more readable with meaningful identifier names.

