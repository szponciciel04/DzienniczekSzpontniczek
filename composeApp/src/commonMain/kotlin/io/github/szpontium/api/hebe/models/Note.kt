@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class NoteCategory(
    @SerialName("Id") val id: Int,
    @SerialName("Name") val name: String,
    @SerialName("Type") val type: String? = null,
    @SerialName("DefaultPoints") val defaultPoints: Int? = null
)

@Serializable
data class Note(
    @SerialName("Id") val id: Int,
    @SerialName("Key") val key: String,
    @SerialName("IdPupil") val pupilId: Int,
    @SerialName("Positive") val positive: Boolean,
    @SerialName("ValidAt") val dateValid: LocalDate,
    @SerialName("ModifiedAt") val dateModify: LocalDateTime,
    @SerialName("Creator") val creator: Employee,
    @SerialName("Category") val category: NoteCategory? = null,
    @SerialName("Content") val content: String,
    @SerialName("Points") val points: Int? = null
)
