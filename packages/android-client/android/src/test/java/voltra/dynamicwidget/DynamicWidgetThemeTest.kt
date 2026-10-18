package voltra.dynamicwidget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.unit.ColorProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import voltra.glance.NativeComposeSeam
import voltra.glance.NativeComposeSeam.Scheme

@RunWith(RobolectricTestRunner::class)
class DynamicWidgetThemeTest {
    @Before
    fun clearLogs() {
        ShadowLog.clear()
    }

    @Test
    fun noThemeJsonIsNoThemeAndLogsNothing() {
        assertNull(DynamicWidgetTheme.parse(null))
        assertTrue(ShadowLog.getLogs().none { it.tag == "DynamicWidgetTheme" })
    }

    @Test
    fun aValidThemeGivesEachDefinedRoleItsDayAndNightColor() {
        val theme =
            DynamicWidgetTheme.parse(
                """
                {
                  "day": { "primary": "#6750A4", "surface": "rgb(255, 251, 254)" },
                  "night": { "primary": "#D0BCFF", "surface": "black" },
                  "dynamicColors": false
                }
                """.trimIndent(),
            )!!

        val colors = theme.toColorProviders(fallback)

        assertEquals(0xFF6750A4.toInt(), colors.primary.under(Scheme.DAY))
        assertEquals(0xFFD0BCFF.toInt(), colors.primary.under(Scheme.NIGHT))
        assertEquals(0xFFFFFBFE.toInt(), colors.surface.under(Scheme.DAY))
        assertEquals(0xFF000000.toInt(), colors.surface.under(Scheme.NIGHT))
        assertFalse(theme.dynamicColors)
    }

    @Test
    fun rolesAPartialThemeLeavesOutKeepTheFallbackPalette() {
        val theme =
            DynamicWidgetTheme.parse("""{"day":{"tertiary":"#00FF00"},"night":{"tertiary":"#008800"}}""")!!

        val colors = theme.toColorProviders(fallback)

        assertEquals(0xFF00FF00.toInt(), colors.tertiary.under(Scheme.DAY))
        assertSame(fallback.primary, colors.primary)
        assertSame(fallback.widgetBackground, colors.widgetBackground)
        assertSame(fallback.onErrorContainer, colors.onErrorContainer)
    }

    @Test
    fun dynamicColorsDefaultsToTrue() {
        val theme = DynamicWidgetTheme.parse("""{"day":{},"night":{}}""")!!

        assertTrue(theme.dynamicColors)
    }

    private fun ColorProvider.under(scheme: Scheme): Int = getColor(NativeComposeSeam.context(scheme)).toArgb()

    private val fallback: ColorProviders =
        colorProviders(
            primary = ColorProvider(Color(0xFF000001)),
            onPrimary = ColorProvider(Color(0xFF000002)),
            primaryContainer = ColorProvider(Color(0xFF000003)),
            onPrimaryContainer = ColorProvider(Color(0xFF000004)),
            secondary = ColorProvider(Color(0xFF000005)),
            onSecondary = ColorProvider(Color(0xFF000006)),
            secondaryContainer = ColorProvider(Color(0xFF000007)),
            onSecondaryContainer = ColorProvider(Color(0xFF000008)),
            tertiary = ColorProvider(Color(0xFF000009)),
            onTertiary = ColorProvider(Color(0xFF00000A)),
            tertiaryContainer = ColorProvider(Color(0xFF00000B)),
            onTertiaryContainer = ColorProvider(Color(0xFF00000C)),
            error = ColorProvider(Color(0xFF00000D)),
            errorContainer = ColorProvider(Color(0xFF00000E)),
            onError = ColorProvider(Color(0xFF00000F)),
            onErrorContainer = ColorProvider(Color(0xFF000010)),
            background = ColorProvider(Color(0xFF000011)),
            onBackground = ColorProvider(Color(0xFF000012)),
            surface = ColorProvider(Color(0xFF000013)),
            onSurface = ColorProvider(Color(0xFF000014)),
            surfaceVariant = ColorProvider(Color(0xFF000015)),
            onSurfaceVariant = ColorProvider(Color(0xFF000016)),
            outline = ColorProvider(Color(0xFF000017)),
            inverseOnSurface = ColorProvider(Color(0xFF000018)),
            inverseSurface = ColorProvider(Color(0xFF000019)),
            inversePrimary = ColorProvider(Color(0xFF00001A)),
            widgetBackground = ColorProvider(Color(0xFF00001B)),
        )
}
