package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.core.util.Constants
import javax.inject.Inject

class IsUnitMmol @Inject constructor(
    private val getUnit: GetUnit,
) {
    operator fun invoke(): Boolean = getUnit.invoke() == Constants.KEY_MMOL
}
