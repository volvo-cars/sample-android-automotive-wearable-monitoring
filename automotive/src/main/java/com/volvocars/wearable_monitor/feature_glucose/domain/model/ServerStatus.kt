package com.volvocars.wearable_monitor.feature_glucose.domain.model

/**
 * Holds status value from server
 */
data class ServerStatus(
    val apiEnabled: Boolean,
    val name: String,
    val serverTime: String,
    val serverTimeEpoch: Long,
    val settings: Settings,
    val status: String,
    val version: String,
)
