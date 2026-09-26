package io.github.szpontium.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.szpontium.api.hebe.SzpontHebeApi
import io.github.szpontium.api.hebe.SzpontHebeCeApi
import io.github.szpontium.api.hebe.credentials.RsaCredential
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.librus.SzpontLibrusAdapterApi
import io.github.szpontium.api.librus.SzpontLibrusApi
import io.github.szpontium.api.librus.models.LibrusSynergiaAccount
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class SessionStorage(
    private val dataStore: DataStore<Preferences>,
    private val httpClient: HttpClient
) {
    private val multiStudentsKey = stringPreferencesKey("session_multi_students")
    private val activeStudentIdKey = stringPreferencesKey("session_active_student_id")

    // Legacy keys for migration / fallback
    private val credentialKey = stringPreferencesKey("session_credential")
    private val accountsKey = stringPreferencesKey("session_accounts")

    suspend fun saveStudents(
        sessions: List<StudentSession>,
        activeStudentId: String? = null
    ) {
        val storedSessions = sessions.map { it.toStored() }
        dataStore.edit { prefs ->
            prefs[multiStudentsKey] = json.encodeToString(ListSerializer(StoredStudentSession.serializer()), storedSessions)
            if (activeStudentId != null) {
                prefs[activeStudentIdKey] = activeStudentId
            }
        }
    }

    suspend fun setActiveStudentId(activeStudentId: String) {
        dataStore.edit { prefs ->
            prefs[activeStudentIdKey] = activeStudentId
        }
    }

    suspend fun save(
        apiType: String,
        credential: RsaCredential,
        accounts: List<Account>,
        pLogin: String? = null,
        pPassword: String? = null,
        pTenant: String? = null
    ) {
        val restUrl = credential.restUrl ?: ""
        val newSessions = accounts.map { account ->
            StudentSession(
                id = StudentSession.generateId(account),
                account = account,
                credential = credential,
                restUrl = restUrl,
                prometheusLogin = pLogin,
                prometheusPassword = pPassword,
                prometheusTenant = pTenant,
                isEnabled = true,
                httpClient = httpClient
            )
        }
        
        // Merge with existing student sessions if any
        val prefs = dataStore.data.first()
        val existingJson = prefs[multiStudentsKey]
        val existing: List<StudentSession> = if (!existingJson.isNullOrBlank()) {
            try {
                val stored = json.decodeFromString(ListSerializer(StoredStudentSession.serializer()), existingJson)
                stored.map { s ->
                    StudentSession(
                        id = s.id,
                        account = s.account,
                        credential = s.credential.toRsaCredential(),
                        restUrl = s.restUrl,
                        prometheusLogin = s.prometheusLogin,
                        prometheusPassword = s.prometheusPassword,
                        prometheusTenant = s.prometheusTenant,
                        isEnabled = s.isEnabled,
                        httpClient = httpClient
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val mergedMap = existing.associateBy { it.id }.toMutableMap()
        newSessions.forEach { mergedMap[it.id] = it }
        val mergedList = mergedMap.values.toList()

        val activeId = newSessions.firstOrNull()?.id ?: prefs[activeStudentIdKey]
        saveStudents(mergedList, activeId)
    }

    suspend fun saveLibrus(
        email: String,
        password: String,
        portalToken: String,
        apiToken: String,
        accounts: List<Account>,
        synergiaAccounts: List<LibrusSynergiaAccount>
    ) {
        val stored = StoredCredential(
            apiType = "librus",
            type = "librus",
            restUrl = "librus",
            certificate = "",
            privateKey = "",
            fingerprint = "",
            notificationToken = null,
            deviceId = "librus",
            deviceOs = "Android",
            deviceModel = "LibrusClient",
            librusEmail = email,
            librusPassword = password,
            librusPortalToken = portalToken,
            librusApiToken = apiToken,
            librusAccountsJson = json.encodeToString(synergiaAccounts)
        )
        dataStore.edit { prefs ->
            prefs[credentialKey] = json.encodeToString(stored)
            prefs[accountsKey] = json.encodeToString(ListSerializer(Account.serializer()), accounts)
        }
    }

    suspend fun restore(session: ApiSession): Boolean {
        val prefs = dataStore.data.first()

        // 1. Check for multi-student EduVulcan sessions
        val multiJson = prefs[multiStudentsKey]
        if (!multiJson.isNullOrBlank()) {
            return try {
                val storedSessions = json.decodeFromString(ListSerializer(StoredStudentSession.serializer()), multiJson)
                if (storedSessions.isEmpty()) return false

                val activeId = prefs[activeStudentIdKey]
                val sessions = storedSessions.map { s ->
                    StudentSession(
                        id = s.id,
                        account = s.account,
                        credential = s.credential.toRsaCredential(),
                        restUrl = s.restUrl,
                        prometheusLogin = s.prometheusLogin,
                        prometheusPassword = s.prometheusPassword,
                        prometheusTenant = s.prometheusTenant,
                        isEnabled = s.isEnabled,
                        httpClient = httpClient
                    )
                }
                session.setStudentSessions(sessions, activeId)
                true
            } catch (e: Exception) {
                false
            }
        }

        // 2. Migration / Fallback for single-account credential
        val credentialJson = prefs[credentialKey] ?: return false
        val accountsJson = prefs[accountsKey] ?: return false
        return try {
            val stored = json.decodeFromString<StoredCredential>(credentialJson)
            val accounts = json.decodeFromString(ListSerializer(Account.serializer()), accountsJson)

            if (stored.apiType == "librus") {
                val portalToken = stored.librusPortalToken ?: return false
                val apiToken = stored.librusApiToken ?: return false
                val synergiaAccounts: List<LibrusSynergiaAccount> =
                    stored.librusAccountsJson?.let { json.decodeFromString(it) } ?: emptyList()
                val firstAccount = synergiaAccounts.firstOrNull() ?: return false
                
                val librusApi = SzpontLibrusApi(
                    httpClient = httpClient,
                    portalAccessToken = portalToken,
                    apiAccessToken = apiToken
                )
                val adapter = SzpontLibrusAdapterApi(
                    librusApi = librusApi,
                    currentSynergiaAccount = firstAccount,
                    httpClient = httpClient
                )
                session.setup(adapter, accounts)
                session.librusApi = librusApi
                session.librusAccounts = synergiaAccounts
                session.librusPortalToken = portalToken
                return true
            }

            val credential = RsaCredential(
                type = stored.type,
                restUrl = stored.restUrl,
                certificate = stored.certificate,
                privateKey = stored.privateKey,
                fingerprint = stored.fingerprint,
                notificationToken = stored.notificationToken,
                deviceId = stored.deviceId,
                deviceOs = stored.deviceOs,
                deviceModel = stored.deviceModel
            )

            if (stored.prometheusLogin == null || stored.prometheusPassword == null || stored.prometheusTenant == null) {
                return false
            }

            // Migrate single credential into multi-student session
            val restUrl = credential.restUrl ?: ""
            val migratedSessions = accounts.map { account ->
                StudentSession(
                    id = StudentSession.generateId(account),
                    account = account,
                    credential = credential,
                    restUrl = restUrl,
                    prometheusLogin = stored.prometheusLogin,
                    prometheusPassword = stored.prometheusPassword,
                    prometheusTenant = stored.prometheusTenant,
                    isEnabled = true,
                    httpClient = httpClient
                )
            }
            session.setStudentSessions(migratedSessions)
            saveStudents(migratedSessions, migratedSessions.firstOrNull()?.id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
