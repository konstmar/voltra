package voltra.modifiers

import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Box
import kotlinx.serialization.json.JsonPrimitive
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
import voltra.styling.VoltraColorValue

/** Colour pairs and the native modifier scope, through the native compose/apply seam. */
@RunWith(RobolectricTestRunner::class)
class ModifierColorPairSeamTest {
    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()

    private val scope = VoltraModifierScope { error("no theme colours in this test") }

    private fun composePairBackground(scheme: Scheme) =
        NativeComposeSeam.compose(scheme) {
            Box(GlanceModifier.background(scope.colorProvider(VoltraColorValue.DayNight(Color.White, Color.Black)))) {}
        }

    @Before
    fun clearLogs() {
        ShadowLog.clear()
    }

    @Test
    @Config(sdk = [31])
    fun scopeMapsAPairToBothColoursFromAndroid12() {
        val views = composePairBackground(Scheme.DAY)

        assertEquals(listOf(white), views.applyUnder(Scheme.DAY).backgroundColors())
        assertEquals(listOf(black), views.applyUnder(Scheme.NIGHT).backgroundColors())
    }

    @Test
    @Config(sdk = [26])
    fun scopeMapsAPairToTheComposedSideBeforeAndroid12() {
        assertEquals(listOf(white), composePairBackground(Scheme.DAY).applyUnder(Scheme.DAY).backgroundColors())
        assertEquals(listOf(black), composePairBackground(Scheme.NIGHT).applyUnder(Scheme.NIGHT).backgroundColors())
    }

    @Test
    fun backgroundStillRejectsAPairUnderColor() {
        val modifiers = """[{"${'$'}type":"background","color":{"day":"#ffffff","night":"#000000"}}]"""
        val json = """{"t":5,"p":{"mods":${JsonPrimitive(modifiers)}},"c":{"t":18,"c":"Hi"}}"""

        val view = NativeComposeSeam.render(json, composeUnder = Scheme.DAY)

        assertEquals(emptyList<Int>(), view.backgroundColors())
        val warnings = ShadowLog.getLogsForTag("VoltraModifiers").map { it.msg }
        assertTrue(warnings.toString(), warnings.isNotEmpty() && warnings.all { it.contains("background") })
    }
}
