package io.github.szpontium.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.szpontium.api.hebe.SzpontHebeCeApi
import io.github.szpontium.api.hebe.SzpontHebeApi
import io.github.szpontium.api.hebe.credentials.RsaCredential
import io.github.szpontium.api.hebe.models.Account
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class SessionStorage(
    private val dataStore: DataStore<Preferences>,
    private val httpClient: HttpClient
) {
    private val credentialKey = stringPreferencesKey("session_credential")
    private val accountsKey = stringPreferencesKey("session_accounts")

    suspend fun save(apiType: String, credential: RsaCredential, accounts: List<Account>) {
        val stored = StoredCredential(
            apiType = apiType,
            type = credential.type,
            restUrl = credential.restUrl,
            certificate = credential.certificate,
            privateKey = credential.privateKey,
            fingerprint = credential.fingerprint,
            notificationToken = credential.notificationToken,
            deviceId = credential.deviceId,
            deviceOs = credential.deviceOs,
            deviceModel = credential.deviceModel
        )
        dataStore.edit { prefs ->
            prefs[credentialKey] = json.encodeToString(stored)
            prefs[accountsKey] = json.encodeToString(ListSerializer(Account.serializer()), accounts)
        }
    }

    suspend fun restore(session: ApiSession): Boolean {
        val prefs = dataStore.data.first()
        val credentialJson = prefs[credentialKey] ?: return false
        val accountsJson = prefs[accountsKey] ?: return false
        return try {
            val stored = json.decodeFromString<StoredCredential>(credentialJson)
            val accounts = json.decodeFromString(ListSerializer(Account.serializer()), accountsJson)
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
            val api = when (stored.apiType) {
                "hebe_ce" -> SzpontHebeCeApi(credential, httpClient)
                else -> SzpontHebeApi(credential, httpClient)
            }
            session.setup(api, accounts)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
