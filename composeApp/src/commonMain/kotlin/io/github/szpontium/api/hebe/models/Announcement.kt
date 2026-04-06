@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Announcement(
    @SerialName("Id") val id: Int,
    @SerialName("IdUnit") val unitId: Int,
    @SerialName("Title") val title: String,
    @SerialName("Content") val content: String,
    @SerialName("Category") val category: String? = null,
    @SerialName("From") val dateFrom: LocalDate,
    @SerialName("To") val dateTo: LocalDate,
    @SerialName("Sender") val sender: Employee,
    @SerialName("Attachments") val attachments: List<Attachment>,
    @SerialName("CreatedAt") val createdAt: LocalDateTime,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime
)
