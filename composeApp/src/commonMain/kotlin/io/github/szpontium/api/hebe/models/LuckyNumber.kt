package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LuckyNumber(
    @SerialName("Day") val day: LocalDate,
    @SerialName("Number") val number: Int
)
