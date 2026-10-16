// Type tests for colour pairs on component colour props. Each `@ts-expect-error` line must fail to compile; every
// other line must compile. Run by `pnpm --filter @use-voltra/android typecheck`.
import type { ComponentProps } from 'react'

import { AndroidDynamicColors, VoltraAndroid } from '@use-voltra/android'

const pair = { day: '#ffffff', night: '#000000' } as const

export const componentPropTypeTests = [
  // A representative set of component colour props takes a pair.
  (): ComponentProps<typeof VoltraAndroid.Text> => ({ color: pair }),
  (): ComponentProps<typeof VoltraAndroid.Image> => ({
    source: { assetName: 'icon' },
    tintColor: pair,
    colorFilter: pair,
  }),
  (): ComponentProps<typeof VoltraAndroid.FilledButton> => ({ text: 'Go', backgroundColor: pair, contentColor: pair }),
  (): ComponentProps<typeof VoltraAndroid.TitleBar> => ({
    title: 'Title',
    startIcon: { assetName: 'icon' },
    textColor: pair,
    iconColor: pair,
  }),
  (): ComponentProps<typeof VoltraAndroid.Switch> => ({
    id: 'switch',
    thumbCheckedColor: pair,
    thumbUncheckedColor: pair,
    trackCheckedColor: pair,
    trackUncheckedColor: pair,
  }),
  (): ComponentProps<typeof VoltraAndroid.LinearProgressIndicator> => ({ color: pair, backgroundColor: pair }),
  (): ComponentProps<typeof VoltraAndroid.ArcProgressIndicator> => ({ color: pair, trackColor: pair }),
  (): ComponentProps<typeof VoltraAndroid.Scaffold> => ({ backgroundColor: pair }),

  // Strings and role tokens keep working.
  (): ComponentProps<typeof VoltraAndroid.Text> => ({ color: '#ff0000' }),
  (): ComponentProps<typeof VoltraAndroid.CheckBox> => ({
    id: 'check',
    checkedColor: AndroidDynamicColors.primary,
    uncheckedColor: '#888',
  }),

  // A component prop pair still needs both string sides.
  // @ts-expect-error `night` is missing.
  (): ComponentProps<typeof VoltraAndroid.Text> => ({ color: { day: '#fff' } }),
  // @ts-expect-error sides must be strings.
  (): ComponentProps<typeof VoltraAndroid.CircularProgressIndicator> => ({ color: { day: 1, night: 2 } }),
]
