package com.volvocars.wearablemonitor.data.remote

import com.volvocars.wearablemonitor.data.remote.dto.BasalDto
import com.volvocars.wearablemonitor.data.remote.dto.DeviceStatusDto
import com.volvocars.wearablemonitor.data.remote.dto.ExtendedSettingsDto
import com.volvocars.wearablemonitor.data.remote.dto.GlucoseDto
import com.volvocars.wearablemonitor.data.remote.dto.ProfileDto
import com.volvocars.wearablemonitor.data.remote.dto.ServerStatusDto
import com.volvocars.wearablemonitor.data.remote.dto.SettingsDto
import com.volvocars.wearablemonitor.data.remote.dto.ThresholdsDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for NightScoutApi using Ktor MockEngine.
 *
 * This demonstrates how to test API calls without needing a running server.
 * MockEngine allows you to define custom responses for different scenarios.
 */
class NightScoutApiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `getGlucose returns list of glucose entries`() = runBlocking {
        // Given
        val mockGlucoseData = listOf(
            GlucoseDto(
                id = "test1",
                sgv = 120,
                date = System.currentTimeMillis(),
                dateString = "2026-04-04T10:00:00.000Z",
                trend = "4",
                direction = "Flat",
                type = "sgv",
            ),
            GlucoseDto(
                id = "test2",
                sgv = 115,
                date = System.currentTimeMillis() - 300000,
                dateString = "2026-04-04T09:55:00.000Z",
                trend = "3",
                direction = "FortyFiveDown",
                type = "sgv",
            ),
        )

        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/api/v1/entries.json") -> {
                    respond(
                        content = json.encodeToString(mockGlucoseData),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = NightScoutApi(client)

        // When
        val result = api.getGlucose("http://test.com", 2)

        // Then
        assertTrue(result.isSuccess)
        val data = result.getOrNull()!!
        assertEquals(2, data.size)
        assertEquals(120, data[0].sgv)
        assertEquals("Flat", data[0].direction)
        assertEquals(115, data[1].sgv)
        assertEquals("FortyFiveDown", data[1].direction)

        client.close()
    }

    @Test
    fun `getGlucose handles network error`() = runBlocking {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"error": "Internal Server Error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = NightScoutApi(client)

        // When
        val result = api.getGlucose("http://test.com", 10)

        // Then
        assertTrue(result.isFailure)

        client.close()
    }

