# Flutter Semantics to Browser Accessibility Report

## Executive Summary

This report explains how Flutter's `Semantics` widget is converted into browser accessibility tags (HTML elements with ARIA attributes) when running a Flutter web application. The conversion happens through a sophisticated pipeline that transforms high-level semantic descriptions into platform-specific accessibility implementations.

---

## 1. Overview: The Semantics Pipeline

The conversion from Flutter's `Semantics` widget to browser accessibility tags follows this flow:

```
Flutter Widget Tree (Semantics)
    ↓
Rendering Layer (RenderSemanticsAnnotations)
    ↓
Semantics Tree (SemanticsNode)
    ↓
Platform Channel (SemanticsUpdate)
    ↓
Web Engine (SemanticsObject)
    ↓
Role Detection & Creation (SemanticRole)
    ↓
DOM Elements with ARIA attributes
```

---

## 2. Flutter Framework Layer

### 2.1 The Semantics Widget

The `Semantics` widget (defined in `packages/flutter/lib/src/widgets/basic.dart`) allows developers to annotate their UI with semantic information:

```dart
Semantics(
  label: 'Submit button',
  button: true,
  enabled: true,
  onTap: () { /* action */ },
  child: Container(...),
)
```

**Key Properties:**
- **Structural**: `container`, `explicitChildNodes`, `excludeSemantics`
- **State Flags**: `button`, `header`, `textField`, `image`, `checked`, `selected`, `enabled`, `focused`
- **Text Content**: `label`, `value`, `hint`, `tooltip`
- **Actions**: `onTap`, `onLongPress`, `onIncrease`, `onDecrease`, `onScrollUp`, etc.
- **Role**: `role` (SemanticsRole enum for explicit role specification)
- **Advanced**: `headingLevel`, `linkUrl`, `identifier`, `controlsNodes`

#### Important: The `identifier` Property

The `identifier` property is a **stable, developer-defined string** used to uniquely identify semantic nodes. Unlike auto-generated IDs, identifiers:
- Are set explicitly by developers: `Semantics(identifier: 'submit_button', ...)`
- Are converted to the `flt-semantics-identifier` attribute in the DOM
- Enable cross-element relationships via `controlsNodes`
- Remain stable across app rebuilds and updates

### 2.2 From Widget to Render Object

The `Semantics` widget creates a `RenderSemanticsAnnotations` render object that participates in the rendering pipeline. This render object builds a `SemanticsConfiguration` containing all the semantic properties.

### 2.3 Semantics Tree Construction

The Flutter framework builds a separate `SemanticsNode` tree alongside the render tree. The `SemanticsNode` objects:
- Represent semantic information for a portion of the UI
- Can merge child semantics or create explicit boundaries
- Are sent to the platform via a `SemanticsUpdate` message

---

## 3. Web Engine Layer

### 3.1 Receiving Semantics Updates

The web engine (located in `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/`) receives `SemanticsUpdate` messages containing `SemanticsNodeUpdate` objects.

**Key Data Structures:**

```dart
class SemanticsNodeUpdate {
  final int id;                        // Unique node identifier
  final ui.SemanticsFlags flags;       // Bitfield of semantic flags
  final int actions;                   // Bitfield of available actions
  final String? label;                 // Descriptive text
  final String? value;                 // Current value
  final String? hint;                  // Hint text
  final ui.Rect rect;                  // Position and size
  final ui.SemanticsRole role;         // Explicit role
  final int headingLevel;              // H1-H6 level
  final String? linkUrl;               // URL for links
  // ... many more fields
}
```

### 3.2 SemanticsObject Creation

Each `SemanticsNodeUpdate` is used to create or update a `SemanticsObject` (defined in `semantics.dart`). The `SemanticsObject`:
- Maintains the semantic state for a node
- Tracks which fields are "dirty" (changed)
- Manages the DOM element hierarchy
- Determines the appropriate semantic role

---

## 4. Role Detection and Mapping

### 4.1 The Role Detection Algorithm

The `_getEngineSemanticsRole()` method determines which semantic role to assign based on a priority system:

