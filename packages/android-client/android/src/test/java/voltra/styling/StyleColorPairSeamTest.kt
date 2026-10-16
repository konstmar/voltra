package voltra.styling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import voltra.glance.NativeComposeSeam
import voltra.glance.NativeComposeSeam.Scheme
import voltra.glance.applyUnder
import voltra.glance.backgroundColors
import voltra.glance.textColors

/** `{ day, night }` pairs on style color keys, through the native compose/apply seam. */
@RunWith(RobolectricTestRunner::class)
class StyleColorPairSeamTest {
    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()

    // A Column with a pair background holding a Text with the opposite pair as its color.
    private val card =
        """
        {"t":9,"p":{"s":{"bg":{"day":"#ffffff","night":"#000000"}}},
         "c":{"t":18,"c":"Hi","p":{"s":{"c":{"day":"#000000","night":"#ffffff"}}}}}
        """.trimIndent()

    @Before
    fun clearLogs() {
        ShadowLog.clear()
    }

    @Test
    @Config(sdk = [31])
    fun launcherSwapsBothColorsWithoutRecomposingFromAndroid12() {
        val views = NativeComposeSeam.compose(NativeComposeSeam.node(card), Scheme.DAY)

        val day = views.applyUnder(Scheme.DAY)
        assertEquals(listOf(white), day.backgroundColors())
        assertEquals(black, day.textColors()["Hi"])

        val night = views.applyUnder(Scheme.NIGHT)
        assertEquals(listOf(black), night.backgroundColors())
        assertEquals(white, night.textColors()["Hi"])
    }

    @Test
    @Config(sdk = [26])
    fun recompositionPicksTheSideBeforeAndroid12() {
        val day = NativeComposeSeam.render(card, composeUnder = Scheme.DAY)
        assertEquals(listOf(white), day.backgroundColors())
        assertEquals(black, day.textColors()["Hi"])

        val night = NativeComposeSeam.render(card, composeUnder = Scheme.NIGHT)
        assertEquals(listOf(black), night.backgroundColors())
        assertEquals(white, night.textColors()["Hi"])

        // The side is baked in when composing: a scheme flip needs a native recomposition.
        val stale = NativeComposeSeam.render(card, composeUnder = Scheme.DAY, applyUnder = Scheme.NIGHT)
        assertEquals(listOf(white), stale.backgroundColors())
    }

    @Test
    fun aPairReachesEveryStyleColorKeyWithoutError() {
        val pair = """{"day":"#ffffff","night":"#000000"}"""
        val json =
            """
            {"t":9,"p":{"s":{"bg":$pair,"bw":1,"bc":$pair,"shr":2,"shc":$pair}},
             "c":{"t":18,"c":"Hi","p":{"s":{"c":{"day":"#000000","night":"#ffffff"}}}}}
            """.trimIndent()

        val day = NativeComposeSeam.render(json, composeUnder = Scheme.DAY)
        val night = NativeComposeSeam.render(json, composeUnder = Scheme.NIGHT)

        assertEquals(listOf(white), day.backgroundColors())
        assertEquals(black, day.textColors()["Hi"])
        assertEquals(listOf(black), night.backgroundColors())
        assertEquals(white, night.textColors()["Hi"])
        assertTrue(ShadowLog.getLogsForTag("JSColorParser").isEmpty())
    }

    @Test
    fun anInvalidPairFallsBackToTheDefaultAndNamesTheKey() {
        val json = """{"t":9,"p":{"s":{"bg":{"day":"~p","night":"#000000"}}},"c":{"t":18,"c":"Hi"}}"""

        val view = NativeComposeSeam.render(json, composeUnder = Scheme.NIGHT)

        assertEquals(emptyList<Int>(), view.backgroundColors())
        val warnings = ShadowLog.getLogsForTag("JSColorParser")
        assertTrue(warnings.isNotEmpty())
        // Style conversion can run more than once per element, so count only what each warning says.
        assertTrue(warnings.map { it.msg }.toString(), warnings.all { it.msg.contains("backgroundColor") })
    }
}
