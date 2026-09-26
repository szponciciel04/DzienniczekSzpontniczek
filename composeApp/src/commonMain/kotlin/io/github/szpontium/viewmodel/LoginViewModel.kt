package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.api.hebe.SzpontHebeApi
import io.github.szpontium.api.hebe.SzpontHebeCeApi
import io.github.szpontium.api.hebe.credentials.RsaCredential
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.librus.LibrusLoginHelper
import io.github.szpontium.api.librus.LibrusMapper
import io.github.szpontium.api.librus.SzpontLibrusAdapterApi
import io.github.szpontium.api.librus.SzpontLibrusApi
import io.github.szpontium.api.prometheus.PrometheusLoginHelper
import io.github.szpontium.navigation.CandidateStudent
import io.github.szpontium.session.ApiSession
import io.github.szpontium.session.SessionStorage
import io.github.szpontium.session.StudentSession
import io.github.szpontium.session.toStoredRsaCredential
import io.ktor.client.HttpClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

sealed interface LoginEvent {
    data object Success : LoginEvent
    data class SelectStudents(val candidates: List<CandidateStudent>) : LoginEvent
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

                val candidates = mutableListOf<CandidateStudent>()
                val tokensByTenant = if (result.tokensByTenant.isNotEmpty()) {
                    result.tokensByTenant
                } else {
                    val defaultTenant = result.tenantTokens.keys.firstOrNull() ?: "eduvulcan"
                    mapOf(defaultTenant to result.tokens)
                }

                for ((tenant, tokens) in tokensByTenant) {
                    if (tokens.isEmpty()) continue
                    val credential = RsaCredential.createNew(
                        deviceOs = "Android",
                        deviceModel = "Android"
                    )
                    val api = SzpontHebeCeApi(credential, httpClient)
                    val restUrl = api.registerByJwt(tokens, tenant)
                    val accounts = api.getAccounts()

                    for (account in accounts) {
                        val studentId = StudentSession.generateId(account)
                        candidates.add(
                            CandidateStudent(
                                id = studentId,
                                pupilFirstName = account.pupil.firstName,
                                pupilSurname = account.pupil.surname,
                                schoolName = account.unit.displayName.ifBlank { account.unit.name },
                                classDisplay = account.classDisplay ?: "",
                                tenant = tenant,
                                restUrl = restUrl,
                                accountJson = json.encodeToString(Account.serializer(), account),
                                credentialJson = json.encodeToString(credential.toStoredRsaCredential()),
                                pLogin = login.trim(),
                                pPassword = password,
                                isSelected = true
                            )
                        )
                    }
                }

                if (candidates.isEmpty()) {
                    _events.send(LoginEvent.Error("Brak kont uczniów na tym koncie EduVulcan"))
                } else {
                    _events.send(LoginEvent.SelectStudents(candidates))
                }
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

    fun loginWithLibrus(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch {
                _events.send(LoginEvent.Error("E-mail i hasło nie mogą być puste"))
            }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loginHelper = LibrusLoginHelper()
                val tokenResponse = loginHelper.login(email.trim(), password)
                val portalToken = tokenResponse.accessToken

                val librusApi = SzpontLibrusApi(
                    httpClient = httpClient,
                    portalAccessToken = portalToken
                )

                val synergiaAccounts = librusApi.getSynergiaAccounts()
                if (synergiaAccounts.isEmpty()) {
                    _events.send(LoginEvent.Error("Brak powiązanych kont Synergia na tym koncie Librus"))
                    return@launch
                }

                val firstAccount = synergiaAccounts.first()
                val apiToken = firstAccount.accessToken ?: librusApi.getFreshApiToken(firstAccount.login)
                librusApi.apiAccessToken = apiToken

                val me = librusApi.getMe()
                val accounts = synergiaAccounts.map { sAcc ->
                    LibrusMapper.toHebeAccount(sAcc, me)
                }

                val adapterApi = SzpontLibrusAdapterApi(
                    librusApi = librusApi,
                    currentSynergiaAccount = firstAccount,
                    httpClient = httpClient
                )
                session.setup(adapterApi, accounts)
                session.librusApi = librusApi
                session.librusAccounts = synergiaAccounts
                session.librusPortalToken = portalToken

                sessionStorage.saveLibrus(
                    email = email.trim(),
                    password = password,
                    portalToken = portalToken,
                    apiToken = apiToken,
                    accounts = accounts,
                    synergiaAccounts = synergiaAccounts
                )

                _events.send(LoginEvent.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                _events.send(LoginEvent.Error(e.message ?: "Błąd logowania do Librusa"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
