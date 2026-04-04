package com.volvocars.wearablemonitor.data.remote

import com.volvocars.wearablemonitor.data.remote.dto.GlucoseDto
import com.volvocars.wearablemonitor.data.remote.dto.ServerStatusDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class NightScoutApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getGlucose(
        url: String,
        count: Int,
    ): Result<List<GlucoseDto>> = runCatching {
        client.get("$url/api/v1/entries.json") {
            parameter("count", count)
        }.body()
    }

    suspend fun getStatus(url: String): Result<ServerStatusDto> = runCatching {
        client.get("$url/api/v1/status.json").body()
    }

    companion object {
        val TAG = NightScoutApi::class.simpleName
    }
}
