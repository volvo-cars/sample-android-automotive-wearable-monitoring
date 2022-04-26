package com.volvocars.diabetesmonitor.feature_glucose.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.volvocars.diabetesmonitor.feature_glucose.data.repository.FakeDiabetesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DeleteCachedGlucoseValuesTest {
    private lateinit var deleteCachedGlucoseValues: DeleteCachedGlucoseValues
    private lateinit var fakeDiabetesRepository: FakeDiabetesRepository

    @Before
    fun setUp() {
        fakeDiabetesRepository = FakeDiabetesRepository()
        deleteCachedGlucoseValues = DeleteCachedGlucoseValues(fakeDiabetesRepository)
        fakeDiabetesRepository.insertGlucoseValues(FakeDiabetesRepository.glucoseList)
    }

    @Test
    fun `Delete cached glucose values`() = runTest {
        deleteCachedGlucoseValues.invoke()
        val glucoseValues = fakeDiabetesRepository.fetchCachedGlucoseValues(1).lastOrNull()
        assertThat(glucoseValues).isEmpty()
    }

}