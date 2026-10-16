---
title: "Styling"
description: "The React Native style properties Voltra supports on Android and how they map to Jetpack Compose Glance modifiers."
---

You can style Voltra components on Android using React Native-style `style` props. These properties are automatically converted to Jetpack Compose Glance modifiers. 

For Android system-aware colors, use [`AndroidDynamicColors`](/android/development/dynamic-colors) from `@use-voltra/android` instead of snapshotting palette values in JavaScript.

:::caution[Glance Limitations]
Android widgets are built using **Jetpack Compose Glance**, which has a significantly more limited styling API compared to standard Compose or SwiftUI. Many common React Native style properties are either not supported or have limited support.
:::

## Supported Properties

The following React Native style properties are supported on Android:

### Layout

- `width`, `height` - Fixed dimensions (number values in dp) or `"100%"` to fill available space.
- `flex`, `flexGrow` - Flex weight. When > 0, the component will take up a proportional amount of space in its parent container (maps to `.defaultWeight()` in Glance).
- `padding` - Uniform padding on all edges.
- `paddingTop`, `paddingBottom`, `paddingLeft`, `paddingRight` - Individual edge padding.
- `paddingHorizontal`, `paddingVertical` - Horizontal and vertical padding.
- `visibility` - Controls component visibility (`"visible"`, `"hidden"`, or `"invisible"`).

### Visual Style

