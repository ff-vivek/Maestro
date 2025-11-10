# Flutter Web Identifier Test

This test workspace demonstrates the new feature that allows Maestro to use Flutter's `flt-semantics-identifier` attribute for element selection.

## What's New

Previously, Maestro could only use the auto-generated `id` attribute (e.g., `flt-semantic-node-42`) which changes between runs. Now, Maestro can use the stable `flt-semantics-identifier` attribute that Flutter developers set explicitly.

## Flutter Code Example

In your Flutter web app, you can now use semantic identifiers:

```dart
Semantics(
  identifier: 'submit_button',  // Stable identifier
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
  id="flt-semantic-node-42"                    <!-- Unstable, changes -->
  flt-semantics-identifier="submit_button"     <!-- Stable ✅ -->
  role="button">
  Submit Form
</flt-semantics>
```

## Maestro Test

You can now use the `flutter-id` selector for Flutter identifiers:

```yaml
# Before (unstable - would break after rebuild):
- tapOn:
    id: "flt-semantic-node-42"

# After (stable - uses flt-semantics-identifier):
- tapOn:
    flutter-id: "submit_button"
```

## How It Works

Maestro extracts `flt-semantics-identifier` as a separate attribute called `flutter-id`. You can then use the `flutter-id` selector to target elements by their Flutter identifier:

- `flutter-id` selector → matches `flt-semantics-identifier` attribute (Flutter's stable identifier)
- `id` selector → matches HTML `id` attribute (traditional web IDs)
- `text` selector → matches visible text content

## Running the Test

```bash
# From the Maestro root directory
maestro test e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml
```

## Benefits

1. **Stable Tests**: Your tests won't break when Flutter rebuilds the semantic tree
2. **Better Readability**: Use meaningful names like `submit_button` instead of `flt-semantic-node-42`
3. **Backward Compatible**: Existing tests continue to work
4. **Works with All Selectors**: Combine with state-based and relative selectors

## Examples in Test

The test file demonstrates:

- ✅ Using `flutter-id` for text fields
- ✅ Using `flutter-id` for buttons
- ✅ Using `flutter-id` for checkboxes
- ✅ Using `flutter-id` for tabs with state checks
- ✅ Combining `flutter-id` with other selectors (like `enabled`, `selected`)

## Implementation Details

The feature works by:
1. Modifying `maestro-web.js` to extract the `flt-semantics-identifier` attribute as `flutter-id`
2. Adding a new `flutter-id` selector in Maestro's element selection system
3. The `flutter-id` selector specifically targets the `flutter-id` attribute
4. This keeps Flutter identifiers separate from traditional HTML `id` attributes