**Priority Order:**
1. **Platform View** (if `isPlatformView`)
2. **Explicit Role** (if `role` is explicitly set: tab, table, menu, dialog, form, etc.)
3. **Property-based Detection** (in order):
   - Heading (if `isHeading` or labeled header)
   - Text Field (if `isTextField`)
   - Incrementable (if has increase/decrease actions)
   - Image (if `isVisualOnly`)
   - Checkable (if has checked/toggled state)
   - Link (if `isLink`)
   - Button (if `isButton`)
   - Scrollable (if `isScrollContainer`)
   - Route (if `scopesRoute`)
   - Header (if `isHeader`)
   - Button-like (if tappable with no children)
4. **Generic** (fallback)

**Code Location:** `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/semantics.dart:2023-2119`

### 4.2 Role Creation

Once the role is determined, a corresponding `SemanticRole` subclass is instantiated:

```dart
SemanticRole _createSemanticRole(EngineSemanticsRole role) {
  return switch (role) {
    EngineSemanticsRole.button => SemanticButton(this),
    EngineSemanticsRole.checkable => SemanticCheckable(this),
    EngineSemanticsRole.textField => SemanticTextField(this),
    EngineSemanticsRole.heading => SemanticHeading(this),
    EngineSemanticsRole.link => SemanticLink(this),
    EngineSemanticsRole.image => SemanticImage(this),
    // ... 30+ role types
  };
}
```

---

## 5. Semantic Roles and DOM Generation

Each `SemanticRole` subclass is responsible for:
1. Creating the appropriate DOM element
2. Setting ARIA attributes
3. Managing interactive behaviors
4. Updating when semantic properties change

### 5.1 Example: Button Role

**File:** `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/tappable.dart`

```dart
class SemanticButton extends SemanticRole {
  SemanticButton(SemanticsObject semanticsObject)
    : super.withBasics(
        EngineSemanticsRole.button,
        semanticsObject,
        preferredLabelRepresentation: LabelRepresentation.domText,
      ) {
    addTappable();
    setAriaRole('button');
  }

  void update() {
    super.update();
    if (semanticsObject.enabledState() == EnabledState.disabled) {
      setAttribute('aria-disabled', 'true');
    } else {
      removeAttribute('aria-disabled');
    }
  }
}
```

**Generated DOM:**
```html
<flt-semantics role="button"
               id="flt-semantic-node-1"
               style="position: absolute; ...">
  Submit button
</flt-semantics>
```

### 5.2 Example: Checkbox Role

**File:** `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/checkable.dart`

Detection logic:
- Has `hasCheckedState` flag
- NOT in `isInMutuallyExclusiveGroup`
- NOT has `hasToggledState`

```dart
class SemanticCheckable extends SemanticRole {
  void update() {
    switch (_kind) {
      case _CheckableKind.checkbox:
        setAriaRole('checkbox');
      case _CheckableKind.radio:
        setAriaRole('radio');
      case _CheckableKind.toggle:
        setAriaRole('switch');
    }

    setAttribute('aria-checked',
      semanticsObject.flags.isChecked ? 'true' : 'false');
  }
}
```

**Generated DOM:**
```html
<flt-semantics role="checkbox"
               aria-checked="true"
               aria-label="Accept terms"
               flt-tappable
               style="...">
</flt-semantics>
```

### 5.3 Example: Heading Role

**File:** `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/heading.dart`

```dart
class SemanticHeading extends SemanticRole {
  DomElement createElement() {
    final element = createDomElement('h${semanticsObject.effectiveHeadingLevel}');
    element.style
      ..margin = '0'
      ..padding = '0'
      ..fontSize = '10px';
    return element;
  }
}
```

**Generated DOM:**
```html
<h2 id="flt-semantic-node-5"
    style="position: absolute; margin: 0; padding: 0; ...">
  Welcome to Our App
</h2>
```

### 5.4 Example: Link Role

**File:** `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/link.dart`

```dart
class SemanticLink extends SemanticRole {
  DomElement createElement() {
    final DomElement element = domDocument.createElement('a');
    element.style.display = 'block';
    return element;
  }

  void update() {
    if (semanticsObject.hasLinkUrl) {
      element.setAttribute('href', semanticsObject.linkUrl!);
    }
  }
}
```

**Generated DOM:**
```html
<a href="https://flutter.dev"
   id="flt-semantic-node-10"
   style="display: block; position: absolute; ...">
  Visit Flutter
</a>
```

### 5.5 Example: Text Field Role

**File:** `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/text_field.dart`

Creates actual HTML input elements:

