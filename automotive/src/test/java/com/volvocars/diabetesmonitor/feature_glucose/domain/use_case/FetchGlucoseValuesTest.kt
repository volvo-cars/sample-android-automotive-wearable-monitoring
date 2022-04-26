package com.volvocars.diabetesmonitor.feature_glucose.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.volvocars.diabetesmonitor.feature_glucose.data.repository.FakeDiabetesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
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
    fun `URL input empty, return empty flow`() = runTest {
        val glucoseValues = fetchGlucoseValues.invoke("", 0).lastOrNull()
        assertThat(glucoseValues).isNull()
    }

    @Test
    fun `counts is 0, return empty flow`() = runTest {
        val glucoseValues = fetchGlucoseValues.invoke("https://volvocars.com", 0).lastOrNull()
        assertThat(glucoseValues).isNull()
    }

    @Test
    fun `URL not valid, return empty flow`() = runTest {
        val glucoseValues = fetchGlucoseValues.invoke("http://volvocars.com", 12).lastOrNull()
        assertThat(glucoseValues).isNull()
    }

    @Test
    fun `URL valid, return a flow of GlucoseValues`() = runTest {
        val response =
            fetchGlucoseValues.invoke("https://volvocars.com", 12).toList()

        assertThat(response[0].data).isNull()

        val glucoseValues = response[1].data!!
        for (i in 0..glucoseValues.size.minus(2)) {
            assertThat(glucoseValues[i].id).isLessThan(glucoseValues[i + 1].id)
        }
    }

    @Test
    fun `Could not fetch data, return Resource Error`() = runTest {
        fakeDiabetesRepository.shouldReturnError(true)

        val response = fetchGlucoseValues.invoke("https://volvocars.com", 12).toList()
        assertThat(response[0].data).isNull()
        assertThat(response[1].message).isEqualTo("ERROR")

        fakeDiabetesRepository.shouldReturnError(false)
    }
}