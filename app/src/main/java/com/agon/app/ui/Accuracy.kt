package com.agon.app.ui

/**
 * Quality classification of a closed traverse, derived from its precision
 * ratio (1 : N).
 */
enum class AccuracyLevel { NONE, EXCELLENT, ACCEPTABLE, POOR }

fun accuracyLevel(denominator: Double, closed: Boolean): AccuracyLevel = when {
    !closed || denominator <= 0.0 || denominator.isInfinite() -> AccuracyLevel.NONE
    denominator >= 10000 -> AccuracyLevel.EXCELLENT
    denominator >= 3000 -> AccuracyLevel.ACCEPTABLE
    else -> AccuracyLevel.POOR
}

fun Strings.accuracyText(level: AccuracyLevel): String = when (level) {
    AccuracyLevel.EXCELLENT -> accuracyGood
    AccuracyLevel.ACCEPTABLE -> accuracyFair
    AccuracyLevel.POOR -> accuracyPoor
    AccuracyLevel.NONE -> openPath
}
