package com.agon.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.AppLanguage
import com.agon.app.data.AppSettings
import com.agon.app.data.PointCategory
import com.agon.app.data.SampleData
import com.agon.app.data.SettingsRepository
import com.agon.app.data.SurveyPoint
import com.agon.app.data.SurveyRepository
import com.agon.app.data.ThemeMode
import com.agon.app.data.UnitSystem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PointSort { TIME, CODE, ELEVATION }

data class SurveyUiState(
    val loading: Boolean = true,
    val points: List<SurveyPoint> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val query: String = "",
    val categoryFilter: PointCategory? = null,
    val sort: PointSort = PointSort.TIME,
    val traverseClosed: Boolean = true,
    val traverseReversed: Boolean = false,
) {
    val filteredPoints: List<SurveyPoint>
        get() {
            val q = query.trim().lowercase()
            var result = points.filter { point ->
                val matchesQuery = q.isEmpty() ||
                    point.code.lowercase().contains(q) ||
                    point.description.lowercase().contains(q)
                val matchesCategory = categoryFilter == null || point.category == categoryFilter
                matchesQuery && matchesCategory
            }
            result = when (sort) {
                PointSort.TIME -> result.sortedByDescending { it.recordedAt }
                PointSort.CODE -> result.sortedBy { it.code.lowercase() }
                PointSort.ELEVATION -> result.sortedByDescending { it.elevation }
            }
            return result
        }

    /** Points ordered for the traverse computation. */
    val traverseOrder: List<SurveyPoint>
        get() {
            val ordered = points.sortedBy { it.recordedAt }
            return if (traverseReversed) ordered.reversed() else ordered
        }
}

sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data class Share(val subject: String, val body: String) : UiEvent
}

class SurveyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SurveyRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _state = MutableStateFlow(SurveyUiState())
    val state: StateFlow<SurveyUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var lastDeleted: SurveyPoint? = null

    init {
        viewModelScope.launch {
            val seeded = repository.seedIfEmpty()
            _state.update { it.copy(points = seeded, loading = false) }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { prefs ->
                _state.update { it.copy(settings = prefs) }
            }
        }
    }

    // ----- points -----------------------------------------------------------

    fun addPoint(
        code: String,
        easting: Double,
        northing: Double,
        elevation: Double,
        category: PointCategory,
        description: String,
        accuracy: Double,
    ) {
        viewModelScope.launch {
            val point = SurveyPoint(
                id = "pt-" + System.currentTimeMillis().toString(36) + "-" + (0..999).random(),
                code = code.trim(),
                easting = easting,
                northing = northing,
                elevation = elevation,
                category = category,
                description = description.trim(),
                accuracy = accuracy,
                recordedAt = System.currentTimeMillis(),
            )
            val updated = _state.value.points + point
            persist(updated)
            _events.tryEmit(UiEvent.Message(MESSAGE_SAVED))
        }
    }

    fun updatePoint(point: SurveyPoint) {
        viewModelScope.launch {
            val updated = _state.value.points.map { if (it.id == point.id) point else it }
            persist(updated)
            _events.tryEmit(UiEvent.Message(MESSAGE_UPDATED))
        }
    }

    fun deletePoint(id: String) {
        viewModelScope.launch {
            val current = _state.value.points
            val target = current.firstOrNull { it.id == id } ?: return@launch
            lastDeleted = target
            persist(current.filterNot { it.id == id })
            _events.tryEmit(UiEvent.Message(MESSAGE_DELETED))
        }
    }

    fun restoreLastDeleted() {
        val target = lastDeleted ?: return
        viewModelScope.launch {
            persist(_state.value.points + target)
            lastDeleted = null
            _events.tryEmit(UiEvent.Message(MESSAGE_RESTORED))
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clear()
            lastDeleted = null
            _state.update { it.copy(points = emptyList()) }
            _events.tryEmit(UiEvent.Message(MESSAGE_CLEARED))
        }
    }

    fun loadSample() {
        viewModelScope.launch {
            val sample = SampleData.points()
            persist(sample)
            _events.tryEmit(UiEvent.Message(MESSAGE_SAMPLE))
        }
    }

    private suspend fun persist(points: List<SurveyPoint>) {
        repository.save(points)
        _state.update { it.copy(points = points) }
    }

    // ----- filters ----------------------------------------------------------

    fun setQuery(query: String) = _state.update { it.copy(query = query) }

    fun setCategoryFilter(category: PointCategory?) =
        _state.update { it.copy(categoryFilter = category) }

    fun setSort(sort: PointSort) = _state.update { it.copy(sort = sort) }

    fun setTraverseClosed(closed: Boolean) = _state.update { it.copy(traverseClosed = closed) }

    fun reverseTraverse() = _state.update { it.copy(traverseReversed = !it.traverseReversed) }

    // ----- settings ---------------------------------------------------------

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }

    fun setUnits(units: UnitSystem) = viewModelScope.launch { settingsRepository.setUnits(units) }

    fun setLanguage(language: AppLanguage) = viewModelScope.launch { settingsRepository.setLanguage(language) }

    fun setDecimals(decimals: Int) = viewModelScope.launch { settingsRepository.setDecimals(decimals) }

    fun setProjectName(name: String) = viewModelScope.launch { settingsRepository.setProjectName(name) }

    // ----- exports ----------------------------------------------------------

    fun shareCsv(points: List<SurveyPoint>, header: String) {
        val body = buildString {
            append(header).append('\n')
            append("Code,Easting,Northing,Elevation,Accuracy,Category,Description,Timestamp\n")
            points.forEach { p ->
                append(
                    listOf(
                        p.code,
                        p.easting,
                        p.northing,
                        p.elevation,
                        p.accuracy,
                        p.category.key,
                        p.description.replace(',', ' '),
                        p.recordedAt,
                    ).joinToString(",")
                )
                append('\n')
            }
        }
        _events.tryEmit(UiEvent.Share(EXPORT_SUBJECT, body))
    }

    fun shareGeoJson(points: List<SurveyPoint>, header: String) {
        val features = points.joinToString(",\n") { p ->
            """    {
      "type": "Feature",
      "geometry": { "type": "Point", "coordinates": [${p.easting}, ${p.northing}, ${p.elevation}] },
      "properties": {
        "code": "${p.code}",
        "category": "${p.category.key}",
        "description": "${p.description.replace('"', '\'')}",
        "accuracy": ${p.accuracy}
      }
    }"""
        }
        val body = """
{
  "type": "FeatureCollection",
  "name": "$header",
  "features": [
$features
  ]
}
        """.trimIndent()
        _events.tryEmit(UiEvent.Share(EXPORT_SUBJECT, body))
    }

    companion object {
        const val MESSAGE_SAVED = "point_saved"
        const val MESSAGE_UPDATED = "point_updated"
        const val MESSAGE_DELETED = "point_deleted"
        const val MESSAGE_RESTORED = "point_restored"
        const val MESSAGE_CLEARED = "data_cleared"
        const val MESSAGE_SAMPLE = "sample_loaded"
        const val EXPORT_SUBJECT = "GeoField — Survey export"
    }
}
