package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vacation(
    @SerialName("Id") val id: Int,
    @SerialName("Name") val name: String,
    @SerialName("From") val dateFrom: LocalDate,
    @SerialName("To") val dateTo: LocalDate
)
