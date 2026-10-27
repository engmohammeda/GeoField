package com.agon.app.ui

import com.agon.app.data.AppLanguage
import com.agon.app.data.PointCategory
import com.agon.app.data.SurveyCalculator
import com.agon.app.data.UnitSystem
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale

/**
 * Number / unit helpers. Digits are always Latin so coordinate values stay
 * readable in the field regardless of the UI language.
 */
object Format {

    private val symbols = DecimalFormatSymbols(Locale.US)

    private fun formatter(decimals: Int): DecimalFormat =
        DecimalFormat(if (decimals <= 0) "#" else "0." + "#".repeat(decimals), symbols)

    fun number(value: Double, decimals: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        return formatter(decimals).format(value)
    }

    fun distance(meters: Double, units: UnitSystem, decimals: Int): String {
        val value = if (units == UnitSystem.METRIC) meters else meters * SurveyCalculator.METERS_PER_FOOT
        val unit = if (units == UnitSystem.METRIC) "m" else "ft"
        return "${number(value, decimals)} $unit"
    }

    fun distanceValue(meters: Double, units: UnitSystem, decimals: Int): Double =
        if (units == UnitSystem.METRIC) meters else meters * SurveyCalculator.METERS_PER_FOOT

    fun area(squareMeters: Double, units: UnitSystem): String {
        val value = if (units == UnitSystem.METRIC) {
            squareMeters
        } else {
            squareMeters * SurveyCalculator.SQUARE_METERS_PER_SQUARE_FOOT
        }
        val unit = if (units == UnitSystem.METRIC) "m²" else "ft²"
        return "${number(value, 1)} $unit"
    }

    /** Bearing as degrees + minutes + seconds, e.g. 45° 30' 12" */
    fun bearing(degrees: Double): String {
        val d = ((degrees % 360.0) + 360.0) % 360.0
        val whole = d.toInt()
        val minutesFull = (d - whole) * 60.0
        val minutes = minutesFull.toInt()
        val seconds = ((minutesFull - minutes) * 60.0).toInt()
        return "$whole° $minutes' $seconds\""
    }

    fun timestamp(millis: Long, language: AppLanguage): String {
        val locale = if (language == AppLanguage.ARABIC) Locale("ar") else Locale.US
        val fmt = java.text.SimpleDateFormat("dd MMM yyyy · HH:mm", locale)
        return fmt.format(Date(millis))
    }

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - millis
        val minutes = diff / 60_000L
        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m"
            minutes < 60 * 24 -> "${minutes / 60}h"
            else -> "${minutes / (60 * 24)}d"
        }
    }
}

fun Strings.forCategory(category: PointCategory): String = when (category) {
    PointCategory.CORNER -> catCorner
    PointCategory.TRAVERSE -> catTraverse
    PointCategory.BENCHMARK -> catBenchmark
    PointCategory.FEATURE -> catFeature
    PointCategory.OBSERVATION -> catObservation
}
