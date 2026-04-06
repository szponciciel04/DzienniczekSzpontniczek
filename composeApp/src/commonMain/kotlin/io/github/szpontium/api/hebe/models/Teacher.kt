package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    @SerialName("Description") val description: String,
    @SerialName("Position") val position: Int,
    @SerialName("BoxId") val boxId: String,
    @SerialName("Id") val id: Int,
    @SerialName("Surname") val surname: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("DisplayName") val displayName: String
)
