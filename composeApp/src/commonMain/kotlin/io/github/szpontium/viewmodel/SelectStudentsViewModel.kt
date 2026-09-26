package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.navigation.CandidateStudent
import io.github.szpontium.session.ApiSession
import io.github.szpontium.session.SessionStorage
import io.github.szpontium.session.StoredRsaCredential
import io.github.szpontium.session.StudentSession
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class SelectStudentsViewModel(
    private val session: ApiSession,
    private val sessionStorage: SessionStorage,
    private val httpClient: HttpClient
) : ViewModel() {

    private val _candidates = MutableStateFlow<List<CandidateStudent>>(emptyList())
    val candidates: StateFlow<List<CandidateStudent>> = _candidates.asStateFlow()

    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()

    fun initCandidates(list: List<CandidateStudent>) {
        if (_candidates.value.isEmpty()) {
            _candidates.value = list
        }
    }

    fun toggleCandidate(id: String) {
        _candidates.value = _candidates.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun confirmSelection(onSuccess: () -> Unit) {
        val selected = _candidates.value.filter { it.isSelected }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _isConfirming.value = true
            try {
                val newSessions = selected.map { cand ->
                    val account = json.decodeFromString<Account>(cand.accountJson)
                    val storedCred = json.decodeFromString<StoredRsaCredential>(cand.credentialJson)
                    StudentSession(
                        id = cand.id,
                        account = account,
                        credential = storedCred.toRsaCredential(),
                        restUrl = cand.restUrl,
                        prometheusLogin = cand.pLogin,
                        prometheusPassword = cand.pPassword,
                        prometheusTenant = cand.tenant,
                        isEnabled = true,
                        httpClient = httpClient
                    )
                }

                session.addStudentSessions(newSessions)
                
                // Save all active sessions to DataStore
                val currentSessions = session.studentSessions.value
                val activeId = session.activeStudent.value?.id ?: newSessions.firstOrNull()?.id
                sessionStorage.saveStudents(currentSessions, activeId)

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isConfirming.value = false
            }
        }
    }
}
