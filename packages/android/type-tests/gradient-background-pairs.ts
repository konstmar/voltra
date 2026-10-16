// Type tests for a `{ day, night }` pair of CSS gradients on `backgroundImage`. Each `@ts-expect-error` line must fail
// to compile; every other line must compile. Run by `pnpm --filter @use-voltra/android typecheck`.
import type { VoltraAndroidViewStyle } from '@use-voltra/android'

export const gradientTypeTests = [
  // A whole-value pair: each side is a full CSS gradient.
  (): VoltraAndroidViewStyle => ({
    backgroundImage: {
      day: 'linear-gradient(to right, #ffffff, #90caf9)',
      night: 'linear-gradient(to right, #000000, #0d47a1)',
    },
  }),

  // A single gradient keeps working, with role tokens in its stops.
  (): VoltraAndroidViewStyle => ({ backgroundImage: 'linear-gradient(to right, ~p, ~s)' }),

  // A gradient pair still needs both string sides.
  // @ts-expect-error `night` is missing.
  (): VoltraAndroidViewStyle => ({ backgroundImage: { day: 'linear-gradient(red, blue)' } }),
  // @ts-expect-error sides must be strings.
  (): VoltraAndroidViewStyle => ({ backgroundImage: { day: 1, night: 2 } }),
]
