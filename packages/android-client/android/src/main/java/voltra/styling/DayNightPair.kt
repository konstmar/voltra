package voltra.styling

import android.util.Log

/**
 * Parses both sides of a `{ day, night }` pair with [parseSide] and joins them with [combine]. If
 * either side fails, the whole pair is dropped with one warning under [tag] naming [key] and the
 * first failing side.
 *
 * @param kind what the pair holds, e.g. "color", for "Ignoring invalid color pair".
 * @param expected what each side must be, e.g. "a literal color string".
 */
internal fun <Side : Any, Result> parseDayNightPair(
    pair: Map<*, *>,
    key: String?,
    tag: String,
    kind: String,
    expected: String,
    parseSide: (Any?) -> Side?,
    combine: (day: Side, night: Side) -> Result,
): Result? {
    val day = parseSide(pair["day"])
    val night = parseSide(pair["night"])
    if (day != null && night != null) return combine(day, night)

    val side = if (day == null) "day" else "night"
    val value = pair[side]
    val got =
        when {
            side !in pair -> "nothing"
            value is String -> "\"$value\""
            else -> value.toString()
        }
    Log.w(tag, "Ignoring invalid $kind pair${key?.let { " for $it" } ?: ""}: $side must be $expected, got $got")
    return null
}
