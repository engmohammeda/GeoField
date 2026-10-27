package com.agon.app.data

import kotlinx.serialization.Serializable

/**
 * Category of a surveyed point. [key] is the stable value persisted in JSON.
 */
enum class PointCategory(val key: String) {
    CORNER("corner"),
    TRAVERSE("traverse"),
    BENCHMARK("benchmark"),
    FEATURE("feature"),
    OBSERVATION("observation");

    companion object {
        fun fromKey(value: String): PointCategory =
            entries.firstOrNull { it.key == value } ?: CORNER
    }
}

/**
 * A single surveyed point in a local engineering grid (Easting / Northing / Elevation).
 */
@Serializable
data class SurveyPoint(
    val id: String,
    val code: String,
    val easting: Double,
    val northing: Double,
    val elevation: Double,
    val category: PointCategory = PointCategory.CORNER,
    val description: String = "",
    val accuracy: Double = 0.0,
    val recordedAt: Long = 0L,
)

/**
 * Computed result of a (possibly closed) traverse.
 */
data class TraverseResult(
    val order: List<SurveyPoint> = emptyList(),
    val adjusted: List<SurveyPoint> = emptyList(),
    val legLengths: List<Double> = emptyList(),
    val legBearings: List<Double> = emptyList(),
    val perimeter: Double = 0.0,
    val area: Double = 0.0,
    val closingEast: Double = 0.0,
    val closingNorth: Double = 0.0,
    val closingError: Double = 0.0,
    val misclosureBearing: Double = 0.0,
    val precisionDenominator: Double = 0.0,
    val isClosed: Boolean = false,
)

/**
 * One row of the traverse legs table.
 */
data class TraverseLeg(
    val index: Int,
    val from: SurveyPoint,
    val to: SurveyPoint,
    val length: Double,
    val bearing: Double,
    val deltaEast: Double,
    val deltaNorth: Double,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class UnitSystem { METRIC, IMPERIAL }

enum class AppLanguage { ARABIC, ENGLISH }

/**
 * User preferences persisted with DataStore.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val units: UnitSystem = UnitSystem.METRIC,
    val language: AppLanguage = AppLanguage.ARABIC,
    val decimals: Int = 2,
    val projectName: String = "مشروع المسح الميداني",
)
