package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Timeslot(
    @SerialName("Id") val id: Int,
    @SerialName("Start") val start: LocalTime,
    @SerialName("End") val end: LocalTime,
    @SerialName("Display") val display: String,
    @SerialName("Position") val position: Int
)
