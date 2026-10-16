// Type tests for color pairs. Each `@ts-expect-error` line must fail to compile; every other line must compile.
// Run by `pnpm --filter @use-voltra/android typecheck`.
import { AndroidDynamicColors, VoltraAndroid } from '@use-voltra/android'
import type * as Root from '@use-voltra/android'
import type { AndroidColorPair } from '@use-voltra/android'

const { background } = VoltraAndroid.modifiers

export const typeTests = [
  // A developer can type their own pair constants.
  (): AndroidColorPair => ({ day: '#ffffff', night: '#000000' }),

  // A pair needs both sides, and both sides must be strings.
  // @ts-expect-error `night` is missing.
  (): AndroidColorPair => ({ day: '#fff' }),
  // @ts-expect-error sides must be strings.
  (): AndroidColorPair => ({ day: 1, night: 2 }),

  // `background` accepts a string, a role token or a pair of static colors.
  () => background({ day: '#ffffff', night: '#000000' }),
  () => background('#fff'),
  () => background(AndroidDynamicColors.primary),

  // `background` rejects a role token inside a pair where the literal is inferred.
  // @ts-expect-error role token on the day side.
  () => background({ day: AndroidDynamicColors.primary, night: '#000' }),
  // @ts-expect-error role token on the night side.
  () => background({ day: '#fff', night: AndroidDynamicColors.surface }),

  // The 2.4.0 paths keep resolving under VoltraAndroid.modifiers.
  (pair: AndroidColorPair): VoltraAndroid.modifiers.DayNightColors => pair,
  (legacy: VoltraAndroid.modifiers.DayNightColors): AndroidColorPair => legacy,
  (): VoltraAndroid.modifiers.StaticColor<'#fff'> => '#fff',
  // @ts-expect-error StaticColor is reachable only under VoltraAndroid.modifiers, not from the package root.
  (): Root.StaticColor<'#fff'> => '#fff',
  // @ts-expect-error StaticColor resolves to never for a role token.
  (): VoltraAndroid.modifiers.StaticColor<typeof AndroidDynamicColors.primary> => AndroidDynamicColors.primary,

  // Known StaticColor bypasses. These document a limitation, not a promise: the compile-time token check only works
  // where literals are inferred, and the runtime rejects tokens inside a pair either way.
  () => background<string, string>({ day: AndroidDynamicColors.primary, night: '#000' }),
  (widened: string) => background({ day: widened, night: '#000' }),
]
