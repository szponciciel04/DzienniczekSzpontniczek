package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Homework
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class HomeworkState(
    val isLoading: Boolean = false,
    val homework: List<Homework> = emptyList(),
    val error: String? = null
)

class HomeworkViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(HomeworkState(isLoading = true))
    val state: StateFlow<HomeworkState> = _state

    init {
        load()
    }

    fun load() {
        val account = session.currentAccount ?: return
        val api = session.api ?: return
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val endDate = today.plus(30, DateTimeUnit.DAY)

        viewModelScope.launch {
            _state.value = HomeworkState(isLoading = true)
            try {
                val homework = api.getHomework(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    dateFrom = today,
                    dateTo = endDate
                )
                _state.value = HomeworkState(
                    homework = homework.sortedBy { it.deadline }
                )
            } catch (e: Exception) {
                _state.value = HomeworkState(error = e.message ?: "Błąd ładowania zadań")
            }
        }
    }
}