```dart
class SemanticTextField extends SemanticRole {
  DomElement createElement() {
    return defaultTextEditingRoot.activeTextField?.editableElement
        ?? domDocument.createElement('input');
  }
}
```

**Generated DOM:**
```html
<input type="text"
       id="flt-semantic-node-15"
       aria-label="Email address"
       style="...">
```

---

## 6. Label Representation Strategies

The web engine uses three strategies for representing text labels:

### 6.1 AriaLabel Representation

Most efficient - uses `aria-label` attribute:

```html
<flt-semantics role="button" aria-label="Submit">
</flt-semantics>
```

**Usage:** Buttons, checkboxes, sliders, radios, most container elements

### 6.2 DomText Representation

Creates a text node inside the element:

```html
<flt-semantics role="button">
  Submit
</flt-semantics>
```

**Usage:** Buttons, links, headings (improves SEO and web crawling)

### 6.3 SizedSpan Representation

Creates a sized `<span>` element:

```html
<flt-semantics>
  <span style="...">Plain text content</span>
</flt-semantics>
```

**Usage:** Plain text nodes, table cells (ensures proper sizing)

---

## 7. Behavioral Components

Semantic roles can add various behaviors:

### 7.1 Focusable Behavior

Manages keyboard focus:
```javascript
element.setAttribute('tabindex', semanticsObject.isFocusable ? '0' : '-1');
```

### 7.2 Tappable Behavior

Listens for click events and forwards to Flutter:
```dart
class Tappable extends SemanticBehavior {
  void update() {
    if (_isListening) {
      owner.element.setAttribute('flt-tappable', '');
    }
  }
}
```

### 7.3 Live Region Behavior

Announces dynamic content changes:
```dart
if (semanticsObject.isLiveRegion) {
  element.setAttribute('aria-live', 'polite');
}
```

### 7.4 Selectable Behavior

For selectable items (tabs, list items):
```dart
setAttribute('aria-selected', semanticsObject.isSelected ? 'true' : 'false');
```

### 7.5 Expandable Behavior

For expandable/collapsible elements:
```dart
setAttribute('aria-expanded', semanticsObject.isExpanded ? 'true' : 'false');
```

---

## 8. Complex Examples

### 8.1 Radio Button Group

**Flutter Code:**
```dart
Semantics(
  container: true,
  child: Column(children: [
    Semantics(
      inMutuallyExclusiveGroup: true,
      checked: true,
      label: 'Option 1',
      child: Radio(...),
    ),
    Semantics(
      inMutuallyExclusiveGroup: true,
      checked: false,
      label: 'Option 2',
      child: Radio(...),
    ),
  ]),
)
```

**Generated DOM:**
```html
<flt-semantics role="radiogroup">
  <flt-semantics role="radio"
                 aria-checked="true"
                 aria-label="Option 1"
                 flt-tappable>
  </flt-semantics>
  <flt-semantics role="radio"
                 aria-checked="false"
                 aria-label="Option 2"
                 flt-tappable>
  </flt-semantics>
</flt-semantics>
```

### 8.2 Dialog with Heading

**Flutter Code:**
```dart
Semantics(
  scopesRoute: true,
  namesRoute: true,
  label: 'Settings Dialog',
  explicitChildNodes: true,
  child: Column(children: [
    Semantics(
      header: true,
      headingLevel: 1,
      label: 'Settings',
      child: Text('Settings'),
    ),
    // ... content
  ]),
)
```

**Generated DOM:**
```html
<flt-semantics role="dialog"
               aria-label="Settings Dialog"
               aria-live="polite">
  <h1 style="...">Settings</h1>
  <!-- other content -->
</flt-semantics>
```

### 8.3 Data Table

**Flutter Code:**
```dart
Semantics(
  role: SemanticsRole.table,
  child: Table(children: [
    TableRow(children: [
      Semantics(role: SemanticsRole.columnHeader, label: 'Name', ...),
      Semantics(role: SemanticsRole.columnHeader, label: 'Age', ...),
    ]),
    TableRow(children: [
      Semantics(role: SemanticsRole.cell, label: 'John', ...),
      Semantics(role: SemanticsRole.cell, label: '25', ...),
    ]),
  ]),
)
```

