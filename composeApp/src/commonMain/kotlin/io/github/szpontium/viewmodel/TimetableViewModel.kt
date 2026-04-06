package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Schedule
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class TimetableState(
    val isLoading: Boolean = false,
    val schedule: List<Schedule> = emptyList(),
    val weekStart: LocalDate = mondayOfCurrentWeek(),
    val error: String? = null
)

private fun mondayOfCurrentWeek(): LocalDate {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysFromMonday = (today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    return today.minus(daysFromMonday, DateTimeUnit.DAY)
}

class TimetableViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(TimetableState(isLoading = true))
    val state: StateFlow<TimetableState> = _state

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

    private fun loadWeek(weekStart: LocalDate) {
        val account = session.currentAccount ?: return
        val api = session.api ?: return
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, weekStart = weekStart)
            try {
                val schedule = api.getSchedule(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    dateFrom = weekStart,
                    dateTo = weekEnd
                )
                _state.value = TimetableState(
                    schedule = schedule.sortedWith(
                        compareBy({ it.date }, { it.timeSlot.position })
                    ),
                    weekStart = weekStart
                )
            } catch (e: Exception) {
                _state.value = TimetableState(
                    weekStart = weekStart,
                    error = e.message ?: "Błąd ładowania planu"
                )
            }
        }
    }
}
