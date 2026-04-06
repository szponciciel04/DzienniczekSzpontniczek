package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Exam
import io.github.szpontium.api.hebe.models.Grade
import io.github.szpontium.api.hebe.models.Homework
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class StartState(
    val isLoading: Boolean = false,
    val firstName: String = "",
    val recentGrades: List<Grade> = emptyList(),
    val upcomingExams: List<Exam> = emptyList(),
    val upcomingHomework: List<Homework> = emptyList(),
    val error: String? = null
)

class StartViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(StartState(isLoading = true))
    val state: StateFlow<StartState> = _state

    init {
        loadSummary()
    }

    private fun loadSummary() {
        val account = session.currentAccount ?: return
        val api = session.api ?: return
        val period = account.periods.firstOrNull { it.current } ?: account.periods.lastOrNull() ?: return
        
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateToday = today.date
        val oneWeekAgo = dateToday.minus(7, DateTimeUnit.DAY)
        val oneWeekAhead = dateToday.plus(7, DateTimeUnit.DAY)

        viewModelScope.launch {
            _state.value = StartState(isLoading = true)
            try {
                // Fetch grades
                val grades = api.getGrades(
                    restUrl = account.unit.restUrl,
                    unitId = account.unit.id,
                    pupilId = account.pupil.id,
                    periodId = period.id
                )
                val recentGrades = grades.filter { 
                    it.createdAt.date >= oneWeekAgo
                }.sortedByDescending { it.createdAt }

                // Fetch exams
                val exams = api.getExams(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    dateFrom = dateToday,
                    dateTo = oneWeekAhead
                ).sortedBy { it.deadline }
                
                // Fetch homework
                val homework = api.getHomework(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    dateFrom = dateToday,
                    dateTo = oneWeekAhead
                ).sortedBy { it.deadline }

                _state.value = StartState(
                    isLoading = false,
                    firstName = account.pupil.firstName,
                    recentGrades = recentGrades,
                    upcomingExams = exams,
                    upcomingHomework = homework
                )
            } catch (e: Exception) {
                _state.value = StartState(
                    isLoading = false,
                    error = e.message ?: "Błąd ładowania podsumowania"
                )
            }
        }
    }
}
