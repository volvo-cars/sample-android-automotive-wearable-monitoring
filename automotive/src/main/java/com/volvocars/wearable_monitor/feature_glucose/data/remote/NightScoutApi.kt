package com.volvocars.wearable_monitor.feature_glucose.data.remote

import com.volvocars.wearable_monitor.feature_glucose.data.remote.dto.GlucoseDto
import com.volvocars.wearable_monitor.feature_glucose.data.remote.dto.ServerStatusDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * API calls to NightScout
 */
interface NightScoutApi {
    @GET
    @Headers("Accept: application/json")
    suspend fun getGlucose(
        @Url url: String,
        @Query("count") count: Int,
    ): Response<List<GlucoseDto>>

    @GET
    @Headers("Accept: application/json")
    suspend fun getStatus(
        @Url url: String,
    ): Response<ServerStatusDto>
}