**Generated DOM:**
```html
<flt-semantics role="table">
  <flt-semantics role="row">
    <flt-semantics role="columnheader">Name</flt-semantics>
    <flt-semantics role="columnheader">Age</flt-semantics>
  </flt-semantics>
  <flt-semantics role="row">
    <flt-semantics role="cell">John</flt-semantics>
    <flt-semantics role="cell">25</flt-semantics>
  </flt-semantics>
</flt-semantics>
```

---

## 9. Advanced Features

### 9.1 Accessibility Announcements

Flutter can make announcements to screen readers:

```dart
SemanticsService.announce('Form submitted successfully', TextDirection.ltr);
```

**Web Implementation:**
Creates temporary `aria-live` regions:
```html
<flt-announcement-polite aria-live="polite" style="position: fixed; ...">
  <div>Form submitted successfully</div>
</flt-announcement-polite>
```

### 9.2 The `identifier` Property and Element References

The `identifier` property provides a **stable way to reference semantic elements**, both for testing and for cross-element relationships.

#### Setting an Identifier

**Flutter Code:**
```dart
Semantics(
  identifier: 'submit_button',
  button: true,
  label: 'Submit Form',
  child: ElevatedButton(...),
)
```

**Generated DOM:**
```html
<flt-semantics
  id="flt-semantic-node-42"
  flt-semantics-identifier="submit_button"
  role="button">
  Submit Form
</flt-semantics>
```

**Key Characteristics:**
- **Stable**: Unlike `flt-semantic-node-{n}` IDs, identifiers don't change between runs
- **Developer-Controlled**: You choose meaningful names like `'login_form'`, `'submit_button'`
- **Testing-Friendly**: Provides reliable selectors for automated tests
- **Unique**: Should be unique within your app to avoid confusion

#### Using Identifiers for Relationships

Identifiers enable elements to reference each other via the `controlsNodes` property:

**Flutter Code:**
```dart
// The controlled content
Semantics(
  identifier: 'panel_content',
  container: true,
  child: CollapsiblePanel(...),
)

// The controlling button
Semantics(
  identifier: 'expand_button',
  button: true,
  controlsNodes: ['panel_content'],  // References by identifier
  expanded: false,
  label: 'Expand Panel',
  child: IconButton(...),
)
```

**Generated DOM:**
```html
<!-- The controlled element -->
<flt-semantics
  id="flt-semantic-node-100"
  flt-semantics-identifier="panel_content">
  <!-- content -->
</flt-semantics>

<!-- The controlling element -->
<flt-semantics
  id="flt-semantic-node-50"
  flt-semantics-identifier="expand_button"
  role="button"
  aria-controls="flt-semantic-node-100"
  aria-expanded="false">
  Expand Panel
</flt-semantics>
```

**How It Works:**
1. Each identifier is mapped to its semantic node ID in `EngineSemanticsOwner.identifiersToIds`
2. When `controlsNodes` references an identifier, the engine looks up the corresponding node ID
3. The DOM `aria-controls` attribute receives the actual DOM ID (`flt-semantic-node-{id}`)
4. Screen readers can now understand the relationship between elements

### 9.3 Validation States

Form fields can indicate validation:

```dart
Semantics(
  textField: true,
  validationResult: SemanticsValidationResult.invalid,
  ...
)
```

**Generated DOM:**
```html
<input type="text" aria-invalid="true" ...>
```

---

## 10. DOM Element Structure

### 10.1 Base Structure

Every semantic element starts as:

```html
<flt-semantics
  id="flt-semantic-node-{id}"
  flt-semantics-identifier="{identifier}"  <!-- Optional, if set -->
  style="position: absolute;
         width: {width}px;
         height: {height}px;
         transform: matrix(...); ">
  <!-- content -->
</flt-semantics>
```

**Attributes:**
- `id`: Auto-generated, format `flt-semantic-node-{number}`, **may change between runs**
- `flt-semantics-identifier`: Developer-defined identifier (if specified), **stable across runs**
- ARIA attributes: `role`, `aria-label`, `aria-checked`, etc.
- Flutter-specific: `flt-tappable` (marks interactive elements)

### 10.2 Root Semantics Element

The root node (id=0) has special styling:

```html
<flt-semantics
  id="flt-semantic-node-0"
  style="filter: opacity(0%);     /* Make visually transparent */
         color: rgba(0,0,0,0);    /* Transparent text */
         ... ">
  <!-- all other semantic nodes -->
</flt-semantics>
```

This ensures semantic elements are accessible to screen readers but invisible to sighted users.

---

## 11. Complete Role Mappings

