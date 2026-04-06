@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class GradeSummary(
    @SerialName("Id") val id: Int,
    @SerialName("PupilId") val pupilId: Int,
    @SerialName("PeriodId") val periodId: Int,
    @SerialName("Subject") val subject: Subject,
    @SerialName("Entry_1") val entry1: String? = null,
    @SerialName("Entry_2") val entry2: String? = null,
    @SerialName("Entry_3") val entry3: String? = null,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime? = null
)