    @Test
    fun `getStatus returns server configuration`() = runBlocking {
        // Given
        val mockStatus = createMockServerStatus()

        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/api/v1/status.json") -> {
                    respond(
                        content = json.encodeToString(mockStatus),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = NightScoutApi(client)

        // When
        val result = api.getStatus("http://test.com")

        // Then
        assertTrue(result.isSuccess)
        val status = result.getOrNull()!!
        assertEquals("Test NightScout", status.name)
        assertEquals("ok", status.status)
        assertEquals("mg/dl", status.settings.units)
        assertEquals(180, status.settings.thresholds.bgHigh)
        assertEquals(70, status.settings.thresholds.bgLow)

        client.close()
    }

    @Test
    fun `getGlucose with hypoglycemia scenario`() = runBlocking {
        // Given - simulate low glucose readings
        val mockGlucoseData = listOf(
            GlucoseDto(
                id = "hypo1",
                sgv = 65,
                date = System.currentTimeMillis(),
                dateString = "2026-04-04T10:00:00.000Z",
                trend = "2",
                direction = "SingleDown",
                type = "sgv",
            ),
            GlucoseDto(
                id = "hypo2",
                sgv = 60,
                date = System.currentTimeMillis() - 300000,
                dateString = "2026-04-04T09:55:00.000Z",
                trend = "1",
                direction = "DoubleDown",
                type = "sgv",
            ),
        )

        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(mockGlucoseData),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = NightScoutApi(client)

        // When
        val result = api.getGlucose("http://test.com", 2)

        // Then
        assertTrue(result.isSuccess)
        val data = result.getOrNull()!!
        assertEquals(2, data.size)
        assertTrue(data.all { it.sgv < 70 }) // All readings are hypoglycemic
        assertEquals("DoubleDown", data[1].direction) // Rapidly falling

        client.close()
    }

    @Test
    fun `getGlucose with hyperglycemia scenario`() = runBlocking {
        // Given - simulate high glucose readings
        val mockGlucoseData = listOf(
            GlucoseDto(
                id = "hyper1",
                sgv = 250,
                date = System.currentTimeMillis(),
                dateString = "2026-04-04T10:00:00.000Z",
                trend = "6",
                direction = "SingleUp",
                type = "sgv",
            ),
            GlucoseDto(
                id = "hyper2",
                sgv = 240,
                date = System.currentTimeMillis() - 300000,
                dateString = "2026-04-04T09:55:00.000Z",
                trend = "7",
                direction = "DoubleUp",
                type = "sgv",
            ),
        )

        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(mockGlucoseData),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val api = NightScoutApi(client)

        // When
        val result = api.getGlucose("http://test.com", 2)

        // Then
        assertTrue(result.isSuccess)
        val data = result.getOrNull()!!
        assertEquals(2, data.size)
        assertTrue(data.all { it.sgv > 180 }) // All readings are hyperglycemic
        assertEquals("DoubleUp", data[1].direction) // Rapidly rising

        client.close()
    }

    // Helper function to create mock server status
    private fun createMockServerStatus() = ServerStatusDto(
        apiEnabled = true,
        authorized = null,
        boluscalcEnabled = false,
        careportalEnabled = true,
        extendedSettings = ExtendedSettingsDto(
            basal = BasalDto(render = "default"),
            deviceStatus = DeviceStatusDto(advanced = true),
            profile = ProfileDto(multiple = true),
        ),
        name = "Test NightScout",
        serverTime = "2026-04-04T10:00:00.000Z",
        serverTimeEpoch = System.currentTimeMillis(),
        settings = SettingsDto(
            alarmHigh = true,
            alarmHighMins = listOf(30, 60, 90, 120),
            alarmLow = true,
            alarmLowMins = listOf(15, 30, 45, 60),
            alarmPumpBatteryLow = false,
            alarmTimeagoUrgent = true,
            alarmTimeagoUrgentMins = 30,
            alarmTimeagoWarn = true,
            alarmTimeagoWarnMins = 15,
            alarmTypes = listOf("simple"),
            alarmUrgentHigh = true,
            alarmUrgentHighMins = listOf(30, 60, 90, 120),
            alarmUrgentLow = true,
            alarmUrgentLowMins = listOf(15, 30, 45, 60),
            alarmUrgentMins = listOf(30, 60, 90, 120),
            alarmWarnMins = listOf(30, 60, 90, 120),
            authDefaultRoles = "readable",
            baseURL = "http://test.com",
            customTitle = "Test NightScout",
            DEFAULT_FEATURES = listOf("bgnow", "delta", "direction", "timeago"),
            editMode = false,
            enable = listOf("careportal"),
            focusHours = 3,
            heartbeat = 60,
            insecureUseHttp = true,
            language = "en",
            nightMode = false,
            scaleY = "log",
            secureCsp = false,
            secureHstsHeader = false,
            secureHstsHeaderIncludeSubdomains = false,
            secureHstsHeaderPreload = false,
            showForecast = "ar2",
            showPlugins = "bgnow delta direction timeago",
            showRawbg = "never",
            theme = "default",
            thresholds = ThresholdsDto(
                bgHigh = 180,
                bgLow = 70,
                bgTargetBottom = 80,
                bgTargetTop = 160,
            ),
            timeFormat = 24,
            units = "mg/dl",
        ),
        status = "ok",
        version = "14.2.6",
    )
}
