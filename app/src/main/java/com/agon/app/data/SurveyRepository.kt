package com.agon.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class PointFile(
    val points: List<SurveyPoint> = emptyList(),
)

/**
 * Local persistence for surveyed points, stored as JSON inside the app's
 * private files directory.
 */
class SurveyRepository(private val context: Context) {

    private val file: File get() = File(context.filesDir, FILE_NAME)
    private val mutex = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    suspend fun load(): List<SurveyPoint> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!file.exists()) return@withLock emptyList()
            runCatching {
                val content = file.readText()
                if (content.isBlank()) emptyList()
                else json.decodeFromString<PointFile>(content).points
            }.getOrDefault(emptyList())
        }
    }

    suspend fun save(points: List<SurveyPoint>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                file.writeText(json.encodeToString(PointFile(points)))
            }
        }
        Unit
    }

    suspend fun clear() {
        save(emptyList())
    }

    /**
     * Seeds a realistic demo traverse the first time the app is opened so the
     * dashboard and charts are meaningful straight away.
     */
    suspend fun seedIfEmpty(): List<SurveyPoint> {
        val existing = load()
        if (existing.isNotEmpty()) return existing
        val sample = SampleData.points()
        save(sample)
        return sample
    }

    companion object {
        private const val FILE_NAME = "geofield_points.json"
    }
}

/**
 * Demo dataset: a closed traverse around a block plus a few extra points.
 */
object SampleData {
    fun points(now: Long = System.currentTimeMillis()): List<SurveyPoint> {
        val minute = 60_000L
        return listOf(
            SurveyPoint(
                id = "sample-1",
                code = "A1",
                easting = 1000.000,
                northing = 2000.000,
                elevation = 15.20,
                category = PointCategory.CORNER,
                description = "ركن البداية — قبالة البوابة",
                accuracy = 0.012,
                recordedAt = now - 300 * minute,
            ),
            SurveyPoint(
                id = "sample-2",
                code = "A2",
                easting = 1120.450,
                northing = 2038.120,
                elevation = 16.85,
                category = PointCategory.CORNER,
                description = "ركن شرقي على الحائط",
                accuracy = 0.018,
                recordedAt = now - 280 * minute,
            ),
            SurveyPoint(
                id = "sample-3",
                code = "A3",
                easting = 1205.880,
                northing = 2105.330,
                elevation = 18.10,
                category = PointCategory.TRAVERSE,
                description = "نقطة مسار وسط المقطع",
                accuracy = 0.009,
                recordedAt = now - 250 * minute,
            ),
            SurveyPoint(
                id = "sample-4",
                code = "A4",
                easting = 1188.210,
                northing = 2212.740,
                elevation = 17.40,
                category = PointCategory.CORNER,
                description = "ركن شمالي شرقي",
                accuracy = 0.021,
                recordedAt = now - 220 * minute,
            ),
            SurveyPoint(
                id = "sample-5",
                code = "A5",
                easting = 1104.560,
                northing = 2286.190,
                elevation = 16.05,
                category = PointCategory.CORNER,
                description = "ركن شمالي غربي",
                accuracy = 0.015,
                recordedAt = now - 190 * minute,
            ),
            SurveyPoint(
                id = "sample-6",
                code = "F-2",
                easting = 1002.330,
                northing = 2251.870,
                elevation = 15.85,
                category = PointCategory.FEATURE,
                description = "عمود كهرباء على الزاوية",
                accuracy = 0.025,
                recordedAt = now - 140 * minute,
            ),
            SurveyPoint(
                id = "sample-7",
                code = "OBS-7",
                easting = 942.740,
                northing = 2158.020,
                elevation = 16.60,
                category = PointCategory.OBSERVATION,
                description = "ملاحظة ميدانية على استواء الأرض",
                accuracy = 0.030,
                recordedAt = now - 90 * minute,
            ),
            SurveyPoint(
                id = "sample-8",
                code = "A1-B",
                easting = 1000.062,
                northing = 2000.031,
                elevation = 15.19,
                category = PointCategory.CORNER,
                description = "ملاحظة الإغلاق على A1",
                accuracy = 0.010,
                recordedAt = now - 30 * minute,
            ),
        )
    }
}
