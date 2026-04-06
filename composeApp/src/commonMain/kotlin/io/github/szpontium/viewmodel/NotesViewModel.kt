package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Note
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotesState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val error: String? = null
)

class NotesViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState(isLoading = true))
    val state: StateFlow<NotesState> = _state

    init {
        load()
    }

    fun load() {
        val account = session.currentAccount ?: return
        val api = session.api ?: return

        viewModelScope.launch {
            _state.value = NotesState(isLoading = true)
            try {
                val notes = api.getNotes(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id
                )
                _state.value = NotesState(
                    notes = notes.sortedByDescending { it.dateValid }
                )
            } catch (e: Exception) {
                _state.value = NotesState(error = e.message ?: "Błąd ładowania uwag")
            }
        }
    }
}
