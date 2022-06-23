package com.volvocars.wearable_monitor.feature_glucose.data.remote

import com.volvocars.wearable_monitor.feature_glucose.data.remote.dto.GlucoseDto
import com.volvocars.wearable_monitor.feature_glucose.data.remote.dto.ServerStatusDto
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject

class NightScoutApi @Inject constructor(private val client: HttpClient) {
    suspend fun getGlucose(
        url: String,
        count: Int,
    ): List<GlucoseDto> = client.get("$url/api/v1/entries.json") {
        parameter("count", count)
    }

    suspend fun getStatus(url: String): ServerStatusDto = client.get("$url/api/v1/status.json")
}