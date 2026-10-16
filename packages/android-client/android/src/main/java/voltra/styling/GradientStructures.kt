package voltra.styling

import android.content.Context

data class UnitPoint(
    val x: Float,
    val y: Float,
) {
    companion object {
        val Center = UnitPoint(0.5f, 0.5f)
    }
}

enum class GradientKind {
    LINEAR,
    RADIAL,
    CONIC,
}

enum class RadialGradientShape {
    CIRCLE,
    ELLIPSE,
}

enum class RadialGradientExtent {
    CLOSEST_SIDE,
    FARTHEST_SIDE,
    CLOSEST_CORNER,
    FARTHEST_CORNER,
}

data class GradientStop(
    val color: VoltraColorValue,
    val location: Float,
)

sealed class BackgroundImageValue {
    abstract val stops: List<GradientStop>

    data class LinearGradient(
        val startPoint: UnitPoint,
        val endPoint: UnitPoint,
        override val stops: List<GradientStop>,
    ) : BackgroundImageValue()

    data class RadialGradient(
        val center: UnitPoint,
        val shape: RadialGradientShape,
        val extent: RadialGradientExtent,
        override val stops: List<GradientStop>,
    ) : BackgroundImageValue()

    data class ConicGradient(
        val center: UnitPoint,
        val angleDegrees: Float,
        override val stops: List<GradientStop>,
    ) : BackgroundImageValue()
}

/** A style `backgroundImage`: one gradient, or a `{ day, night }` pair of gradients. */
sealed interface VoltraBackgroundImage {
    data class Single(
        val gradient: BackgroundImageValue,
    ) : VoltraBackgroundImage

    /** Both sides are parsed up front; rendering picks one by the night bit. */
    data class DayNight(
        val day: BackgroundImageValue,
        val night: BackgroundImageValue,
    ) : VoltraBackgroundImage
}

/** The gradient to draw under [context]'s color scheme. */
fun VoltraBackgroundImage.resolveGradient(context: Context): BackgroundImageValue =
    when (this) {
        is VoltraBackgroundImage.Single -> gradient
        is VoltraBackgroundImage.DayNight -> if (context.isNightMode()) night else day
    }
