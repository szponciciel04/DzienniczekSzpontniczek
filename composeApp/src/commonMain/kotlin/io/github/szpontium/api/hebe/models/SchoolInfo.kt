@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class SchoolInfo(
    @SerialName("Id") val id: Int,
    @SerialName("UnitId") val unitId: Int,
    @SerialName("DateAt") val date: LocalDate,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime,
    @SerialName("Availability") val availability: Int,
    @SerialName("Topic") val topic: String,
    @SerialName("Content") val content: String
)
