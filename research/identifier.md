# Testing Maestro with Flutter's identifier Property

## Can Maestro Access flt-semantics-identifier?

### Test Scenario

**Flutter Code:**
```dart
Semantics(
  identifier: 'submit_button',
  button: true,
  label: 'Submit Form',
  enabled: true,
  child: ElevatedButton(
    onPressed: () {},
    child: Text('Submit'),
  ),
)
```

**Generated HTML:**
```html
<flt-semantics
  id="flt-semantic-node-42"
  flt-semantics-identifier="submit_button"
  role="button"
  flt-tappable>
  Submit Form
</flt-semantics>
```

---

## Approach 1: Direct ID Selector ✅ **NOW WORKS!**

**Maestro Test:**
```yaml
# This NOW WORKS! - Maestro checks flt-semantics-identifier first
- tapOn:
    id: "submit_button"
```

**Result:** ✅ **WORKS**
- Maestro now looks for `flt-semantics-identifier="submit_button"` FIRST
- If not found, falls back to `id="submit_button"`
- Element has both `id="flt-semantic-node-42"` and `flt-semantics-identifier="submit_button"`
- Maestro prioritizes the stable `flt-semantics-identifier`

---

## Approach 2: CSS Attribute Selector ❓

**Maestro Test:**
```yaml
# Attempt to use CSS attribute selector
- tapOn: "[flt-semantics-identifier='submit_button']"
```

**Status:** ❓ **EXPERIMENTAL - Needs Testing**
- Maestro's text selector *might* support CSS selectors
- This is not documented officially
- Unlikely to work based on Maestro's architecture

**To Test This:**
1. Create a Flutter web app with identifiers
2. Run Maestro flow with attribute selector
3. Check if it finds the element

---

## Approach 3: JavaScript Evaluation 🤔

**Maestro Test:**
```yaml
# Use JavaScript to find element by custom attribute
- runScript:
    file: "find_by_identifier.js"
```

**find_by_identifier.js:**
```javascript
const element = document.querySelector("[flt-semantics-identifier='submit_button']");
if (element) {
  element.click();
  return true;
}
return false;
```

**Status:** 🤔 **MIGHT WORK**
- Maestro supports JavaScript execution
- You can query by any attribute in JavaScript
- Then trigger actions programmatically

---

## Approach 4: Text-Based Selector ✅ (RECOMMENDED)

**Maestro Test:**
```yaml
# Use the label text instead
- tapOn: "Submit Form"
```

**Status:** ✅ **WORKS RELIABLY**
- This is the recommended approach
- Stable and documented
- Works consistently

---

## Approach 5: Combine Identifier + Unique Label ✅✅ (BEST PRACTICE)

**Flutter Code:**
```dart
Semantics(
  identifier: 'submit_registration_form',     // For code organization
  button: true,
  label: 'Submit Registration Form',          // Unique, descriptive - for testing
  enabled: true,
  child: ElevatedButton(...),
)
```

**Maestro Test:**
```yaml
- tapOn: "Submit Registration Form"
```

**Why This Works:**
- `identifier`: Provides stable reference in code, enables `controlsNodes`
- `label`: Provides Maestro-friendly selector
- Both serve different but complementary purposes

---

## Summary: Current State

| Method | Maestro Support | Recommendation |
|--------|-----------------|----------------|
| Direct `id: "identifier_value"` | ✅ **YES!** | **Use this** ⭐ |
| CSS attribute selector | ❓ Unknown | Not needed anymore |
| JavaScript workaround | 🤔 Possible | Not needed anymore |
| Text-based selector | ✅ Yes | Still works great |
| Unique label + identifier | ✅✅ Best | **Recommended pattern** ⭐⭐ |

---

## Why Still Use Identifiers?

Even though Maestro can't directly access them, identifiers are valuable for:

### 1. Code Documentation
```dart
// Clear, self-documenting code
Semantics(
  identifier: 'user_profile_section',
  container: true,
  child: ProfileWidget(),
)
```

### 2. ARIA Relationships
```dart
Semantics(
  identifier: 'expandable_panel',
  child: Panel(),
)

Semantics(
  button: true,
  controlsNodes: ['expandable_panel'],  // ← Uses identifier
  expanded: false,
  label: 'Expand Details',
  child: IconButton(...),
)
```

This generates:
```html
<flt-semantics
  role="button"
  aria-controls="flt-semantic-node-100"  <!-- Properly linked! -->
  aria-expanded="false">
  Expand Details
</flt-semantics>
```

### 3. Other Testing Tools
Some tools (Playwright, Cypress, Selenium) **can** query by custom attributes:

```javascript
// Playwright example
await page.locator('[flt-semantics-identifier="submit_button"]').click();
```

### 4. Future Maestro Support
If Maestro adds custom attribute support, your code is ready.

---

## Recommended Pattern

```dart
// ✅ BEST PRACTICE
Semantics(
  identifier: 'user_login_submit',           // Stable reference
  button: true,
  label: 'Log In to Your Account',           // Maestro tests this
  enabled: _isFormValid,
  onTap: _handleLogin,
  child: ElevatedButton(
    onPressed: _isFormValid ? _handleLogin : null,
    child: Text('Log In'),
  ),
)
```

```yaml
# Maestro test
- tapOn: "Log In to Your Account"
- assertVisible: "Welcome back!"
```

---

## Conclusion

**Direct Answer:** ✅ **YES! Maestro can now utilize Flutter's `identifier` property directly!**

**Implementation:** Maestro's `maestro-web.js` has been updated to check for `flt-semantics-identifier` attribute with highest priority when building the element hierarchy.

**Best Practice:** Set both properties on every semantic element:
- `identifier`: For stable Maestro `id` selector and ARIA relationships
- `label`: For Maestro `text` selector, accessibility, and screen readers

**Priority Order:** Maestro now checks identifiers in this order:
1. `flt-semantics-identifier` (Flutter's stable identifier) ⭐ **Highest Priority**
2. `id` (HTML id attribute)
3. `ariaLabel` (ARIA label)
4. `name` (name attribute)
5. `title` (title attribute)
6. `htmlFor` (for attribute)
7. `data-testid` (test ID)

