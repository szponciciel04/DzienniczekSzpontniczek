package io.github.szpontium.api.hebe.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MealMenuEntry(
    @SerialName("Name") val name: String,
    @SerialName("Metadata") val metadata: JsonElement? = null
)

@Serializable
data class MealMenu(
    @SerialName("InnerId") val innerId: Int,
    @SerialName("Id") val id: Int,
    @SerialName("DietName") val dietName: String,
    @SerialName("MealName") val mealName: String,
    @SerialName("Dishes") val dishes: String,
    @SerialName("Metadata") val metadata: JsonElement? = null,
    @SerialName("Entires") val entries: List<MealMenuEntry> = emptyList()
)
