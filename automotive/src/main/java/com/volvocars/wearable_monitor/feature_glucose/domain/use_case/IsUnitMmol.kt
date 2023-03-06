package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.core.util.Constants
import javax.inject.Inject

class IsUnitMmol @Inject constructor(
    private val getUnit: GetUnit
) {
    operator fun invoke(): Boolean {
        return getUnit.invoke() == Constants.KEY_MMOL
    }
}