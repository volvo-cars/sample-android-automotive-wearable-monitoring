package com.volvocars.wearablemonitor.domain.model

/**
 * Holds status value from server
 */
data class ServerStatus(
    val apiEnabled: Boolean,
    val name: String,
    val serverTime: String,
    val serverTimeEpoch: Long,
    val settings: com.volvocars.wearablemonitor.domain.model.Settings,
    val status: String,
    val version: String,
)
