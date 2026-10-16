// Type tests for color pairs on style color keys. Each `@ts-expect-error` line must fail to compile; every other
// line must compile. Run by `pnpm --filter @use-voltra/android typecheck`.
import { AndroidDynamicColors } from '@use-voltra/android'
import type { VoltraAndroidTextStyle, VoltraAndroidViewStyle } from '@use-voltra/android'

export const styleTypeTests = [
  // Every style color key takes a pair.
  (): VoltraAndroidViewStyle => ({ backgroundColor: { day: '#fff', night: '#000' } }),
  (): VoltraAndroidViewStyle => ({ borderColor: { day: '#fff', night: '#000' } }),
  (): VoltraAndroidViewStyle => ({ shadowColor: { day: '#fff', night: '#000' } }),
  (): VoltraAndroidTextStyle => ({ color: { day: '#fff', night: '#000' } }),

  // Strings and role tokens keep working.
  (): VoltraAndroidViewStyle => ({ backgroundColor: '#fff', borderColor: AndroidDynamicColors.outline }),

  // A role token inside a style pair compiles: style objects don't infer literals, so the runtime rejects it instead.
  (): VoltraAndroidTextStyle => ({ color: { day: AndroidDynamicColors.onSurface, night: '#fff' } }),

  // A style pair still needs both string sides.
  // @ts-expect-error `night` is missing.
  (): VoltraAndroidViewStyle => ({ backgroundColor: { day: '#fff' } }),
  // @ts-expect-error sides must be strings.
  (): VoltraAndroidTextStyle => ({ color: { day: 1, night: 2 } }),
]
