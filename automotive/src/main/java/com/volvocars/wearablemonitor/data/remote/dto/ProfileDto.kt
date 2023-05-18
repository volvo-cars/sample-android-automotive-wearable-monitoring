package com.volvocars.wearablemonitor.data.remote.dto

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ProfileDto(
    @SerialName("multiple")
    val multiple: Boolean,
)
