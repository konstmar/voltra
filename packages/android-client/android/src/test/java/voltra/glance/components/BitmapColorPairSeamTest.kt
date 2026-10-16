package voltra.glance.components

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLog
import voltra.glance.NativeComposeSeam
import voltra.glance.NativeComposeSeam.Scheme
import voltra.glance.imageBitmaps
import voltra.glance.renderers.arc.ArcBitmapCache
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * `{ day, night }` pairs in content drawn as a bitmap at composition (arc indicators and custom-font
 * text), through the native compose/apply seam. The bitmap is baked on every API level, so each
 * scheme is composed on its own, with the arc bitmap cache cleared in between as a scheme flip does.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BitmapColorPairSeamTest {
    private val red = 0xFFFF0000.toInt()
    private val blue = 0xFF0000FF.toInt()
    private val green = 0xFF00FF00.toInt()

    @Before
    fun reset() {
        ShadowLog.clear()
        ArcBitmapCache.clear()
    }

    /**
     * A 100x100 arc, half filled with a 20dp stroke, whose solid `color` is green so a dropped
     * gradient shows. [gradientColors] is the JSON array the renderer encodes as a string.
     */
    private fun arc(gradientColors: String): String {
        val encoded = gradientColors.replace("\"", "\\\"")
        return """{"t":21,"p":{"s":{"w":100,"h":100},"prg":0.5,"sw":20,"c":"#00ff00","gc":"$encoded"}}"""
    }

    private fun arcBitmapUnder(
        json: String,
        scheme: Scheme,
    ): Bitmap {
        ArcBitmapCache.clear()
        return NativeComposeSeam.render(json, composeUnder = scheme).imageBitmaps().single()
    }

    /**
     * Pixel on the stroke centreline just before the filled end. Half of the default 270 degree
     * sweep from 135 degrees ends at 270 degrees, measured the way `drawArc` measures.
     */
    private fun Bitmap.filledEnd(): Int {
        val centre = width / 2f
        val radius = centre - (20f * width / 100f) / 2f
        val radians = Math.toRadians(265.0)
        return getPixel((centre + radius * cos(radians)).roundToInt(), (centre + radius * sin(radians)).roundToInt())
    }

    private fun hex(color: Int) = Integer.toHexString(color)

    @Test
    fun theArcFilledEndFollowsAGradientColourPairInEachScheme() {
        val json = arc("""[{"day":"#ff0000","night":"#0000ff"},{"day":"#ff0000","night":"#0000ff"}]""")

        assertEquals("day", hex(red), hex(arcBitmapUnder(json, Scheme.DAY).filledEnd()))
        assertEquals("night", hex(blue), hex(arcBitmapUnder(json, Scheme.NIGHT).filledEnd()))
        assertTrue(ShadowLog.getLogsForTag("JSColorParser").isEmpty())
    }

    @Test
    fun anInvalidGradientPairFallsBackToTheSolidColourAndNamesTheProp() {
        val json = arc("""[{"day":"#ff0000","night":"~p"},"#0000ff"]""")

        assertEquals(hex(green), hex(arcBitmapUnder(json, Scheme.DAY).filledEnd()))
        val warnings = ShadowLog.getLogsForTag("JSColorParser").map { it.msg }
        val expected =
            "Ignoring invalid colour pair for gradientColors: night must be a literal colour string, got \"~p\""
        assertEquals(listOf(expected), warnings)
    }

    /** Colours of the fully opaque pixels, which antialiasing leaves untouched inside the glyphs. */
    private fun Bitmap.opaqueColours(): Set<String> {
        val pixels = IntArray(width * height).also { getPixels(it, 0, width, 0, 0, width, height) }
        return pixels.filter { it ushr 24 == 0xFF }.map(::hex).toSet()
    }

    @Test
    fun customFontTextIsDrawnInEachSidesColour() {
        val pair = """{"day":"#ff0000","night":"#0000ff"}"""
        // A font family plus `renderAsBitmap` sends Text down the custom-font bitmap path.
        val font = """"ff":"serif","fs":48"""
        val cases =
            mapOf(
                "style color" to """{"t":18,"c":"HH","p":{"renderAsBitmap":true,"s":{$font,"c":$pair}}}""",
                "color prop" to """{"t":18,"c":"HH","p":{"renderAsBitmap":true,"c":$pair,"s":{$font}}}""",
            )

        for ((name, json) in cases) {
            val day = NativeComposeSeam.render(json, composeUnder = Scheme.DAY).imageBitmaps().single()
            val night = NativeComposeSeam.render(json, composeUnder = Scheme.NIGHT).imageBitmaps().single()

            assertEquals("$name by day", setOf(hex(red)), day.opaqueColours())
            assertEquals("$name by night", setOf(hex(blue)), night.opaqueColours())
        }
        assertTrue(ShadowLog.getLogsForTag("JSColorParser").isEmpty())
    }
}
