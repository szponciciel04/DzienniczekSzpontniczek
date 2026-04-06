package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.hebe.models.LuckyNumber
import io.github.szpontium.session.ApiSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val session: ApiSession
) : ViewModel() {

    private val _accounts = MutableStateFlow(session.accounts)
    val accounts: StateFlow<List<Account>> = _accounts

    private val _selectedIndex = MutableStateFlow(session.selectedAccountIndex)
    val selectedIndex: StateFlow<Int> = _selectedIndex

    private val _luckyNumber = MutableStateFlow<LuckyNumber?>(null)
    val luckyNumber: StateFlow<LuckyNumber?> = _luckyNumber

    val currentAccount: Account?
        get() = session.currentAccount

    init {
        loadLuckyNumber()
    }

    fun selectAccount(index: Int) {
        session.selectedAccountIndex = index
        _selectedIndex.value = index
        loadLuckyNumber()
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
