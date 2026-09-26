package io.github.szpontium.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class CandidateStudent(
    val id: String,
    val pupilFirstName: String,
    val pupilSurname: String,
    val schoolName: String,
    val classDisplay: String,
    val tenant: String,
    val restUrl: String,
    val accountJson: String,
    val credentialJson: String,
    val pLogin: String,
    val pPassword: String,
    val isSelected: Boolean = true
)

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Login : Route

    @Serializable
    data class SelectStudents(val candidates: List<CandidateStudent>) : Route

    @Serializable
    data object Dashboard : Route

    @Serializable
    data object Start : Route

    @Serializable
    data object Grades : Route

    @Serializable
    data object Timetable : Route

    @Serializable
    data object Exams : Route

    @Serializable
    data object Homework : Route

    @Serializable
    data object More : Route

    @Serializable
    data object Notes : Route

    @Serializable
    data object Announcements : Route

    @Serializable
    data object Messages : Route

    @Serializable
    data object Account : Route

    @Serializable
    data class MessageDetails(val id: String, val isHebe: Boolean, val hebeContent: String? = null) : Route
}
