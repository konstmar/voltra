// Type tests for color pairs in chart mark colors and a chart's `foregroundStyleScale`. Each `@ts-expect-error`
// line must fail to compile; every other line must compile. Run by `pnpm --filter @use-voltra/android typecheck`.
import type { ComponentProps } from 'react'

import { AndroidDynamicColors, VoltraAndroid } from '@use-voltra/android'

type ChartProps = ComponentProps<typeof VoltraAndroid.Chart>
type BarMarkProps = ComponentProps<typeof VoltraAndroid.BarMark>

const pair = { day: '#ffffff', night: '#000000' } as const
const data = [{ x: 'a', y: 1 }]

export const chartColorPairTypeTests = [
  // A mark color is a color value: a string, a role token or a pair.
  (): BarMarkProps => ({ data, color: pair }),
  (): BarMarkProps => ({ data, color: AndroidDynamicColors.primary }),

  // Scale values mix pairs, literal colors and role tokens.
  (): ChartProps => ({ foregroundStyleScale: { A: pair, B: '#fff', C: AndroidDynamicColors.secondary } }),

  // A pair still needs both string sides.
  // @ts-expect-error `night` is missing.
  (): BarMarkProps => ({ data, color: { day: '#fff' } }),
  // @ts-expect-error sides must be strings.
  (): ChartProps => ({ foregroundStyleScale: { A: { day: 1, night: 2 } } }),
]
