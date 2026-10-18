package voltra.dynamicwidget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import org.json.JSONObject
import voltra.styling.JSColorParser
import voltra.styling.VoltraColorValue
import voltra.styling.VoltraThemeColorRole
import voltra.styling.toColorProvider

/**
 * A Dynamic Widget's `theme` export: a day and a night colour for any of the
 * [VoltraThemeColorRole]s, named as in `AndroidDynamicColors` (`primary`, `onPrimary`, …).
 *
 * Only a role with a colour on both sides is themed; every other role keeps the palette the
 * widget would have used without a theme.
 */
internal class DynamicWidgetTheme(
    private val pairs: Map<VoltraThemeColorRole, Pair<Color, Color>>,
    /** Whether the system's dynamic palette (API 31+) wins over this theme. Defaults to true. */
    val dynamicColors: Boolean,
) {
    /** All 27 roles: this theme's day/night pair where it has one, [fallback]'s provider otherwise. */
    fun toColorProviders(fallback: ColorProviders): ColorProviders {
        fun role(role: VoltraThemeColorRole) =
            pairs[role]?.let { (day, night) -> ColorProvider(day = day, night = night) }
                ?: role.toColorProvider(fallback)

        return colorProviders(
            primary = role(VoltraThemeColorRole.PRIMARY),
            onPrimary = role(VoltraThemeColorRole.ON_PRIMARY),
            primaryContainer = role(VoltraThemeColorRole.PRIMARY_CONTAINER),
            onPrimaryContainer = role(VoltraThemeColorRole.ON_PRIMARY_CONTAINER),
            secondary = role(VoltraThemeColorRole.SECONDARY),
            onSecondary = role(VoltraThemeColorRole.ON_SECONDARY),
            secondaryContainer = role(VoltraThemeColorRole.SECONDARY_CONTAINER),
            onSecondaryContainer = role(VoltraThemeColorRole.ON_SECONDARY_CONTAINER),
            tertiary = role(VoltraThemeColorRole.TERTIARY),
            onTertiary = role(VoltraThemeColorRole.ON_TERTIARY),
            tertiaryContainer = role(VoltraThemeColorRole.TERTIARY_CONTAINER),
            onTertiaryContainer = role(VoltraThemeColorRole.ON_TERTIARY_CONTAINER),
            error = role(VoltraThemeColorRole.ERROR),
            errorContainer = role(VoltraThemeColorRole.ERROR_CONTAINER),
            onError = role(VoltraThemeColorRole.ON_ERROR),
            onErrorContainer = role(VoltraThemeColorRole.ON_ERROR_CONTAINER),
            background = role(VoltraThemeColorRole.BACKGROUND),
            onBackground = role(VoltraThemeColorRole.ON_BACKGROUND),
            surface = role(VoltraThemeColorRole.SURFACE),
            onSurface = role(VoltraThemeColorRole.ON_SURFACE),
            surfaceVariant = role(VoltraThemeColorRole.SURFACE_VARIANT),
            onSurfaceVariant = role(VoltraThemeColorRole.ON_SURFACE_VARIANT),
            outline = role(VoltraThemeColorRole.OUTLINE),
            inverseOnSurface = role(VoltraThemeColorRole.INVERSE_ON_SURFACE),
            inverseSurface = role(VoltraThemeColorRole.INVERSE_SURFACE),
            inversePrimary = role(VoltraThemeColorRole.INVERSE_PRIMARY),
            widgetBackground = role(VoltraThemeColorRole.WIDGET_BACKGROUND),
        )
    }

    companion object {
        /** Parses the JSON [VoltraJSRenderer.themeJSON] returns; null JSON means no theme. */
        fun parse(json: String?): DynamicWidgetTheme? {
            if (json == null) return null
            val root = JSONObject(json)
            val day = root.getJSONObject("day")
            val night = root.getJSONObject("night")
            val pairs =
                VoltraThemeColorRole.entries
                    .mapNotNull { role ->
                        val key = role.themeKey
                        val dayColor = staticColor(day.opt(key)) ?: return@mapNotNull null
                        val nightColor = staticColor(night.opt(key)) ?: return@mapNotNull null
                        role to (dayColor to nightColor)
                    }.toMap()
            return DynamicWidgetTheme(pairs, dynamicColors = root.optBoolean("dynamicColors", true))
        }

        private fun staticColor(value: Any?): Color? =
            (value as? String)?.let { (JSColorParser.parse(it) as? VoltraColorValue.Static)?.color }

        /** The role's key in `AndroidDynamicColors`: `ON_PRIMARY_CONTAINER` → `onPrimaryContainer`. */
        private val VoltraThemeColorRole.themeKey: String
            get() =
                name
                    .lowercase()
                    .split('_')
                    .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                    .joinToString("")
    }
}
