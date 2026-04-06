@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Duty(
    @SerialName("Id") val id: Int,
    @SerialName("UnitId") val unitId: Int,
    @SerialName("JournalId") val journalId: Int,
    @SerialName("PupilId") val pupilId: Int,
    @SerialName("DateAt") val date: LocalDate,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime
)
