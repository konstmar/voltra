---
'@use-voltra/android': minor
'@use-voltra/android-client': minor
'@use-voltra/metro': minor
---

An Android Dynamic Widget can now export a `theme` next to its render function, giving `day` and `night` colors for any `AndroidDynamicColors` role. On Android 11 and older, the widget's role tokens then resolve to the theme instead of the Material baseline colors. With `dynamicColors: false`, the theme also wins over the system palette on Android 12 and later. Roles the theme leaves out keep their usual colors. A malformed theme is ignored with a warning in Logcat, so the widget still renders. The new `AndroidWidgetTheme` and `AndroidThemeColorRole` types describe it.
