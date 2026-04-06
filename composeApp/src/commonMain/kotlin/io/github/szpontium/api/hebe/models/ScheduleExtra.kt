@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class ScheduleExtraSubstitution(
    @SerialName("Id") val id: Int,
    @SerialName("ClassAbsence") val classAbsence: Boolean,
    @SerialName("DateAt") val date: LocalDate? = null,
    @SerialName("JournalId") val journalId: Int,
    @SerialName("LessonDateAt") val lessonDate: LocalDate? = null,
    @SerialName("NoRoom") val noRoom: Boolean,
    @SerialName("PupilNote") val pupilNote: String? = null,
    @SerialName("Reason") val reason: String? = null,
    @SerialName("Room") val room: Room? = null,
    @SerialName("ScheduleExtraId") val scheduleExtraId: Int,
    @SerialName("TeacherAbsenceEffectName") val teacherAbsenceEffectName: String? = null,
    @SerialName("TeacherAbsenceReasonId") val teacherAbsenceReasonId: Int? = null,
    @SerialName("Teacher") val teacher: Employee? = null,
    @SerialName("TimeEnd") val timeEnd: String? = null,
    @SerialName("TimeStart") val timeStart: String? = null,
    @SerialName("UnitId") val unitId: Int,
    @SerialName("ModifiedAt") val modifiedAt: LocalDateTime
)

@Serializable
data class ScheduleExtra(
    @SerialName("Id") val id: Int,
    @SerialName("ScheduleExtraId") val scheduleExtraId: Int,
    @SerialName("UnitId") val unitId: Int,
    @SerialName("Type") val type: Int,
    @SerialName("Year") val year: Int,
    @SerialName("DateAt") val day: LocalDate,
    @SerialName("ExtraDescription") val extraDescription: String,
    @SerialName("ScheduleDescription") val scheduleDescription: String,
    @SerialName("SchedulePupilDescription") val schedulePupilDescription: String,
    @SerialName("Teacher") val teacher: Employee,
    @SerialName("TimeSlot") val timeSlot: Timeslot,
    @SerialName("Room") val room: Room? = null,
    @SerialName("Substitution") val substitution: ScheduleExtraSubstitution? = null
)
