package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PresenceExtraInfo(
    @SerialName("Id") val id: Int,
    @SerialName("Label") val label: String,
    @SerialName("Values") val values: JsonElement? = null
)
