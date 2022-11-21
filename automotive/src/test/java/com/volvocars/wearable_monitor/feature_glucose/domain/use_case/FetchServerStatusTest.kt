package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.volvocars.wearable_monitor.feature_glucose.data.repository.FakeDiabetesRepository
import com.volvocars.wearable_monitor.feature_glucose.domain.model.ServerStatus
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Settings
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Thresholds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FetchServerStatusTest {
    private lateinit var fetchServerStatus: FetchServerStatus
    private lateinit var fakeDiabetesRepository: FakeDiabetesRepository

    @Before
    fun setUp() {
        fakeDiabetesRepository = FakeDiabetesRepository()
        fetchServerStatus = FetchServerStatus(fakeDiabetesRepository)

        val minutesValues = listOf<Long>(30, 60, 90, 120)

        val serverStatus = ServerStatus(
            apiEnabled = true,
            name = "",
            serverTime = "2021-12-15T07:52:41.897Z",
            serverTimeEpoch = 1639554761897,
            settings = Settings(
                units = "mmol",
                baseURL = "",
                alarmUrgentHighMins = minutesValues,
                alarmHigh = false,
                alarmHighMins = minutesValues,
                alarmLow = false,
                alarmLowMins = minutesValues,
                alarmUrgentLow = false,
                alarmUrgentLowMins = listOf(15, 30, 45),
                alarmUrgentMins = minutesValues,
                alarmTimeagoWarn = false,
                alarmTimeagoWarnMins = 15,
                alarmTimeagoUrgent = false,
                alarmTimeagoUrgentMins = 30,
                alarmPumpBatteryLow = false,
                alarmUrgentHigh = false,
                alarmWarnMins = listOf(15),
                thresholds = Thresholds(
                    bgHigh = 280,
                    bgTargetTop = 160,
                    bgTargetBottom = 90,
                    bgLow = 45,
                ),
                timeFormat = 24
            ),
            status = "ok",
            version = "13.0.0"
        )

        fakeDiabetesRepository.insertServerStatus(serverStatus)
    }

    @Test
    fun `URL input is empty, return empty flow`() = runBlocking {
        val serverStatus = fetchServerStatus("").lastOrNull()
        assertThat(serverStatus).isNull()
    }

    @Test
    fun `URL does not contain https, return empty flow`() = runBlocking {
        val serverStatus = fetchServerStatus("http://volovars.com").lastOrNull()
        assertThat(serverStatus).isNull()
    }

    @Test
    fun `URL valid, return a flow of ServerStatus`() = runBlocking {
        val response = fetchServerStatus("https://volvocars.com").toList()

        // The second object should be of Resource.Success and does contain data
        val serverStatus = response[0].getOrThrow()
        assertThat(serverStatus.version).isEqualTo("13.0.0")
    }

    @Test
    fun `Could not fetch data, return Resource Error`() = runBlocking {
        fakeDiabetesRepository.shouldReturnError(true)

        val response = fetchServerStatus("https://volvocars.com").first()
        assert(response.isFailure)
    }
}