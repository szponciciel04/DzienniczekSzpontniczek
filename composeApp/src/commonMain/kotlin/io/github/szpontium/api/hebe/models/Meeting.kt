@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Meeting(
    @SerialName("Id") val id: Int,
    @SerialName("DateAt") val `when`: LocalDateTime,
    @SerialName("Where") val where: String,
    @SerialName("Why") val why: String,
    @SerialName("Agenda") val agenda: String,
    @SerialName("AdditionalInfo") val additionalInfo: String? = null,
    @SerialName("Online") val online: String,
    @SerialName("CreatedAt") val createdAt: LocalDateTime,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime
)
