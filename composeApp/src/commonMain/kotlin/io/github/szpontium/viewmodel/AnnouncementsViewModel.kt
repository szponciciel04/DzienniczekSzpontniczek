package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Announcement
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AnnouncementsState(
    val isLoading: Boolean = false,
    val announcements: List<Announcement> = emptyList(),
    val error: String? = null
)

class AnnouncementsViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _state = MutableStateFlow(AnnouncementsState(isLoading = true))
    val state: StateFlow<AnnouncementsState> = _state

    init {
        load()
    }

    fun load() {
        val account = session.currentAccount ?: return
        val api = session.api ?: return

        viewModelScope.launch {
            _state.value = AnnouncementsState(isLoading = true)
            try {
                val announcements = api.getAnnouncements(
                    restUrl = account.unit.restUrl,
                    unitId = account.unit.id,
                    pupilId = account.pupil.id
                )
                _state.value = AnnouncementsState(announcements = announcements)
            } catch (e: Exception) {
                _state.value = AnnouncementsState(error = e.message ?: "Błąd ładowania ogłoszeń")
            }
        }
    }
}
