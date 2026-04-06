package io.github.szpontium.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Login : Route

    @Serializable
    data object Dashboard : Route
}
