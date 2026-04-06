package io.github.szpontium.api.hebe.credentials

interface ICredential {
    val type: String
    var restUrl: String?
    val certificate: String
    val privateKey: String
    val fingerprint: String
    val notificationToken: String?
    val deviceId: String
    val deviceOs: String
    val deviceModel: String
}
