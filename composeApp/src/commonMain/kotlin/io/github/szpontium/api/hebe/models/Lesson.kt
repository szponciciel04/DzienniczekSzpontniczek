@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement

@Serializable
data class Lesson(
    @SerialName("LessonId") val lessonId: Int,
    @SerialName("PresenceType") val presenceType: PresenceType? = null,
    @SerialName("Collection") val collection: List<JsonElement> = emptyList(),
    @SerialName("JustificationStatus") val justificationStatus: Int? = null,
    @SerialName("Id") val id: Int,
    @SerialName("LessonClassId") val lessonClassId: Int,
    @SerialName("DayAt") val day: LocalDate,
    @SerialName("CalculatePresence") val calculatePresence: Boolean,
    @SerialName("GroupDefinition") val groupDefinition: String? = null,
    @SerialName("PublicResources") val publicResources: String? = null,
    @SerialName("RemoteResources") val remoteResources: String? = null,
    @SerialName("Replacement") val replacement: Boolean,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime,
    @SerialName("GlobalKey") val globalKey: String,
    @SerialName("Note") val note: String? = null,
    @SerialName("Topic") val topic: String? = null,
    @SerialName("LessonNumber") val lessonNumber: Int? = null,
    @SerialName("LessonClassGlobalKey") val lessonClassGlobalKey: String,
    @SerialName("TimeSlot") val timeSlot: Timeslot,
    @SerialName("Subject") val subject: Subject? = null,
    @SerialName("TeacherPrimary") val teacherPrimary: Employee,
    @SerialName("TeacherSecondary") val teacherSecondary: Employee? = null,
    @SerialName("TeacherMod") val teacherMod: Employee,
    @SerialName("Clazz") val clazz: Clazz,
    @SerialName("Distribution") val distribution: Distribution? = null,
    @SerialName("Didactics") val didactics: JsonElement? = null
)
