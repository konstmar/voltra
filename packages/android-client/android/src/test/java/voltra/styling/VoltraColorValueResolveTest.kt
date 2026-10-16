package voltra.styling

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.unit.ColorProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import voltra.glance.NativeComposeSeam
import voltra.glance.NativeComposeSeam.Scheme

/** `resolveColor(context, themeColors)`: one concrete color per scheme, outside composition. */
@RunWith(RobolectricTestRunner::class)
class VoltraColorValueResolveTest {
    private val day = NativeComposeSeam.context(Scheme.DAY)
    private val night = NativeComposeSeam.context(Scheme.NIGHT)

    /** A theme whose primary is red by day and blue by night; every other role is gray. */
    private val themeColors: ColorProviders =
        ColorProvider(Color.Gray).let { gray ->
            colorProviders(
                primary = androidx.glance.color.ColorProvider(day = Color.Red, night = Color.Blue),
                onPrimary = gray,
                primaryContainer = gray,
                onPrimaryContainer = gray,
                secondary = gray,
                onSecondary = gray,
                secondaryContainer = gray,
                onSecondaryContainer = gray,
                tertiary = gray,
                onTertiary = gray,
                tertiaryContainer = gray,
                onTertiaryContainer = gray,
                error = gray,
                errorContainer = gray,
                onError = gray,
                onErrorContainer = gray,
                background = gray,
                onBackground = gray,
                surface = gray,
                onSurface = gray,
                surfaceVariant = gray,
                onSurfaceVariant = gray,
                outline = gray,
                inverseOnSurface = gray,
                inverseSurface = gray,
                inversePrimary = gray,
                widgetBackground = gray,
            )
        }

    @Test
    fun aPairPicksTheSideForTheContextsNightBit() {
        val pair = VoltraColorValue.DayNight(day = Color.White, night = Color.Black)

        assertEquals(Color.White, pair.resolveColor(day, themeColors))
        assertEquals(Color.Black, pair.resolveColor(night, themeColors))
    }

    @Test
    fun aThemeRoleReadsTheGivenThemeColors() {
        val primary = VoltraColorValue.Dynamic(VoltraThemeColorRole.PRIMARY)

        assertEquals(Color.Red, primary.resolveColor(day, themeColors))
        assertEquals(Color.Blue, primary.resolveColor(night, themeColors))
    }

    @Test
    fun aStaticColorIsTheSameInBothSchemes() {
        val green = VoltraColorValue.Static(Color.Green)

        assertEquals(Color.Green, green.resolveColor(day, themeColors))
        assertEquals(Color.Green, green.resolveColor(night, themeColors))
    }
}
