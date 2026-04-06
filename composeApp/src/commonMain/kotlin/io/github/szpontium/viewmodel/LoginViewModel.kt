package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.SzpontHebeCeApi
import io.github.szpontium.api.hebe.SzpontHebeApi
import io.github.szpontium.api.hebe.credentials.RsaCredential
import io.github.szpontium.api.prometheus.PrometheusLoginHelper
import io.github.szpontium.session.ApiSession
import io.github.szpontium.session.SessionStorage
import io.ktor.client.HttpClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface LoginEvent {
    data object Success : LoginEvent
    data class Error(val message: String) : LoginEvent
}

class LoginViewModel(
    private val session: ApiSession,
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun loginWithEduVulcan(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            viewModelScope.launch {
                _events.send(LoginEvent.Error("Login i hasło nie mogą być puste"))
            }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val helper = PrometheusLoginHelper()
                val result = helper.login(
                    login = login.trim(),
                    password = password,
                    deviceModel = "Android"
                )
                val tenant = result.tenantTokens.keys.first()
                val tokens = result.tenantTokens.values.toList()

                val credential = RsaCredential.createNew(
                    deviceOs = "Android",
                    deviceModel = "Android"
                )
                val api = SzpontHebeCeApi(credential, httpClient)
                api.registerByJwt(tokens, tenant)

                val accounts = api.getAccounts()
                session.setup(api, accounts)
                sessionStorage.save("hebe_ce", credential, accounts)
                _events.send(LoginEvent.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                _events.send(LoginEvent.Error(e.message ?: "Błąd logowania"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithToken(token: String, pin: String, symbol: String) {
        if (token.isBlank() || pin.isBlank() || symbol.isBlank()) {
            viewModelScope.launch {
                _events.send(LoginEvent.Error("Token, PIN i symbol nie mogą być puste"))
            }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val credential = RsaCredential.createNew(
                    deviceOs = "Android",
                    deviceModel = "Android"
                )
                val api = SzpontHebeApi(credential, httpClient)
                api.registerByToken(
                    securityToken = token.trim(),
                    pin = pin.trim(),
                    tenant = symbol.trim()
                )
                val accounts = api.getAccounts()
                session.setup(api, accounts)
                sessionStorage.save("hebe", credential, accounts)
                _events.send(LoginEvent.Success)
            } catch (e: Exception) {
                _events.send(LoginEvent.Error(e.message ?: "Błąd rejestracji"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
