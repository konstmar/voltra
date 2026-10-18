package voltra.dynamicwidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import voltra.glance.GlanceFactory
import voltra.glance.NativeComposeSeam
import voltra.glance.NativeComposeSeam.Scheme
import voltra.glance.applyUnder
import voltra.glance.textColors

/** The theme a Dynamic Widget exports, applied around its rendered node, through the compose/apply seam. */
@RunWith(RobolectricTestRunner::class)
class DynamicWidgetThemedContentTest {
    private val primaryText = """{"t":18,"c":"Hi","p":{"s":{"c":"~p"}}}"""
    private val theme = """{"day":{"primary":"#123456"},"night":{"primary":"#abcdef"}}"""

    @Test
    @Config(sdk = [26])
    fun aPrimaryTokenTakesTheThemesDayOrNightPrimaryBeforeAndroid12() {
        assertEquals(0xFF123456.toInt(), primaryTextColor(theme, Scheme.DAY))
        assertEquals(0xFFABCDEF.toInt(), primaryTextColor(theme, Scheme.NIGHT))
    }

    @Test
    @Config(sdk = [26])
    fun withoutAThemeTheWidgetRendersAsItDoesToday() {
        for (scheme in Scheme.entries) {
            val today =
                NativeComposeSeam
                    .compose(NativeComposeSeam.node(primaryText), scheme)
                    .applyUnder(scheme)
                    .textColors()

            assertNotNull("$scheme", today["Hi"])
            assertEquals("$scheme", today["Hi"], primaryTextColor(themeJson = null, scheme))
        }
    }

    private fun primaryTextColor(
        themeJson: String?,
        scheme: Scheme,
    ): Int? {
        val node = NativeComposeSeam.node(primaryText)
        val factory = GlanceFactory("seam", widgetSize = NativeComposeSeam.DEFAULT_SIZE)
        val views = NativeComposeSeam.compose(scheme) { DynamicWidgetThemedContent(themeJson, node, factory) }
        return views.applyUnder(scheme).textColors()["Hi"]
    }
}
