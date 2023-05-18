package com.volvocars.wearablemonitor.data.remote.dto

import com.volvocars.wearablemonitor.domain.model.Profile
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ProfileDto(
    @SerialName("multiple")
    val multiple: Boolean,
) {
    fun toProfile() = Profile(multiple = multiple)
}
