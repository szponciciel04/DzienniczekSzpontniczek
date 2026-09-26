package io.github.szpontium.session

import io.github.szpontium.api.hebe.SzpontApi
import io.github.szpontium.api.hebe.SzpontHebeCeApi
import io.github.szpontium.api.hebe.credentials.RsaCredential
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.prometheus.PrometheusMessagesApi
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable

@Serializable
data class StoredRsaCredential(
    val type: String,
    val restUrl: String?,
    val certificate: String,
    val privateKey: String,
    val fingerprint: String,
    val notificationToken: String?,
    val deviceId: String,
    val deviceOs: String,
    val deviceModel: String
) {
    fun toRsaCredential(): RsaCredential {
        return RsaCredential(
            type = type,
            restUrl = restUrl,
            certificate = certificate,
            privateKey = privateKey,
            fingerprint = fingerprint,
            notificationToken = notificationToken,
            deviceId = deviceId,
            deviceOs = deviceOs,
            deviceModel = deviceModel
        )
    }
}

fun RsaCredential.toStoredRsaCredential(): StoredRsaCredential {
    return StoredRsaCredential(
        type = type,
        restUrl = restUrl,
        certificate = certificate,
        privateKey = privateKey,
        fingerprint = fingerprint,
        notificationToken = notificationToken,
        deviceId = deviceId,
        deviceOs = deviceOs,
        deviceModel = deviceModel
    )
}

@Serializable
data class StoredStudentSession(
    val id: String,
    val account: Account,
    val credential: StoredRsaCredential,
    val restUrl: String,
    val prometheusLogin: String? = null,
    val prometheusPassword: String? = null,
    val prometheusTenant: String? = null,
    val isEnabled: Boolean = true
)

class StudentSession(
    val id: String,
    val account: Account,
    val credential: RsaCredential,
    val restUrl: String,
    val prometheusLogin: String? = null,
    val prometheusPassword: String? = null,
    val prometheusTenant: String? = null,
    var isEnabled: Boolean = true,
    httpClient: HttpClient
) {
    val api: SzpontApi

    init {
        credential.restUrl = restUrl
        api = SzpontHebeCeApi(credential, httpClient)
    }

    val prometheusMessagesApi: PrometheusMessagesApi? = if (prometheusLogin != null && prometheusPassword != null && prometheusTenant != null) {
        PrometheusMessagesApi(
            tenant = prometheusTenant,
            login = prometheusLogin,
            password = prometheusPassword
        )
    } else null

    var prometheusMailboxKey: String? = null

    fun toStored(): StoredStudentSession {
        return StoredStudentSession(
            id = id,
            account = account,
            credential = credential.toStoredRsaCredential(),
            restUrl = restUrl,
            prometheusLogin = prometheusLogin,
            prometheusPassword = prometheusPassword,
            prometheusTenant = prometheusTenant,
            isEnabled = isEnabled
        )
    }

    companion object {
        fun generateId(account: Account): String {
            return "${account.pupil.id}_${account.unit.id}_${account.journal?.id ?: 0}_${account.constituentUnit.id}"
        }
    }
}
