package voltra.styling

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** A `backgroundImage` `{ day, night }` pair is narrowed to one gradient by the night bit during conversion. */
class StyleConverterBackgroundImagePairTest {
    private val pair =
        mapOf(
            "backgroundImage" to
                mapOf(
                    "day" to "linear-gradient(to right, #ff0000, #ff0000)",
                    "night" to "radial-gradient(circle at center, #0000ff, #0000ff)",
                ),
        )

    private fun BackgroundImageValue?.firstStopColor() = this?.stops?.firstOrNull()?.color

    @Test
    fun theDaySideIsConvertedWhenNotInNightMode() {
        val image = StyleConverter.convert(pair, isNightMode = false).decoration.backgroundImage

        assertTrue(image.toString(), image is BackgroundImageValue.LinearGradient)
        assertEquals(VoltraColorValue.Static(Color.Red), image.firstStopColor())
    }

    @Test
    fun theNightSideIsConvertedInNightMode() {
        val image = StyleConverter.convert(pair, isNightMode = true).decoration.backgroundImage

        assertTrue(image.toString(), image is BackgroundImageValue.RadialGradient)
        assertEquals(VoltraColorValue.Static(Color.Blue), image.firstStopColor())
    }

    @Test
    fun conversionDefaultsToTheDaySide() {
        val image = StyleConverter.convert(pair).decoration.backgroundImage

        assertTrue(image.toString(), image is BackgroundImageValue.LinearGradient)
    }

    @Test
    fun extraKeysInThePairAreIgnored() {
        val withExtra =
            mapOf(
                "backgroundImage" to
                    mapOf(
                        "day" to "linear-gradient(to right, #ff0000, #ff0000)",
                        "night" to "linear-gradient(to right, #0000ff, #0000ff)",
                        "dim" to 42,
                    ),
            )

        val image = StyleConverter.convert(withExtra, isNightMode = true).decoration.backgroundImage

        assertEquals(VoltraColorValue.Static(Color.Blue), image.firstStopColor())
    }
}
