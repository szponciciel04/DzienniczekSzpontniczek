package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Schedule
import io.github.szpontium.api.hebe.models.isCanceled
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private fun initialSelectedDate(): LocalDate {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (today.dayOfWeek) {
        DayOfWeek.SATURDAY -> today.plus(2, DateTimeUnit.DAY)
        DayOfWeek.SUNDAY -> today.plus(1, DateTimeUnit.DAY)
        else -> today
    }
}

fun mondayOfWeek(date: LocalDate): LocalDate {
    val daysFromMonday = (date.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    return date.minus(daysFromMonday, DateTimeUnit.DAY)
}

data class TimetableState(
    val isLoading: Boolean = false,
    val schedule: List<Schedule> = emptyList(),
    val selectedDate: LocalDate = initialSelectedDate(),
    val error: String? = null
) {
    val dayLessons: List<Schedule>
        get() {
            val rawLessonsForDay = schedule.filter { it.date == selectedDate }
            val byPosition = rawLessonsForDay.groupBy { it.timeSlot.position }
            val processed = mutableListOf<Schedule>()

            for ((_, lessons) in byPosition) {
                val activeSubstitute = lessons.firstOrNull { it.substitution != null && !it.isCanceled }
                if (activeSubstitute != null) {
                    processed.add(activeSubstitute)
                } else {
                    processed.addAll(lessons)
                }
            }

            return processed.sortedBy { it.timeSlot.position }
        }
}

class TimetableViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(TimetableState(isLoading = true))
    val state: StateFlow<TimetableState> = _state

    init {
        viewModelScope.launch {
            session.activeStudent.collectLatest {
                val weekStart = mondayOfWeek(_state.value.selectedDate)
                loadWeek(weekStart)
            }
        }
    }

    fun selectDate(date: LocalDate) {
        val currentWeekStart = mondayOfWeek(_state.value.selectedDate)
        val newWeekStart = mondayOfWeek(date)
        _state.value = _state.value.copy(selectedDate = date)
        if (currentWeekStart != newWeekStart || _state.value.schedule.isEmpty()) {
            loadWeek(newWeekStart)
        }
    }

    fun previousDay() {
        var newDate = _state.value.selectedDate.minus(1, DateTimeUnit.DAY)
        if (newDate.dayOfWeek == DayOfWeek.SUNDAY) {
            newDate = newDate.minus(2, DateTimeUnit.DAY)
        }
        selectDate(newDate)
    }

    fun nextDay() {
        var newDate = _state.value.selectedDate.plus(1, DateTimeUnit.DAY)
        if (newDate.dayOfWeek == DayOfWeek.SATURDAY) {
            newDate = newDate.plus(2, DateTimeUnit.DAY)
        }
        selectDate(newDate)
    }

    private fun loadWeek(weekStart: LocalDate) {
        val account = session.currentAccount
        val api = session.api
        if (account == null || api == null) {
            _state.value = _state.value.copy(isLoading = false)
            return
        }

        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val schedule = api.getSchedule(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    dateFrom = weekStart,
                    dateTo = weekEnd
                )
                _state.value = _state.value.copy(
                    isLoading = false,
                    schedule = schedule.sortedWith(
                        compareBy({ it.date }, { it.timeSlot.position })
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Błąd ładowania planu"
                )
            }
        }
    }
}
