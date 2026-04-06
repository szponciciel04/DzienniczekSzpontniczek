package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceMonthStats(
    @SerialName("PeriodId") val periodId: Int,
    @SerialName("Month") val month: Int,
    @SerialName("PresencePercentage") val presencePercentage: Double,
    @SerialName("Absences") val absences: Int,
    @SerialName("AbsencesJustified") val absencesJustified: Int,
    @SerialName("LateArrivals") val lateArrivals: Int,
    @SerialName("LateArrivalsJustified") val lateArrivalsJustified: Int,
    @SerialName("Exemptions") val exemptions: Int,
    @SerialName("AbsencesDueToSchool") val absencesDueToSchool: Int
)
