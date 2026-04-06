@file:UseSerializers(VulcanDateTimeSerializer::class, VulcanDateSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement

@Serializable
data class Exam(
    @SerialName("Id") val id: Int,
    @SerialName("Key") val key: String,
    @SerialName("Type") val type: String,
    @SerialName("TypeId") val typeId: Int,
    @SerialName("Content") val content: String,
    @SerialName("CreatedAt") val createdAt: LocalDateTime,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime,
    @SerialName("DeadlineAt") val deadline: LocalDate,
    @SerialName("Creator") val creator: Employee,
    @SerialName("Subject") val subject: Subject,
    @SerialName("PupilId") val pupilId: Int,
    @SerialName("Didactics") val didactics: JsonElement? = null
)
