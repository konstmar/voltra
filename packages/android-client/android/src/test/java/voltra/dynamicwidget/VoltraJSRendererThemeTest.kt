package voltra.dynamicwidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VoltraJSRendererThemeTest {
    private val bundle = "__d(function () {}, 0, []);\n__r(0);"

    @Test
    fun readsTheWidgetThemeAfterASuccessfulEvaluation() {
        val engine = FakeEngine(theme = """{"day":{"primary":"#112233"},"night":{"primary":"#445566"}}""")
        val renderer = VoltraJSRendererCore(engine)

        assertTrue(renderer.evaluateBundle(bundle, "weather"))

        assertEquals(
            """{"day":{"primary":"#112233"},"night":{"primary":"#445566"}}""",
            renderer.themeJSON("weather"),
        )
    }

    @Test
    fun rereadsTheThemeOnEveryEvaluationAndForgetsARemovedOne() {
        val engine = FakeEngine(theme = """{"day":{},"night":{}}""")
        val renderer = VoltraJSRendererCore(engine)
        renderer.evaluateBundle(bundle, "weather")

        engine.theme = """{"day":{"primary":"red"},"night":{"primary":"blue"}}"""
        renderer.evaluateBundle(bundle, "weather")
        assertEquals("""{"day":{"primary":"red"},"night":{"primary":"blue"}}""", renderer.themeJSON("weather"))

        engine.theme = null
        renderer.evaluateBundle(bundle, "weather")
        assertNull(renderer.themeJSON("weather"))
    }

    @Test
    fun aFailedThemeReadLeavesTheEvaluationResultAndClearsTheTheme() {
        val engine = FakeEngine(theme = """{"day":{},"night":{}}""")
        val renderer = VoltraJSRendererCore(engine)
        renderer.evaluateBundle(bundle, "weather")

        engine.themeFailure = UnsatisfiedLinkError("nativeThemeJSON")

        assertTrue(renderer.evaluateBundle(bundle, "weather"))
        assertNull(renderer.themeJSON("weather"))
    }

    @Test
    fun keepsEachWidgetsThemeSeparate() {
        val engine = FakeEngine(theme = """{"day":{"primary":"red"},"night":{"primary":"red"}}""")
        val renderer = VoltraJSRendererCore(engine)
        renderer.evaluateBundle(bundle, "weather")

        engine.theme = null
        renderer.evaluateBundle(bundle, "inbox")

        assertEquals("""{"day":{"primary":"red"},"night":{"primary":"red"}}""", renderer.themeJSON("weather"))
        assertNull(renderer.themeJSON("inbox"))
    }

    @Test
    fun doesNotReadTheThemeWhenEvaluationFails() {
        val engine = FakeEngine(theme = """{"day":{},"night":{}}""", evaluates = false)
        val renderer = VoltraJSRendererCore(engine)

        assertFalse(renderer.evaluateBundle(bundle, "weather"))

        assertEquals(0, engine.themeReads)
        assertNull(renderer.themeJSON("weather"))
    }

    private class FakeEngine(
        var theme: String?,
        private val evaluates: Boolean = true,
    ) : VoltraJSEngine {
        var themeFailure: Throwable? = null
        var themeReads = 0

        override fun evaluateBundle(
            wrappedSource: String,
            sourceURL: String,
            widgetId: String,
        ): Boolean = evaluates

        override fun render(
            widgetId: String,
            propsJSON: String,
            envJSON: String,
        ): String? = null

        override fun themeJSON(widgetId: String): String? {
            themeReads++
            themeFailure?.let { throw it }
            return theme
        }
    }
}
