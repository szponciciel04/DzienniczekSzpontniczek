package io.github.szpontium.api.prometheus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JwtPayload(
    val name: String,
    val uid: String,
    val tenant: String,
    @SerialName("unituid")
    val unitUId: String,
    val uri: String,
    val service: JsonElement,
    val caps: JsonElement,
    val nbf: Long,
    val exp: Long,
    val iat: Long,
)
