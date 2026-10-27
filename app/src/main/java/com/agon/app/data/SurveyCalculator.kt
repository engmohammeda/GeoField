package com.agon.app.data

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Pure surveying math: distances, bearings, areas and traverse adjustment.
 * All internal units are metres and degrees.
 */
object SurveyCalculator {

    const val METERS_PER_FOOT = 3.280839895
    const val SQUARE_METERS_PER_SQUARE_FOOT = 10.7639104167

    fun distance(a: SurveyPoint, b: SurveyPoint): Double =
        hypot(b.easting - a.easting, b.northing - a.northing)

    /** Azimuth measured clockwise from north, normalised to [0, 360). */
    fun azimuth(a: SurveyPoint, b: SurveyPoint): Double {
        val dE = b.easting - a.easting
        val dN = b.northing - a.northing
        if (dE == 0.0 && dN == 0.0) return 0.0
        var deg = Math.toDegrees(atan2(dE, dN))
        if (deg < 0) deg += 360.0
        return deg
    }

    /** Shoelace area (m²) of a closed ring. Returns 0 for fewer than 3 points. */
    fun shoelaceArea(points: List<SurveyPoint>): Double {
        if (points.size < 3) return 0.0
        var sum = 0.0
        for (i in points.indices) {
            val p = points[i]
            val q = points[(i + 1) % points.size]
            sum += p.easting * q.northing - q.easting * p.northing
        }
        return abs(sum) / 2.0
    }

    fun perimeter(points: List<SurveyPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) total += distance(points[i], points[i + 1])
        return total
    }

    /**
     * Builds the legs (segments) of an ordered traverse.
     */
    fun legs(order: List<SurveyPoint>, closed: Boolean): List<TraverseLeg> {
        if (order.size < 2) return emptyList()
        val result = mutableListOf<TraverseLeg>()
        val count = if (closed) order.size else order.size - 1
        for (i in 0 until count) {
            val from = order[i]
            val to = order[(i + 1) % order.size]
            result.add(
                TraverseLeg(
                    index = i + 1,
                    from = from,
                    to = to,
                    length = distance(from, to),
                    bearing = azimuth(from, to),
                    deltaEast = to.easting - from.easting,
                    deltaNorth = to.northing - from.northing,
                )
            )
        }
        return result
    }

    /**
     * Computes a full traverse report. For a closed loop the closing error is
     * distributed proportionally to the cumulative leg length (Bowditch method).
     */
    fun traverse(order: List<SurveyPoint>, closed: Boolean): TraverseResult {
        if (order.size < 2) {
            return TraverseResult(order = order, adjusted = order, isClosed = false)
        }
        val legList = legs(order, closed)
        val perim = legList.sumOf { it.length }
        val area = if (closed && order.size >= 3) shoelaceArea(order) else 0.0

        if (!closed) {
            return TraverseResult(
                order = order,
                adjusted = order,
                legLengths = legList.map { it.length },
                legBearings = legList.map { it.bearing },
                perimeter = perim,
                area = area,
                isClosed = false,
            )
        }

        val first = order.first()
        val last = order.last()
        val closeE = first.easting - last.easting
        val closeN = first.northing - last.northing
        val err = hypot(closeE, closeN)

        // cumulative distance from the first point to each point
        val cumulative = DoubleArray(order.size)
        var running = 0.0
        for (i in 1 until order.size) {
            running += distance(order[i - 1], order[i])
            cumulative[i] = running
        }
        val total = if (running > 0) running else 1.0

        val adjusted = order.mapIndexed { i, p ->
            val f = cumulative[i] / total
            p.copy(
                easting = p.easting + closeE * f,
                northing = p.northing + closeN * f,
            )
        }

        // precision ratio expressed as 1 : N  (N = perimeter / closure error)
        val denom = if (err > 1e-9) (perim / err) else Double.POSITIVE_INFINITY

        var misclosure = 0.0
        if (err > 1e-9) {
            misclosure = Math.toDegrees(atan2(closeE, closeN))
            if (misclosure < 0) misclosure += 360.0
        }

        return TraverseResult(
            order = order,
            adjusted = adjusted,
            legLengths = legList.map { it.length },
            legBearings = legList.map { it.bearing },
            perimeter = perim,
            area = area,
            closingEast = closeE,
            closingNorth = closeN,
            closingError = err,
            misclosureBearing = misclosure,
            precisionDenominator = denom,
            isClosed = true,
        )
    }

    /** Bounding box of a point cloud. */
    data class Bounds(
        val minE: Double,
        val maxE: Double,
        val minN: Double,
        val maxN: Double,
    ) {
        val width: Double get() = maxE - minE
        val height: Double get() = maxN - minN
        val centerE: Double get() = (minE + maxE) / 2
        val centerN: Double get() = (minN + maxN) / 2
    }

    fun bounds(points: List<SurveyPoint>): Bounds? {
        if (points.isEmpty()) return null
        var minE = Double.MAX_VALUE
        var maxE = -Double.MAX_VALUE
        var minN = Double.MAX_VALUE
        var maxN = -Double.MAX_VALUE
        points.forEach {
            minE = min(minE, it.easting); maxE = max(maxE, it.easting)
            minN = min(minN, it.northing); maxN = max(maxN, it.northing)
        }
        return Bounds(minE, maxE, minN, maxN)
    }

    /** Elevation statistics used by the dashboard. */
    fun elevationStats(points: List<SurveyPoint>): Triple<Double, Double, Double> {
        if (points.isEmpty()) return Triple(0.0, 0.0, 0.0)
        var minZ = Double.MAX_VALUE
        var maxZ = -Double.MAX_VALUE
        var sum = 0.0
        points.forEach {
            minZ = min(minZ, it.elevation)
            maxZ = max(maxZ, it.elevation)
            sum += it.elevation
        }
        return Triple(minZ, maxZ, sum / points.size)
    }
}
