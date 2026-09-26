package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Note
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
        viewModelScope.launch {
            session.activeStudent.collectLatest {
                load()
            }
        }
    }

    fun load() {
        val account = session.currentAccount
        val api = session.api
        if (account == null || api == null) {
            _state.value = NotesState(isLoading = false)
            return
        }

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
