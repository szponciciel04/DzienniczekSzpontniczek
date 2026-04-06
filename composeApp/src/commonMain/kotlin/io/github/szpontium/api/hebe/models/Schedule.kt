@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class ScheduleChange(
    @SerialName("Id") val id: Int,
    @SerialName("Type") val type: Int,
    @SerialName("IsMerge") val isMerge: Boolean,
    @SerialName("Separation") val separation: Boolean
)

@Serializable
data class ScheduleSubstitution(
    @SerialName("Id") val id: Int,
    @SerialName("UnitId") val unitId: Int,
    @SerialName("ScheduleId") val scheduleId: Int,
    @SerialName("DateAt") val date: LocalDate,
    @SerialName("ChangeDateAt") val changeDate: LocalDate? = null,
    @SerialName("PupilNote") val pupilNote: String? = null,
    @SerialName("Reason") val reason: String? = null,
    @SerialName("Event") val event: String? = null,
    @SerialName("Room") val room: Room? = null,
    @SerialName("TimeSlot") val timeSlot: Timeslot? = null,
    @SerialName("Subject") val subject: Subject? = null,
    @SerialName("TeacherPrimary") val teacherPrimary: Employee? = null,
    @SerialName("TeacherAbsenceReasonId") val teacherAbsenceReasonId: Int? = null,
    @SerialName("TeacherAbsenceEffectName") val teacherAbsenceEffectName: String? = null,
    @SerialName("TeacherSecondary") val teacherSecondary: Employee? = null,
    @SerialName("TeacherSecondaryAbsenceReasonId") val teacherSecondaryAbsenceReasonId: Int? = null,
    @SerialName("TeacherSecondaryAbsenceEffectName") val teacherSecondaryAbsenceEffectName: String? = null,
    @SerialName("TeacherSecondary2") val teacherSecondary2: Employee? = null,
    @SerialName("TeacherSecondary2AbsenceReasonId") val teacherSecondary2AbsenceReasonId: Int? = null,
    @SerialName("TeacherSecondary2AbsenceEffectName") val teacherSecondary2AbsenceEffectName: String? = null,
    @SerialName("Change") val change: ScheduleChange? = null,
    @SerialName("Clazz") val clazz: Clazz? = null,
    @SerialName("Distribution") val distribution: Distribution? = null,
    @SerialName("ClassAbsence") val classAbsence: Boolean,
    @SerialName("NoRoom") val noRoom: Boolean,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime,
    @SerialName("Description") val description: String? = null
)

@Serializable
data class Schedule(
    @SerialName("Id") val id: Int,
    @SerialName("MergeChangeId") val mergeChangeId: Int? = null,
    @SerialName("Event") val event: String? = null,
    @SerialName("DateAt") val date: LocalDate,
    @SerialName("Room") val room: Room? = null,
    @SerialName("TimeSlot") val timeSlot: Timeslot,
    @SerialName("Subject") val subject: Subject? = null,
    @SerialName("TeacherPrimary") val teacherPrimary: Employee? = null,
    @SerialName("TeacherSecondary") val teacherSecondary: Employee? = null,
    @SerialName("TeacherSecondary2") val teacherSecondary2: Employee? = null,
    @SerialName("Clazz") val clazz: Clazz,
    @SerialName("Distribution") val distribution: Distribution? = null,
    @SerialName("PupilAlias") val pupilAlias: String? = null,
    @SerialName("Substitution") val substitution: ScheduleSubstitution? = null,
    @SerialName("Parent") val parent: String? = null
)
