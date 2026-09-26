package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.hebe.models.LuckyNumber
import io.github.szpontium.session.ApiSession
import io.github.szpontium.session.SessionStorage
import io.github.szpontium.session.StudentSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val session: ApiSession,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    val activeStudent: StateFlow<StudentSession?> = session.activeStudent
    val studentSessions: StateFlow<List<StudentSession>> = session.studentSessions

    private val _luckyNumber = MutableStateFlow<LuckyNumber?>(null)
    val luckyNumber: StateFlow<LuckyNumber?> = _luckyNumber.asStateFlow()

    val currentAccount: Account?
        get() = session.currentAccount

    init {
        viewModelScope.launch {
            session.activeStudent.collectLatest {
                _luckyNumber.value = null
                loadLuckyNumber()
            }
        }
    }

    fun selectStudent(studentId: String) {
        session.selectStudent(studentId)
        viewModelScope.launch {
            sessionStorage.setActiveStudentId(studentId)
        }
    }

    private fun loadLuckyNumber() {
        val account = session.currentAccount ?: return
        val api = session.api ?: return
        viewModelScope.launch {
            runCatching {
                api.getLuckyNumber(
                    restUrl = account.unit.restUrl,
                    pupilId = account.pupil.id,
                    constituentUnitId = account.constituentUnit.id
                )
            }.onSuccess { _luckyNumber.value = it }
        }
    }
}
