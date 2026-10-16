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

/**
 * `{ day, night }` pairs in chart colours, through the native compose/apply seam. A chart is a bitmap
 * baked at composition on every API level, so each scheme is composed on its own.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChartColorPairSeamTest {
    private val red = 0xFFFF0000.toInt()
    private val blue = 0xFF0000FF.toInt()

    /** First colour of the chart's default palette, used when a mark or series has no colour. */
    private val defaultColour = 0xFF4E79A7.toInt()

    @Before
    fun reset() {
        ShadowLog.clear()
    }

    /**
     * A 100x100 chart with axes and grids hidden, so the bars are the only opaque pixels. [marks] and
     * [scale] are the JSON arrays the renderer encodes as strings.
     */
    private fun chart(
        marks: String,
        scale: String? = null,
    ): String {
        val scaleProp = scale?.let { ""","fss":"${it.escaped()}"""" } ?: ""
        return """{"t":20,"p":{"s":{"w":100,"h":100},"mrk":"${marks.escaped()}"$scaleProp,""" +
            """"xav":"hidden","yav":"hidden","xAxisGridVisible":false,"yAxisGridVisible":false}}"""
    }

    private fun String.escaped() = replace("\"", "\\\"")

    private fun barColoursUnder(
        json: String,
        scheme: Scheme,
    ): Set<String> =
        NativeComposeSeam
            .render(json, composeUnder = scheme)
            .imageBitmaps()
            .single()
            .opaqueColours()

    /** Colours of the fully opaque pixels, which antialiasing leaves untouched inside the bars. */
    private fun Bitmap.opaqueColours(): Set<String> {
        val pixels = IntArray(width * height).also { getPixels(it, 0, width, 0, 0, width, height) }
        return pixels.filter { it ushr 24 == 0xFF }.map(::hex).toSet()
    }

    private fun hex(color: Int) = Integer.toHexString(color)

    @Test
    fun barsFollowAMarkColourPairInEachScheme() {
        val json = chart("""[["bar",[["a",1]],{"c":{"day":"#ff0000","night":"#0000ff"}}]]""")

        assertEquals("day", setOf(hex(red)), barColoursUnder(json, Scheme.DAY))
        assertEquals("night", setOf(hex(blue)), barColoursUnder(json, Scheme.NIGHT))
        assertTrue(ShadowLog.getLogsForTag("JSColorParser").isEmpty())
    }

    @Test
    fun barsFollowAForegroundStyleScalePairInEachScheme() {
        val json =
            chart(
                marks = """[["bar",[["a",1,"A"]],{}]]""",
                scale = """[["A",{"day":"#ff0000","night":"#0000ff"}]]""",
            )

        assertEquals("day", setOf(hex(red)), barColoursUnder(json, Scheme.DAY))
        assertEquals("night", setOf(hex(blue)), barColoursUnder(json, Scheme.NIGHT))
        assertTrue(ShadowLog.getLogsForTag("JSColorParser").isEmpty())
    }

    @Test
    fun anInvalidMarkColourPairFallsBackToTheDefaultColourWithOneWarning() {
        val json = chart("""[["bar",[["a",1]],{"c":{"day":"#ff0000","night":"~p"}}]]""")

        assertEquals(setOf(hex(defaultColour)), barColoursUnder(json, Scheme.DAY))
        val expected = "Ignoring invalid colour pair for color: night must be a literal colour string, got \"~p\""
        assertEquals(listOf(expected), ShadowLog.getLogsForTag("JSColorParser").map { it.msg })
    }

    @Test
    fun anInvalidForegroundStyleScalePairFallsBackToTheDefaultColourWithOneWarning() {
        val json =
            chart(
                marks = """[["bar",[["a",1,"A"]],{}]]""",
                scale = """[["A",{"day":"#ff0000"}]]""",
            )

        assertEquals(setOf(hex(defaultColour)), barColoursUnder(json, Scheme.DAY))
        val expected =
            "Ignoring invalid colour pair for foregroundStyleScale: night must be a literal colour string, got nothing"
        assertEquals(listOf(expected), ShadowLog.getLogsForTag("JSColorParser").map { it.msg })
    }
}
