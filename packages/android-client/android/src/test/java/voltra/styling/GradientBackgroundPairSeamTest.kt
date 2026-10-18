package voltra.styling

import android.graphics.Bitmap
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLog
import voltra.glance.NativeComposeSeam
import voltra.glance.NativeComposeSeam.Scheme
import voltra.glance.backgroundColors
import voltra.glance.imageBitmaps
import voltra.glance.renderers.renderGradientBitmap

/** `backgroundImage` gradients following the color scheme, through the native compose/apply seam. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GradientBackgroundPairSeamTest {
    private val red = 0xFFFF0000.toInt()
    private val blue = 0xFF0000FF.toInt()

    @Before
    fun clearLogs() {
        ShadowLog.clear()
    }

    /** A fixed-size Column whose style `backgroundImage` is [backgroundImage] (already JSON). */
    private fun column(backgroundImage: String) =
        """{"t":9,"p":{"s":{"w":40,"h":20,"bgi":$backgroundImage}},"c":{"t":18,"c":"Hi"}}"""

    private fun gradientUnder(
        json: String,
        scheme: Scheme,
    ): Bitmap? = NativeComposeSeam.render(json, composeUnder = scheme).imageBitmaps().singleOrNull()

    private fun Bitmap.centre(): Int = getPixel(width / 2, height / 2)

    @Test
    fun aWholeValuePairSelectsTheGradientByScheme() {
        val json =
            column(
                """{"day":"linear-gradient(to right, #ff0000, #ff0000)",
                   "night":"linear-gradient(to right, #0000ff, #0000ff)"}""",
            )

        assertEquals(red, requireNotNull(gradientUnder(json, Scheme.DAY)).centre())
        assertEquals(blue, requireNotNull(gradientUnder(json, Scheme.NIGHT)).centre())
        assertTrue(ShadowLog.getLogsForTag("JSGradientParser").isEmpty())
    }

    @Test
    @Config(sdk = [30])
    fun tokenStopsDrawADistinctBitmapPerScheme() {
        val json = column("\"linear-gradient(to right, ~p, ~p)\"")

        val day = requireNotNull(gradientUnder(json, Scheme.DAY))
        val night = requireNotNull(gradientUnder(json, Scheme.NIGHT))

        // Glance's baseline Material 3 primary below Android 12.
        assertEquals("day", Integer.toHexString(0xFF6750A4.toInt()), Integer.toHexString(day.centre()))
        assertEquals("night", Integer.toHexString(0xFFD0BCFF.toInt()), Integer.toHexString(night.centre()))
    }

    @Test
    fun theNightKeyNeverReturnsTheDayBitmap() {
        val gradient = requireNotNull(JSGradientParser.parse("linear-gradient(to right, #ff0000, #0000ff)"))

        fun renderUnder(scheme: Scheme) =
            renderGradientBitmap(
                context = NativeComposeSeam.context(scheme),
                gradient = gradient,
                layout = LayoutStyle(width = SizeValue.Fixed(40.dp), height = SizeValue.Fixed(20.dp)),
                widgetSize = null,
                backgroundArgb = 0,
                colors = intArrayOf(red, blue),
                positions = floatArrayOf(0f, 1f),
            )

        val day = renderUnder(Scheme.DAY)
        assertSame("the day key is cached", day, renderUnder(Scheme.DAY))
        // Identical inputs apart from the scheme still miss the day entry.
        assertNotSame(day, renderUnder(Scheme.NIGHT))
    }

    /** A fixed-size green Column whose style `backgroundImage` is [backgroundImage] (already JSON). */
    private fun columnOverGreen(backgroundImage: String) =
        """{"t":9,"p":{"s":{"w":40,"h":20,"bg":"#00ff00","bgi":$backgroundImage}},"c":{"t":18,"c":"Hi"}}"""

    @Test
    fun aStructurallyInvalidPairFallsBackToTheBackgroundColorWithOneWarning() {
        val json = columnOverGreen("""{"day":"linear-gradient(to right, #ff0000, #ff0000)","night":7}""")

        val view = NativeComposeSeam.render(json, composeUnder = Scheme.DAY)

        assertEquals(emptyList<Bitmap>(), view.imageBitmaps())
        assertEquals(listOf(0xFF00FF00.toInt()), view.backgroundColors())
        val warnings = ShadowLog.getLogsForTag("JSGradientParser").map { it.msg }
        assertEquals(warnings.toString(), 1, warnings.size)
        assertTrue(warnings.single(), warnings.single().contains("backgroundImage"))
    }

    @Test
    fun anUnparsableUnselectedSideDoesNotAffectTheSelectedOne() {
        val json = columnOverGreen("""{"day":"linear-gradient(to right, #ff0000, #ff0000)","night":"not a gradient"}""")

        val day = NativeComposeSeam.render(json, composeUnder = Scheme.DAY)
        assertEquals(red, requireNotNull(day.imageBitmaps().singleOrNull()).centre())

        // The night side fails like any unparsable gradient string: the background color shows.
        val night = NativeComposeSeam.render(json, composeUnder = Scheme.NIGHT)
        assertEquals(emptyList<Bitmap>(), night.imageBitmaps())
        assertEquals(listOf(0xFF00FF00.toInt()), night.backgroundColors())
        assertTrue(ShadowLog.getLogsForTag("JSGradientParser").isEmpty())
    }
}
