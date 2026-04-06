package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GradeAverage(
    @SerialName("Id") val id: Int,
    @SerialName("PupilId") val pupilId: Int,
    @SerialName("PeriodId") val periodId: Int,
    @SerialName("Subject") val subject: Subject,
    @SerialName("Average") val average: String? = null,
    @SerialName("Points") val points: String? = null,
    @SerialName("Annotation") val annotation: JsonElement? = null,
    @SerialName("Scope") val scope: String
)
