package io.github.szpontium.session

import io.github.szpontium.api.hebe.SzpontApi
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.librus.SzpontLibrusApi
import io.github.szpontium.api.librus.models.LibrusSynergiaAccount
import io.github.szpontium.api.prometheus.PrometheusMessagesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApiSession {
    // Multi-student EduVulcan sessions
    private val _studentSessions = MutableStateFlow<List<StudentSession>>(emptyList())
    val studentSessions: StateFlow<List<StudentSession>> = _studentSessions.asStateFlow()

    private val _activeStudent = MutableStateFlow<StudentSession?>(null)
    val activeStudent: StateFlow<StudentSession?> = _activeStudent.asStateFlow()

    // Legacy / Librus fallbacks
    var legacyApi: SzpontApi? = null
    var legacyAccounts: List<Account> = emptyList()
    var selectedAccountIndex: Int = 0
    var librusApi: SzpontLibrusApi? = null
    var librusAccounts: List<LibrusSynergiaAccount> = emptyList()
    var librusPortalToken: String? = null

    val currentAccount: Account?
        get() = _activeStudent.value?.account ?: legacyAccounts.getOrNull(selectedAccountIndex)

    val api: SzpontApi?
        get() = _activeStudent.value?.api ?: legacyApi

    val prometheusMessagesApi: PrometheusMessagesApi?
        get() = _activeStudent.value?.prometheusMessagesApi

    var prometheusMailboxKey: String?
        get() = _activeStudent.value?.prometheusMailboxKey
        set(value) {
            _activeStudent.value?.prometheusMailboxKey = value
        }

    val enabledStudents: List<StudentSession>
        get() = _studentSessions.value.filter { it.isEnabled }

    fun setStudentSessions(sessions: List<StudentSession>, selectId: String? = null) {
        _studentSessions.value = sessions
        val enabled = sessions.filter { it.isEnabled }
        val toSelect = enabled.firstOrNull { it.id == selectId }
            ?: enabled.firstOrNull()
            ?: sessions.firstOrNull()
        _activeStudent.value = toSelect
    }

    fun addStudentSessions(sessions: List<StudentSession>) {
        val current = _studentSessions.value.toMutableList()
        sessions.forEach { newSession ->
            val index = current.indexOfFirst { it.id == newSession.id }
            if (index >= 0) {
                current[index] = newSession
            } else {
                current.add(newSession)
            }
        }
        _studentSessions.value = current
        if (_activeStudent.value == null) {
            _activeStudent.value = current.firstOrNull { it.isEnabled }
        }
    }

    fun selectStudent(studentId: String) {
        val match = _studentSessions.value.firstOrNull { it.id == studentId && it.isEnabled }
        if (match != null) {
            _activeStudent.value = match
        }
    }

    fun toggleStudentEnabled(studentId: String, enabled: Boolean) {
        val current = _studentSessions.value
        val updated = current.map {
            if (it.id == studentId) {
                it.apply { isEnabled = enabled }
            } else it
        }
        _studentSessions.value = updated

        // If the active student was disabled, select a new active student
        if (_activeStudent.value?.id == studentId && !enabled) {
            _activeStudent.value = updated.firstOrNull { it.isEnabled }
        } else if (_activeStudent.value == null && enabled) {
            _activeStudent.value = updated.firstOrNull { it.id == studentId }
        }
    }

    fun removeStudent(studentId: String) {
        val current = _studentSessions.value.filterNot { it.id == studentId }
        _studentSessions.value = current
        if (_activeStudent.value?.id == studentId) {
            _activeStudent.value = current.firstOrNull { it.isEnabled } ?: current.firstOrNull()
        }
    }

    fun setup(api: SzpontApi, accounts: List<Account>) {
        this.legacyApi = api
        this.legacyAccounts = accounts
        this.selectedAccountIndex = 0
    }

    fun clear() {
        _studentSessions.value = emptyList()
        _activeStudent.value = null
        legacyApi = null
        legacyAccounts = emptyList()
        selectedAccountIndex = 0
        librusApi = null
        librusAccounts = emptyList()
        librusPortalToken = null
    }
}
