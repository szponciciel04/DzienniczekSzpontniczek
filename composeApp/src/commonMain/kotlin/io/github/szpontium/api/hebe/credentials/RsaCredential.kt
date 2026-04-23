package io.github.szpontium.api.hebe.credentials

import io.github.szpontium.api.hebe.signer.HebeSzpontSigner
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
            val (publicKey, privatePem, fingerprint) = HebeSzpontSigner.generateKeyPair()
            return RsaCredential(
                type = "RSA_PEM",
                restUrl = restUrl,
                certificate = publicKey,
                privateKey = privatePem,
                fingerprint = fingerprint,
                notificationToken = notificationToken,
                deviceId = Uuid.random().toString(),
                deviceOs = deviceOs,
                deviceModel = deviceModel
            )
        }
    }
}
