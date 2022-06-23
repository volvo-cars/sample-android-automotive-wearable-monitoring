package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Profile
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ProfileDto(
    @SerialName("multiple")
    val multiple: Boolean,
) {
    fun toProfile(): Profile = Profile(multiple = multiple)
}
