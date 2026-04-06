@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class UserEvent(
    @SerialName("Id") val id: Int,
    @SerialName("PupilId") val pupilId: Int,
    @SerialName("Name") val name: String,
    @SerialName("Description") val description: String? = null,
    @SerialName("DisplayMode") val displayMode: Int,
    @SerialName("DateAt") val date: LocalDate,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime,
    @SerialName("StartTime") val startTime: LocalTime,
    @SerialName("EndTime") val endTime: LocalTime,
    @SerialName("RepeatMode") val repeatMode: Int,
    @SerialName("EndAt") val endAt: LocalDateTime? = null,
    @SerialName("IsOwner") val isOwner: Boolean
)
