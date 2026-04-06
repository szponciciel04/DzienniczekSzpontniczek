package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Exam
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class ExamsState(
    val isLoading: Boolean = false,
    val exams: List<Exam> = emptyList(),
    val weekStart: LocalDate = mondayOfCurrentWeekExams(),
    val error: String? = null
)

private fun mondayOfCurrentWeekExams(): LocalDate {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysFromMonday = (today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    return today.minus(daysFromMonday, DateTimeUnit.DAY)
}

class ExamsViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(ExamsState(isLoading = true))
    val state: StateFlow<ExamsState> = _state

    init {
        loadWeek(_state.value.weekStart)
    }

    fun previousWeek() {
        val newStart = _state.value.weekStart.minus(7, DateTimeUnit.DAY)
        loadWeek(newStart)
    }

    fun nextWeek() {
        val newStart = _state.value.weekStart.plus(7, DateTimeUnit.DAY)
        loadWeek(newStart)
    }

    fun load() {
        loadWeek(_state.value.weekStart)
    }

    private fun loadWeek(weekStart: LocalDate) {
        val account = session.currentAccount ?: return
        val api = session.api ?: return
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, weekStart = weekStart)
            try {
                val exams = api.getExams(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    dateFrom = weekStart,
                    dateTo = weekEnd
                )
                _state.value = ExamsState(
                    exams = exams.sortedBy { it.deadline },
                    weekStart = weekStart
                )
            } catch (e: Exception) {
                _state.value = ExamsState(
                    weekStart = weekStart,
                    error = e.message ?: "Błąd ładowania sprawdzianów"
                )
            }
        }
    }
}
