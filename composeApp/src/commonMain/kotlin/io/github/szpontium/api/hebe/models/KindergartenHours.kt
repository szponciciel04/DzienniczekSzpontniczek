package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KindergartenHours(
    @SerialName("Id") val constituentUnitId: Int,
    @SerialName("HourFrom") val hourFrom: LocalTime,
    @SerialName("HourTo") val hourTo: LocalTime
)
