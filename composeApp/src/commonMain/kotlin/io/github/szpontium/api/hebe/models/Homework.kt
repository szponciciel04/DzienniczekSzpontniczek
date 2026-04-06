@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement

@Serializable
data class Homework(
    @SerialName("Id") val id: Int,
    @SerialName("Key") val key: String,
    @SerialName("IdPupil") val pupilId: Int,
    @SerialName("IdHomework") val homeworkId: Int,
    @SerialName("Content") val content: String,
    @SerialName("IsAnswerRequired") val isAnswerRequired: Boolean,
    @SerialName("CreatedAt") val createdAt: LocalDateTime,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime,
    @SerialName("DateAt") val date: LocalDate,
    @SerialName("AnswerAt") val answerAt: LocalDateTime? = null,
    @SerialName("DeadlineAt") val deadline: LocalDate,
    @SerialName("Creator") val creator: Employee,
    @SerialName("Subject") val subject: Subject,
    @SerialName("Attachments") val attachments: List<Attachment>,
    @SerialName("Didactics") val didactics: JsonElement? = null
)
