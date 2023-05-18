package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.local.entity.GlucoseEntity
import com.volvocars.wearablemonitor.data.remote.dto.GlucoseDto
import com.volvocars.wearablemonitor.data.util.getDate
import com.volvocars.wearablemonitor.data.util.getUnicode
import com.volvocars.wearablemonitor.data.util.round
import com.volvocars.wearablemonitor.data.util.sgvToUnitMmol
import com.volvocars.wearablemonitor.domain.model.Glucose

fun GlucoseEntity.toGlucose() = Glucose(
    id = id,
    sgv = sgv,
    sgvMmol = round(sgvToUnitMmol(sgv), 1f),
    sgvUnit = sgv.toFloat(),
    date = date,
    dateString = getDate(dateString),
    trend = trend,
    direction = getUnicode(trend),
    type = type
)

fun GlucoseDto.toGlucoseEntity()= GlucoseEntity(
    id = id,
    sgv = sgv,
    date = date,
    dateString = dateString,
    trend = trend,
    direction = direction,
    type = type
)