package io.github.szpontium.session

import io.github.szpontium.api.hebe.SzpontApi
import io.github.szpontium.api.hebe.models.Account

class ApiSession {
    var api: SzpontApi? = null
    var accounts: List<Account> = emptyList()
    var selectedAccountIndex: Int = 0

    val currentAccount: Account?
        get() = accounts.getOrNull(selectedAccountIndex)

    fun setup(api: SzpontApi, accounts: List<Account>) {
        this.api = api
        this.accounts = accounts
        this.selectedAccountIndex = 0
    }

    fun clear() {
        api = null
        accounts = emptyList()
        selectedAccountIndex = 0
    }
}
