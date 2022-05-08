package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Profile
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileDto(
    val multiple: Boolean,
) : Parcelable {
    fun toProfile(): Profile = Profile(multiple = multiple)
}
