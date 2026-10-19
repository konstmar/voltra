package voltra.dynamicwidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import voltra.glance.GlanceFactory
import voltra.glance.GlanceRenderHelper
import voltra.glance.GlanceRenderHelper.Scheme
import voltra.glance.applyUnder
import voltra.glance.backgroundColors
import voltra.glance.textColors

/** A Dynamic Widget's exported theme applied around its rendered node, as each scheme shows it. */
@RunWith(RobolectricTestRunner::class)
class DynamicWidgetThemedContentTest {
    private val primaryText = """{"t":18,"c":"Hi","p":{"s":{"c":"~p"}}}"""
    private val secondaryText = """{"t":18,"c":"Hi","p":{"s":{"c":"~s"}}}"""
    private val redText = """{"t":18,"c":"Hi","p":{"s":{"c":"#ff0000"}}}"""
    private val primaryBackgroundModifier =
        """{"t":18,"c":"Hi","p":{"mods":"[{\"${'$'}type\":\"background\",\"color\":\"~p\"}]"}}"""

    private val themeDay = 0xFF123456.toInt()
    private val themeNight = 0xFFABCDEF.toInt()
    private val theme = """{"day":{"primary":"#123456"},"night":{"primary":"#abcdef"}}"""
    private val themeOverDynamicColors =
        """{"day":{"primary":"#123456"},"night":{"primary":"#abcdef"},"dynamicColors":false}"""

    @Test
    @Config(sdk = [26])
    fun aPrimaryTokenTakesTheThemesDayOrNightPrimaryBeforeAndroid12() {
        assertEquals(themeDay, textColor(primaryText, theme, Scheme.DAY))
        assertEquals(themeNight, textColor(primaryText, theme, Scheme.NIGHT))
    }

    @Test
    @Config(sdk = [31])
    fun fromAndroid12TheSystemPaletteWinsUnlessTheThemeTurnsDynamicColorsOff() {
        for (scheme in Scheme.entries) {
            val outer = textColor(primaryText, themeJSON = null, scheme)

            assertNotEquals("$scheme", themeDay, outer)
            assertNotEquals("$scheme", themeNight, outer)
            assertEquals("$scheme", outer, textColor(primaryText, theme, scheme))
        }
    }

    @Test
    @Config(sdk = [26, 31])
    fun withDynamicColorsOffThePrimaryTokenTakesTheThemeOnEveryApiLevel() {
        assertEquals(themeDay, textColor(primaryText, themeOverDynamicColors, Scheme.DAY))
        assertEquals(themeNight, textColor(primaryText, themeOverDynamicColors, Scheme.NIGHT))
    }

    @Test
    @Config(sdk = [26, 31])
    fun rolesTheThemeLeavesOutKeepTheOuterPalette() {
        for (scheme in Scheme.entries) {
            assertEquals(
                "$scheme",
                textColor(secondaryText, themeJSON = null, scheme),
                textColor(secondaryText, themeOverDynamicColors, scheme),
            )
        }
    }

    @Test
    @Config(sdk = [26, 31])
    fun aLiteralColorStaysAsWrittenUnderAnyTheme() {
        for (themeJSON in listOf(theme, themeOverDynamicColors)) {
            for (scheme in Scheme.entries) {
                assertEquals("$scheme $themeJSON", 0xFFFF0000.toInt(), textColor(redText, themeJSON, scheme))
            }
        }
    }

    @Test
    @Config(sdk = [26, 31])
    fun aRoleTokenInANativeModifierPicksUpTheTheme() {
        for ((scheme, expected) in listOf(Scheme.DAY to themeDay, Scheme.NIGHT to themeNight)) {
            val background = render(primaryBackgroundModifier, themeOverDynamicColors, scheme).backgroundColors()

            assertEquals("$scheme", listOf(expected), background)
        }
    }

    @Test
    @Config(sdk = [26, 31])
    fun aMalformedThemeRendersWithTheDefaultPaletteLikeNoTheme() {
        val malformed = listOf("\"function\"", """{"day":"#123456","night":{}}""", "not json")

        for (themeJSON in malformed) {
            for (scheme in Scheme.entries) {
                assertEquals(
                    "$scheme $themeJSON",
                    textColor(primaryText, themeJSON = null, scheme),
                    textColor(primaryText, themeJSON, scheme),
                )
            }
        }
    }

    @Test
    @Config(sdk = [26])
    fun withoutAThemeTheWidgetRendersAsItDoesToday() {
        for (scheme in Scheme.entries) {
            val today =
                GlanceRenderHelper
                    .compose(GlanceRenderHelper.node(primaryText), scheme)
                    .applyUnder(scheme)
                    .textColors()

            assertNotNull("$scheme", today["Hi"])
            assertEquals("$scheme", today["Hi"], textColor(primaryText, themeJSON = null, scheme))
        }
    }

    private fun textColor(
        json: String,
        themeJSON: String?,
        scheme: Scheme,
    ): Int? = render(json, themeJSON, scheme).textColors()["Hi"]

    /** Composes and applies under the same [scheme], as a widget update in that scheme would. */
    private fun render(
        json: String,
        themeJSON: String?,
        scheme: Scheme,
    ) = run {
        val node = GlanceRenderHelper.node(json)
        val factory = GlanceFactory("test", widgetSize = GlanceRenderHelper.DEFAULT_SIZE)
        GlanceRenderHelper.compose(scheme) { DynamicWidgetThemedContent(themeJSON, node, factory) }.applyUnder(scheme)
    }
}