- `backgroundColor` - Background color (hex strings, color names, `AndroidDynamicColors.*` tokens, or a [`{ day, night }` pair](#day-and-night-colors)).
- `backgroundImage` - CSS gradient background. Supports `linear-gradient(...)`, `radial-gradient(...)`, and `conic-gradient(...)`, or a [`{ day, night }` pair of gradients](#gradients-in-light-and-dark).
- `borderRadius` - Corner radius in dp. **Note:** Requires Android 12+ (API 31). On older versions it is ignored, except on an `Image` with a fixed numeric `width` and `height`. On an `Image`, the radius rounds the image's box, not the picture inside it; see [Image](/android/components/visual#image).

### Text

- `fontSize` - Font size in sp.
- `fontWeight` - Supports `"normal"` and `"bold"`.
- `fontFamily` - Font family name. Built-in values: `"monospace"`, `"serif"`, `"sans-serif"`, `"cursive"`. For custom fonts, see [Custom Fonts](/android/development/custom-fonts).
- `color` - Text color (literal colors, `AndroidDynamicColors.*`, or a [`{ day, night }` pair](#day-and-night-colors)).
- `textDecorationLine` - Supports `"underline"` and `"line-through"`.
- `textAlign` - Alignment of text within the component (`"left"`, `"center"`, `"right"`).
- `numberOfLines` - Limits the number of lines displayed.

### Day and night colors

`backgroundColor` and text `color` also take a pair of colors, one for the light system theme and one for the dark one. The type is `AndroidColorPair`, exported from `@use-voltra/android`:

```tsx
import type { AndroidColorPair } from '@use-voltra/android'

const cardBackground: AndroidColorPair = { day: '#ffffff', night: 'rgba(32, 33, 36, 0.9)' }

<Column style={{ backgroundColor: cardBackground }}>
  <Text style={{ color: { day: '#202124', night: 'white' } }}>Hello</Text>
</Column>
```

Your widget's JavaScript produces the same output in both themes; Android picks the side. On Android 12+ the launcher receives both colors and swaps them itself when the theme changes, even when your app isn't running. On older versions the color is picked when the widget is drawn: Voltra redraws widgets on a theme change while your app's process is alive, and otherwise the widget keeps the previous side until its next update.

- Each side takes any literal color the string form accepts: hex with or without alpha (`#fff`, `#ffffff80`), `rgb()`/`rgba()`, `hsl()`/`hsla()`, color names and `transparent`. Alpha is kept per side.
- Both `day` and `night` are required. Other keys are ignored.
- `AndroidDynamicColors.*` tokens are not allowed inside a pair. TypeScript does not catch a token in a style pair; it is rejected when the widget renders.
- An invalid pair (a missing or empty side, a token, or a value that isn't a color) is dropped as a whole with one warning in Logcat, and the property falls back to its default, as an unparsable color string does.
- `borderColor` and `shadowColor` accept pairs too, but borders and shadows are not drawn yet.
- Component color props take pairs as well: `Text` `color`, `Image` `tintColor`/`colorFilter`, the button, `Switch`, `CheckBox` and `RadioButton` colors, the progress indicator colors, and the `TitleBar` and `Scaffold` colors. A `Text` `color` prop wins over the style's `color`.
- Gradients follow the theme too, either through tokens in their stops or as a pair of whole gradients. See [Gradients in light and dark](#gradients-in-light-and-dark).

### Image Specific

In addition to general styles, `Image` components support:

- `resizeMode` - `"cover"`, `"contain"`, `"stretch"`, `"repeat"`, or `"center"`.
- `contentScale` - `"crop"`, `"cover"`, `"fit"`, `"contain"`, `"fill-bounds"`, or `"stretch"`.
- `alpha` - Opacity of the image (0.0 to 1.0).
- `colorFilter` - Applies a color filter to the image.

## Dynamic colors

Android widgets can use semantic Material color roles (`AndroidDynamicColors.*`) so they follow the system palette even when the app isn't running. See [Dynamic Colors](/android/development/dynamic-colors) for the full role list, examples, and server-rendering behavior.

## Gradient Backgrounds

Android widgets support gradient backgrounds through the camel-case `style.backgroundImage` property.

```tsx
import { VoltraAndroid } from '@use-voltra/android'

const element = (
  <VoltraAndroid.Box
    style={{
      width: '100%',
      height: '100%',
      padding: 16,
      borderRadius: 16,
      backgroundColor: '#0F172A',
      backgroundImage: 'linear-gradient(to right, #22D3EE 0%, #6366F1 100%)',
    }}
  >
    <VoltraAndroid.Text
      style={{
        color: '#F8FAFC',
        fontSize: 18,
        fontWeight: 'bold',
      }}
    >
      Gradient Widget
    </VoltraAndroid.Text>
  </VoltraAndroid.Box>
)
```

Supported gradient functions are `linear-gradient(...)`, `radial-gradient(...)`, and `conic-gradient(...)`. Repeating gradients, malformed gradients, unsupported color tokens, and invalid stop positions are ignored. If `backgroundColor` is also provided, Android paints it behind transparent gradient pixels and uses it as a fallback when a gradient cannot be rendered.

Use `backgroundImage`, not `background-image`. Gradient bitmaps are generated natively during widget rendering and capped before being passed to Glance, so the bitmap does not control layout size.

### Gradients in light and dark

A gradient can follow the system theme in two ways:

- **Tokens in the stops.** Stops accept `AndroidDynamicColors.*` tokens, so the gradient uses the theme's colors in each scheme. Stops do not take `{ day, night }` pairs, because a gradient is a single CSS string.
- **A pair of whole gradients.** `backgroundImage` takes `{ day: '<css gradient>', night: '<css gradient>' }`, and Android draws the side for the current theme. The two sides can differ in anything: direction, kind, stops or stop count.

```tsx
import { AndroidDynamicColors, VoltraAndroid } from '@use-voltra/android'

// Theme colors in the stops
<VoltraAndroid.Box
  style={{
    backgroundImage: `linear-gradient(to right, ${AndroidDynamicColors.primary}, ${AndroidDynamicColors.tertiary})`,
  }}
/>

// A different gradient per theme
<VoltraAndroid.Box
  style={{
    backgroundImage: {
      day: 'linear-gradient(to right, #e3f2fd, #90caf9)',
      night: 'radial-gradient(circle at center, #1a237e, #000000)',
    },
  }}
/>
```

Both sides are required, and each must be a gradient Android can draw. If either side is missing, empty or can't be parsed, the pair is dropped with one warning in Logcat, and `backgroundColor` shows instead, as it does for any gradient that can't be drawn.

A gradient is drawn into a bitmap when the widget renders, in the colors of the theme at that moment, on every Android version. When the theme changes, Voltra redraws widgets while your app's process is alive; otherwise the gradient keeps the previous theme's colors until the widget's next update.

## Limitations

The following properties are **NOT supported** on Android due to Glance limitations:

- **Margins:** `margin`, `marginTop`, etc. are not part of Android style types. If you need margin-like outside spacing, use `VoltraAndroid.Spacer` between elements.
- **Borders:** `borderWidth` and `borderColor` are not yet implemented.
- **Shadows:** `shadowColor`, `shadowOffset`, `shadowOpacity`, and `shadowRadius` are not supported.
- **Positioning:** Absolute positioning (`top`, `left`, `zIndex`) is not supported. Use stack alignments and spacers.
- **Transforms:** `transform` (rotate, scale, etc.) is not supported.
- **Opacity:** The general `style.opacity` property is not supported (except for the `alpha` prop on `Image`).
- **Dimensions:** `minWidth`, `maxWidth`, `minHeight`, `maxHeight`, and `aspectRatio` are not supported.
- **Text Effects:** `letterSpacing`, `fontVariant`, and custom `lineHeight` are not supported.

## Example

```tsx
import { VoltraAndroid } from '@use-voltra/android'

const element = (
  <VoltraAndroid.Column
    style={{
      padding: 16,
      backgroundColor: '#101828',
    }}
  >
    <VoltraAndroid.Text
      style={{
        color: '#F8FAFC',
        fontSize: 18,
        fontWeight: 'bold',
      }}
    >
      Android Widget Text
    </VoltraAndroid.Text>
  </VoltraAndroid.Column>
)
```

## Sharing styles with `StyleSheet`

Widget files can import `StyleSheet` and `Platform` from `react-native`, so styles can live
outside the element tree exactly as they do in the rest of your app:

```tsx
import { Platform, StyleSheet } from 'react-native'
import { VoltraAndroid } from '@use-voltra/android'

const styles = StyleSheet.create({
  container: {
    padding: 16,
    backgroundColor: '#101828',
  },
  title: {
    color: '#F8FAFC',
    fontSize: 18,
    fontWeight: 'bold',
  },
})

const element = (
  <VoltraAndroid.Column style={styles.container}>
    <VoltraAndroid.Text style={styles.title}>{Platform.OS}</VoltraAndroid.Text>
  </VoltraAndroid.Column>
)
```

Widget code does not run against the React Native runtime — at build time it is evaluated in a
Node sandbox, and Dynamic Widgets run on device in a separate JS engine with no bridge. Only the
parts of `react-native` that are pure data manipulation are therefore available:

- `StyleSheet.create`, `StyleSheet.flatten`, `StyleSheet.compose`, `StyleSheet.absoluteFill`,
  `StyleSheet.absoluteFillObject`, and `StyleSheet.hairlineWidth`.
- `Platform.OS` and `Platform.select`. Inside a widget, `Platform.OS` is the platform the widget is
  being built for, so `Platform.select` picks the same branch at build time and on device.

Anything else imported from `react-native` — components, `Dimensions`, `Animated`, `PixelRatio` —
is rejected with a message naming the symbol. Build steps that evaluate your widget
(`voltra apply` and `expo prebuild`) fail outright; a symbol that only appears on a branch those
steps never reach throws the same message when the widget renders, rather than reading as
`undefined`. Deep imports such as `react-native/Libraries/...` always fail the build. Use the
`VoltraAndroid` components for everything visual.
