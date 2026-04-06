package io.github.szpontium.session

import kotlinx.serialization.Serializable

@Serializable
data class StoredCredential(
    val apiType: String,
    val type: String,
    val restUrl: String?,
    val certificate: String,
    val privateKey: String,
    val fingerprint: String,
    val notificationToken: String?,
    val deviceId: String,
    val deviceOs: String,
    val deviceModel: String
)
