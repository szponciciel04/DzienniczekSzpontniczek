package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceType(
    @SerialName("Id") val id: Int,
    @SerialName("Symbol") val symbol: String,
    @SerialName("Name") val name: String,
    @SerialName("CategoryId") val categoryId: Int,
    @SerialName("CategoryName") val categoryName: String,
    @SerialName("Position") val position: Int,
    @SerialName("Presence") val presence: Boolean,
    @SerialName("Absence") val absence: Boolean,
    @SerialName("LegalAbsence") val legalAbsence: Boolean,
    @SerialName("Late") val late: Boolean,
    @SerialName("AbsenceJustified") val absenceJustified: Boolean,
    @SerialName("Removed") val removed: Boolean
)
