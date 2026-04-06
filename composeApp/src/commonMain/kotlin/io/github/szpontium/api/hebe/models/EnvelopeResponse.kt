package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiStatus(
    @SerialName("Code") val code: Int,
    @SerialName("Message") val message: String
)

@Serializable
data class EnvelopeResponse(
    @SerialName("EnvelopeType") val envelopeType: String = "",
    @SerialName("Envelope") val envelope: JsonElement? = null,
    @SerialName("Status") val status: ApiStatus,
    @SerialName("RequestId") val requestId: String = "",
    @SerialName("Timestamp") val timestamp: Long = 0,
    @SerialName("TimestampFormatted") val timestampFormatted: String = ""
)
