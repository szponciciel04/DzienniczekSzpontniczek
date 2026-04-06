package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    @SerialName("Id") val id: Int,
    @SerialName("TripId") val tripId: Int,
    @SerialName("From") val dateFrom: LocalDate,
    @SerialName("To") val dateTo: LocalDate,
    @SerialName("Description") val description: String,
    @SerialName("Supervisor") val supervisor: String,
    @SerialName("Goal") val goal: String,
    @SerialName("Route") val route: String,
    @SerialName("Transport") val transport: String,
    @SerialName("StartTimeslot") val startTimeslot: Timeslot? = null,
    @SerialName("EndTimeslot") val endTimeslot: Timeslot? = null
)
