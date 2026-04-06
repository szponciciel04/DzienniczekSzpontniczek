package io.github.szpontium.api.hebe.credentials

import io.github.szpontium.api.hebe.signer.HebeX509Signer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RsaCredential(
    override val type: String,
    override var restUrl: String?,
    override val certificate: String,
    override val privateKey: String,
    override val fingerprint: String,
    override val notificationToken: String?,
    override val deviceId: String,
    override val deviceOs: String,
    override val deviceModel: String
) : ICredential {

    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun createNew(
            deviceOs: String,
            deviceModel: String,
            restUrl: String? = null,
            notificationToken: String? = null
        ): RsaCredential {
            val (publicKey, privateKey, _) = HebeX509Signer.generateKeyPair()
            val (certificate, fingerprint) = HebeX509Signer.generateCertificate(publicKey, privateKey)
            return RsaCredential(
                type = "X509",
                restUrl = restUrl,
                certificate = certificate,
                privateKey = privateKey,
                fingerprint = fingerprint,
                notificationToken = notificationToken,
                deviceId = Uuid.random().toString(),
                deviceOs = deviceOs,
                deviceModel = deviceModel
            )
        }
    }
}
