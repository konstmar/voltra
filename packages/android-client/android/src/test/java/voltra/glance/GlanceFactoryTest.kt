package voltra.glance

import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import voltra.glance.NativeComposeSeam.Scheme
import voltra.payload.ComponentTypeID

/**
 * `{ day, night }` pairs on the color props of the components `GlanceFactory` renders, through the
 * native compose/apply seam.
 */
@RunWith(RobolectricTestRunner::class)
class GlanceFactoryTest {
    private val red = 0xFFFF0000.toInt()
    private val blue = 0xFF0000FF.toInt()
    private val pair = """{"day":"#ff0000","night":"#0000ff"}"""

    /** A compressed payload per prop, and how to read that prop's color off the applied Views. */
    private val viewCases: Map<String, Pair<String, (View) -> Int?>> =
        mapOf(
            "Text color" to ("""{"t":18,"c":"Hi","p":{"c":$pair}}""" to { view -> view.textColors()["Hi"] }),
            "TitleBar textColor" to ("""{"t":19,"p":{"ttl":"T","txc":$pair}}""" to { view -> view.textColors()["T"] }),
            "FilledButton contentColor" to
                ("""{"t":0,"p":{"txt":"B","bg":"#888888","cc":$pair}}""" to { view -> view.textColors()["B"] }),
            "Scaffold backgroundColor" to
                (
                    """{"t":15,"p":{"bg":$pair},"c":{"t":18,"c":"x"}}""" to
                        { view -> view.backgroundColors().firstOrNull() }
                ),
        )

    @Before
    fun clearLogs() {
        ShadowLog.clear()
    }

    @Test
    @Config(sdk = [31])
    fun componentPropPairsSwapWithoutRecomposingFromAndroid12() {
        // Glance only tints progress bars from Android 12, so the progress color joins here.
        val linear = """{"t":12,"p":{"prg":0.5,"c":$pair}}""" to { view: View -> view.progressTints().firstOrNull() }

        for ((name, case) in viewCases + ("LinearProgressIndicator color" to linear)) {
            val (json, read) = case
            val views = NativeComposeSeam.compose(NativeComposeSeam.node(json), Scheme.DAY)

            assertEquals("$name by day", red, read(views.applyUnder(Scheme.DAY)))
            assertEquals("$name by night", blue, read(views.applyUnder(Scheme.NIGHT)))
        }
        assertTrue(ShadowLog.getLogsForTag("JSColorParser").isEmpty())
    }

    @Test
    @Config(sdk = [26])
    fun componentPropPairsPickTheSideWhenComposingBeforeAndroid12() {
        for ((name, case) in viewCases) {
            val (json, read) = case

            assertEquals("$name by day", red, read(NativeComposeSeam.render(json, composeUnder = Scheme.DAY)))
            assertEquals("$name by night", blue, read(NativeComposeSeam.render(json, composeUnder = Scheme.NIGHT)))
        }
        assertTrue(ShadowLog.getLogsForTag("JSColorParser").isEmpty())
    }

    /**
     * Every component color prop hands its raw value to the color parser: an invalid pair is
     * dropped with a warning naming the prop, instead of being skipped as a non-string or crashing
     * the composition. Props are written unshortened; decompression leaves unknown keys as they are.
     */
    @Test
    fun everyComponentColorPropHandsAPairToTheParser() {
        val invalid = """{"day":"~p","night":"#000000"}"""
        val icon = """{"assetName":"icon"}"""
        val cases =
            listOf(
                ComponentTypeID.IMAGE to listOf("colorFilter"),
                ComponentTypeID.IMAGE to listOf("tintColor"),
                ComponentTypeID.FILLED_BUTTON to listOf("backgroundColor", "contentColor"),
                ComponentTypeID.CIRCLE_ICON_BUTTON to listOf("backgroundColor", "contentColor"),
                ComponentTypeID.SQUARE_ICON_BUTTON to listOf("backgroundColor", "contentColor"),
                ComponentTypeID.OUTLINE_BUTTON to listOf("contentColor"),
                ComponentTypeID.TITLE_BAR to listOf("textColor", "iconColor"),
                ComponentTypeID.SWITCH to
                    listOf("thumbCheckedColor", "thumbUncheckedColor", "trackCheckedColor", "trackUncheckedColor"),
                ComponentTypeID.CHECK_BOX to listOf("checkedColor", "uncheckedColor"),
                ComponentTypeID.RADIO_BUTTON to listOf("checkedColor", "uncheckedColor"),
                ComponentTypeID.LINEAR_PROGRESS_INDICATOR to listOf("color", "backgroundColor"),
                ComponentTypeID.CIRCULAR_PROGRESS_INDICATOR to listOf("color"),
                ComponentTypeID.ARC_PROGRESS_INDICATOR to listOf("color", "trackColor"),
                ComponentTypeID.SCAFFOLD to listOf("backgroundColor"),
                ComponentTypeID.TEXT to listOf("color"),
            )

        for ((type, props) in cases) {
            ShadowLog.clear()
            val propsJson = props.joinToString(",") { "\"$it\":$invalid" }
            val json = """{"t":$type,"i":"x","c":"Hi","p":{"text":"Hi","title":"Hi","source":$icon,$propsJson}}"""

            NativeComposeSeam.render(json, composeUnder = Scheme.NIGHT)

            val warnings = ShadowLog.getLogsForTag("JSColorParser").map { it.msg }
            for (prop in props) {
                assertTrue(
                    "component $type: no warning names $prop in $warnings",
                    warnings.any { it.contains("for $prop:") },
                )
            }
        }
    }
}
