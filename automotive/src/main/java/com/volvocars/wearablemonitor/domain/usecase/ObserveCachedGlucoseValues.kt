package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import javax.inject.Inject


class ObserveCachedGlucoseValues @Inject constructor(
    private val diabetesRepository: DiabetesRepository
) {
    operator fun invoke(counts: Int) = diabetesRepository.fetchCachedGlucoseValues(counts)
}