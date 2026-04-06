package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Clazz(
    @SerialName("Id") val id: Int,
    @SerialName("Key") val key: String,
    @SerialName("DisplayName") val displayName: String,
    @SerialName("Symbol") val symbol: String
)
