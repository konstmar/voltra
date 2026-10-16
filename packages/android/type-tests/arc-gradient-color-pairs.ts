// Type tests for colour pairs inside an arc indicator's `gradientColors`. Each `@ts-expect-error` line must fail to
// compile; every other line must compile. Run by `pnpm --filter @use-voltra/android typecheck`.
import type { ComponentProps } from 'react'

import { AndroidDynamicColors, VoltraAndroid } from '@use-voltra/android'

type ArcProps = ComponentProps<typeof VoltraAndroid.ArcProgressIndicator>

const pair = { day: '#ffffff', night: '#000000' } as const

export const arcGradientTypeTests = [
  // Pairs, literal colours and role tokens mix freely as elements.
  (): ArcProps => ({ gradientColors: [pair, '#fff', AndroidDynamicColors.primary] }),
  (): ArcProps => ({ gradientColors: ['#22C55E', '#3B82F6'] }),

  // An element pair still needs both string sides.
  // @ts-expect-error `night` is missing.
  (): ArcProps => ({ gradientColors: [{ day: '#fff' }, '#000'] }),
  // @ts-expect-error sides must be strings.
  (): ArcProps => ({ gradientColors: [{ day: 1, night: 2 }, '#000'] }),
]
