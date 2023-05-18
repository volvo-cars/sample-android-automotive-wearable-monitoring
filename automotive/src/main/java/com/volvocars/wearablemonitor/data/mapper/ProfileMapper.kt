package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.remote.dto.ProfileDto
import com.volvocars.wearablemonitor.domain.model.Profile

fun ProfileDto.toProfile() = Profile(multiple = multiple)
