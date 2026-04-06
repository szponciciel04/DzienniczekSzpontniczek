package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    @SerialName("Name") val name: String,
    @SerialName("Link") val link: String
)
