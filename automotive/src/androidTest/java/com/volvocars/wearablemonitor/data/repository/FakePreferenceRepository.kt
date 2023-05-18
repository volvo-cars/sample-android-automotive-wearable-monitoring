package com.volvocars.wearablemonitor.data.repository

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import com.volvocars.wearablemonitor.domain.util.ThresholdUnit

class FakePreferenceRepository : PreferenceRepository {
    override fun getThreshold(unit: ThresholdUnit): Long {
        TODO("Not yet implemented")
    }

    override fun setThresholdValue(thresholdName: String, thresholdValue: Long) {
        TODO("Not yet implemented")
    }

    override fun getGlucoseFetchInterval(): Int {
        TODO("Not yet implemented")
    }

    override fun setGlucoseFetchInterval(interval: Int) {
        TODO("Not yet implemented")
    }

    override fun getBaseUrl(): String {
        TODO("Not yet implemented")
    }

    override fun getTimeFormat(): Long {
        TODO("Not yet implemented")
    }

    override fun setTimeFormat(timeFormat: Long) {
        TODO("Not yet implemented")
    }

    override fun getUnit(): String {
        TODO("Not yet implemented")
    }

    override fun setUnit(unit: String) {
        TODO("Not yet implemented")
    }

    override fun getCriticalNotificationInterval(): Long {
        TODO("Not yet implemented")
    }

    override fun setCriticalNotificationInterval(interval: Long) {
        TODO("Not yet implemented")
    }

    override fun isGlucoseNotificationEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isGlucoseAlarmLowEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isUserSignedIn(): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearData() {
        TODO("Not yet implemented")
    }
}