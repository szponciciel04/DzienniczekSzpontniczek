package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Room(
    @SerialName("Id") val id: Int,
    @SerialName("Code") val code: String
)
