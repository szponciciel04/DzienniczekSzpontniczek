package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Grade
import io.github.szpontium.api.hebe.models.GradeAverage
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GradesState(
    val isLoading: Boolean = false,
    val grades: List<Grade> = emptyList(),
    val averages: List<GradeAverage> = emptyList(),
    val error: String? = null
)

class GradesViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(GradesState(isLoading = true))
    val state: StateFlow<GradesState> = _state

    init {
        load()
    }

    fun load() {
        val account = session.currentAccount ?: return
        val api = session.api ?: return
        val period = account.periods.firstOrNull { it.current } ?: account.periods.lastOrNull() ?: return

        viewModelScope.launch {
            _state.value = GradesState(isLoading = true)
            try {
                val grades = api.getGrades(
                    restUrl = account.unit.restUrl,
                    unitId = account.unit.id,
                    pupilId = account.pupil.id,
                    periodId = period.id
                )
                val averages = api.getGradesAverages(
                    restUrl = account.unit.restUrl,
                    unitId = account.unit.id,
                    pupilId = account.pupil.id,
                    periodId = period.id
                )
                _state.value = GradesState(grades = grades, averages = averages)
            } catch (e: Exception) {
                _state.value = GradesState(error = e.message ?: "Błąd ładowania ocen")
            }
        }
    }
}
