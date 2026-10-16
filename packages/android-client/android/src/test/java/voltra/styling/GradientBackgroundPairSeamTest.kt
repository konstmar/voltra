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

    @Test
    fun anInvalidPairFallsBackToTheBackgroundColorAndNamesTheKey() {
        val json =
            """
            {"t":9,"p":{"s":{"w":40,"h":20,"bg":"#00ff00",
             "bgi":{"day":"linear-gradient(to right, #ff0000, #ff0000)","night":"not a gradient"}}},
             "c":{"t":18,"c":"Hi"}}
            """.trimIndent()

        val view = NativeComposeSeam.render(json, composeUnder = Scheme.DAY)

        assertEquals(emptyList<Bitmap>(), view.imageBitmaps())
        assertEquals(listOf(0xFF00FF00.toInt()), view.backgroundColors())
        val warnings = ShadowLog.getLogsForTag("JSGradientParser")
        assertTrue(warnings.isNotEmpty())
        // Style conversion can run more than once per element, so count only what each warning says.
        assertTrue(warnings.map { it.msg }.toString(), warnings.all { it.msg.contains("backgroundImage") })
    }
}
