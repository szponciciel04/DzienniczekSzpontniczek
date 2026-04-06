@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class GradeCategory(
    @SerialName("Id") val id: Int,
    @SerialName("Name") val name: String,
    @SerialName("Code") val code: String
)

@Serializable
data class GradeColumn(
    @SerialName("Id") val id: Int,
    @SerialName("Key") val key: String,
    @SerialName("PeriodId") val periodId: Int,
    @SerialName("Name") val name: String,
    @SerialName("Code") val code: String,
    @SerialName("Group") val group: String,
    @SerialName("Number") val number: Int,
    @SerialName("Color") val color: Int,
    @SerialName("Weight") val weight: Double,
    @SerialName("Subject") val subject: Subject,
    @SerialName("Category") val category: GradeCategory? = null
)

@Serializable
data class Grade(
    @SerialName("Id") val id: Int,
    @SerialName("Key") val key: String,
    @SerialName("PupilId") val pupilId: Int,
    @SerialName("ContentRaw") val contentRaw: String,
    @SerialName("Content") val content: String,
    @SerialName("Comment") val comment: String,
    @SerialName("Value") val value: Double? = null,
    @SerialName("Numerator") @Serializable(with = VulcanNullableIntSerializer::class) val numerator: Int? = null,
    @SerialName("Denominator") @Serializable(with = VulcanNullableIntSerializer::class) val denominator: Int? = null,
    @SerialName("CreatedAt") val createdAt: LocalDateTime,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime,
    @SerialName("Creator") val creator: Employee,
    @SerialName("Modifier") val modifier: Employee,
    @SerialName("Column") val column: GradeColumn,
    @SerialName("CorrectedGrade") val correctedGrade: String? = null
)
