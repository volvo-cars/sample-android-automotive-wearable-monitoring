package com.volvocars.diabetesmonitor.feature_glucose.domain.use_case

import com.volvocars.diabetesmonitor.feature_glucose.domain.repository.DiabetesRepository


class ObserveCachedGlucoseValues(private val diabetesRepository: DiabetesRepository) {
    operator fun invoke(counts: Int) = diabetesRepository.fetchCachedGlucoseValues(counts)
}