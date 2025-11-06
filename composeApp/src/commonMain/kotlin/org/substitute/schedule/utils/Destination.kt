package org.substitute.schedule.utils

import androidx.compose.material3.SnackbarHostState
import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data class Today(val url: String = "") : Destination

    @Serializable
    data class Tomorrow(val url: String = "") : Destination

    @Serializable
    data class Settings(val noCredentials: Boolean) : Destination

    @Serializable
    data class AccountSettings(val noCredentials: Boolean) : Destination

    @Serializable
    object UiSettings

    @Serializable
    object InitialLoadingRoute
}