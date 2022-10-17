package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.DiabetesRepository


class ObserveCachedGlucoseValues(private val diabetesRepository: DiabetesRepository) {
    operator fun invoke(counts: Int) = diabetesRepository.fetchCachedGlucoseValues(counts)
}