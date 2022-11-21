package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.volvocars.wearable_monitor.feature_glucose.data.repository.FakeDiabetesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FetchGlucoseValuesTest {
    private lateinit var fetchGlucoseValues: FetchGlucoseValues
    private lateinit var fakeDiabetesRepository: FakeDiabetesRepository

    @Before
    fun setUp() {
        fakeDiabetesRepository = FakeDiabetesRepository()
        fetchGlucoseValues = FetchGlucoseValues(fakeDiabetesRepository)
        fakeDiabetesRepository.insertGlucoseValues(FakeDiabetesRepository.glucoseList)
    }

    @Test
    fun `URL input empty, return empty flow`() = runBlocking {
        val glucoseValues = fetchGlucoseValues.invoke("", 0).lastOrNull()
        assertThat(glucoseValues).isNull()
    }

    @Test
    fun `counts is 0, return empty flow`() = runBlocking {
        val glucoseValues = fetchGlucoseValues.invoke("https://volvocars.com", 0).lastOrNull()
        assertThat(glucoseValues).isNull()
    }

    @Test
    fun `URL not valid, return empty flow`() = runBlocking {
        val glucoseValues = fetchGlucoseValues.invoke("http://volvocars.com", 12).lastOrNull()
        assertThat(glucoseValues).isNull()
    }

    @Test
    fun `URL valid, return a flow of GlucoseValues`() = runBlocking {
        val response = fetchGlucoseValues
            .invoke("https://volvocars.com", 12)
            .toList()

        val glucoseValues = response[0].getOrThrow()
        for (i in 0..glucoseValues.size.minus(2)) {
            assertThat(glucoseValues[i].id).isLessThan(glucoseValues[i + 1].id)
        }
    }

    @Test
    fun `Could not fetch data, return Resource Error`() = runBlocking {
        fakeDiabetesRepository.shouldReturnError(true)

        val response = fetchGlucoseValues.invoke("https://volvocars.com", 12).first()
        assert(response.isFailure)
    }
}