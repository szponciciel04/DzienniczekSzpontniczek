package io.github.szpontium.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Login : Route

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
    data object Account : Route
}
