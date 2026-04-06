package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    @SerialName("GlobalKey") val globalKey: String,
    @SerialName("Name") val name: String,
    @SerialName("Group") val group: String? = null
)
