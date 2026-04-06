package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Exam
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class ExamsState(
    val isLoading: Boolean = false,
    val exams: List<Exam> = emptyList(),
    val error: String? = null
)

class ExamsViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(ExamsState(isLoading = true))
    val state: StateFlow<ExamsState> = _state

    init {
        load()
    }

    fun load() {
        val account = session.currentAccount ?: return
        val api = session.api ?: return
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val endDate = today.plus(60, DateTimeUnit.DAY)

        viewModelScope.launch {
            _state.value = ExamsState(isLoading = true)
            try {
                val exams = api.getExams(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    dateFrom = today,
                    dateTo = endDate
                )
                _state.value = ExamsState(
                    exams = exams.sortedBy { it.deadline }
                )
            } catch (e: Exception) {
                _state.value = ExamsState(error = e.message ?: "Błąd ładowania sprawdzianów")
            }
        }
    }
}