| Flutter Property/Flag | Web Engine Role | HTML Element | ARIA Role |
|----------------------|-----------------|--------------|-----------|
| `button: true` | SemanticButton | `<flt-semantics>` | `role="button"` |
| `checked: true/false` | SemanticCheckable | `<flt-semantics>` | `role="checkbox"` |
| `inMutuallyExclusiveGroup: true` | SemanticCheckable | `<flt-semantics>` | `role="radio"` |
| `toggled: true/false` | SemanticCheckable | `<flt-semantics>` | `role="switch"` |
| `textField: true` | SemanticTextField | `<input>` or `<textarea>` | (native) |
| `image: true` | SemanticImage | `<flt-semantics>` | `role="img"` |
| `header: true` + `headingLevel: N` | SemanticHeading | `<hN>` | (native) |
| `link: true` | SemanticLink | `<a>` | (native) |
| `slider: true` | SemanticIncrementable | `<input type="range">` | `role="slider"` |
| `role: SemanticsRole.tab` | SemanticTab | `<flt-semantics>` | `role="tab"` |
| `role: SemanticsRole.tabBar` | SemanticTabList | `<flt-semantics>` | `role="tablist"` |
| `role: SemanticsRole.dialog` | SemanticDialog | `<flt-semantics>` | `role="dialog"` |
| `role: SemanticsRole.table` | SemanticTable | `<flt-semantics>` | `role="table"` |
| `role: SemanticsRole.list` | SemanticList | `<flt-semantics>` | `role="list"` |
| `role: SemanticsRole.listItem` | SemanticListItem | `<flt-semantics>` | `role="listitem"` |
| `role: SemanticsRole.menu` | SemanticMenu | `<flt-semantics>` | `role="menu"` |
| `role: SemanticsRole.form` | SemanticForm | `<flt-semantics>` | `role="form"` |
| `scopesRoute: true` | SemanticRoute | `<flt-semantics>` | (container) |

---

## 12. Key Takeaways

1. **Separation of Concerns**: Flutter maintains a clean separation between visual rendering and semantic accessibility.

2. **Platform Abstraction**: The same `Semantics` widget works across all platforms (iOS, Android, Web, Desktop), with platform-specific implementations.

3. **Role-Based Architecture**: The web engine uses a role-based system where each semantic role is responsible for its DOM representation.

4. **Priority-Based Detection**: When no explicit role is provided, the engine uses a priority system to infer the most appropriate role from semantic flags.

5. **Behavioral Composition**: Roles can compose multiple behaviors (focusable, tappable, selectable) to create rich interactions.

6. **ARIA-First Approach**: The web engine leverages ARIA attributes extensively to provide rich accessibility information.

7. **Performance Optimization**: The system tracks "dirty" fields to minimize DOM updates and only regenerates what has changed.

8. **Screen Reader Transparency**: Semantic elements are rendered transparently so they don't interfere with the visual UI but remain accessible to assistive technologies.

---

## 13. Files Reference

### Key Framework Files:
- `packages/flutter/lib/src/widgets/basic.dart` - Semantics widget definition
- `packages/flutter/lib/src/semantics/semantics.dart` - SemanticsNode and core abstractions
- `packages/flutter/lib/src/rendering/proxy_box.dart` - RenderSemanticsAnnotations

### Key Web Engine Files:
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/semantics.dart` - Core SemanticsObject and role creation
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/tappable.dart` - Button role
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/checkable.dart` - Checkbox/Radio/Switch roles
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/text_field.dart` - Text field role
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/heading.dart` - Heading role
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/link.dart` - Link role
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/label_and_value.dart` - Label representation strategies
- `engine/src/flutter/lib/web_ui/lib/src/engine/semantics/accessibility.dart` - Accessibility announcements

---

## Conclusion

Flutter's conversion of `Semantics` widgets to browser accessibility tags is a sophisticated multi-layered process that ensures Flutter web apps are fully accessible. The system intelligently maps Flutter's platform-agnostic semantic descriptions to appropriate HTML elements and ARIA attributes, creating a rich accessibility tree that screen readers and other assistive technologies can navigate effectively.

The architecture is designed for maintainability, performance, and completeness—supporting everything from simple buttons to complex data tables, dialogs, and custom interactions. By understanding this pipeline, developers can create Flutter apps that provide excellent accessibility experiences for all users.

