package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceExtra(
    @SerialName("Id") val id: Int,
    @SerialName("PresenceType") val presenceType: PresenceType? = null,
    @SerialName("DayAt") val day: LocalDate,
    @SerialName("TimeSlot") val timeSlot: Timeslot,
    @SerialName("IdWeakRef") val idWeakRef: Int? = null,
    @SerialName("Type") val type: Int
)
