package com.volvocars.diabetesmonitor.feature_glucose.data.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.volvocars.diabetesmonitor.core.util.Constants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@HiltAndroidTest
@MediumTest
@RunWith(AndroidJUnit4::class)
class SharedPreferenceStorageTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var sharedPreferenceStorage: SharedPreferenceStorage

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        sharedPreferenceStorage.clear()
    }

    @Test
    fun testBaseUrl() {
        val baseUrl = "volvocars.com"
        sharedPreferenceStorage.setBaseUrl(baseUrl)
        assertThat(sharedPreferenceStorage.getBaseUrl()).isEqualTo(baseUrl)
    }

    @Test
    fun testUnit() {
        val unit = "mmol"
        sharedPreferenceStorage.setUnit(unit)
        assertThat(sharedPreferenceStorage.getUnit()).isEqualTo(unit)
    }

    @Test
    fun testTimeFormat() {
        val timeFormat = 12L
        sharedPreferenceStorage.setTimeFormat(timeFormat)
        assertThat(sharedPreferenceStorage.getTimeFormat()).isEqualTo(timeFormat)
    }

    @Test
    fun testThresholdValue() {
        val (thresholdLow, thresholdHigh, thresholdTargetLow, thresholdTargetHigh) = listOf(
            3L,
            12L,
            5L,
            13L
        )
        sharedPreferenceStorage.setThresholdValue(Constants.KEY_THRESHOLD_LOW, thresholdLow)
        sharedPreferenceStorage.setThresholdValue(Constants.KEY_THRESHOLD_HIGH, thresholdHigh)
        sharedPreferenceStorage.setThresholdValue(
            Constants.KEY_THRESHOLD_TARGET_LOW,
            thresholdTargetLow
        )
        sharedPreferenceStorage.setThresholdValue(
            Constants.KEY_THRESHOLD_TARGET_HIGH,
            thresholdTargetHigh
        )
        assertThat(sharedPreferenceStorage.getThresholdLow()).isEqualTo(thresholdLow)
        assertThat(sharedPreferenceStorage.getThresholdHigh()).isEqualTo(thresholdHigh)
        assertThat(sharedPreferenceStorage.getThresholdTargetLow()).isEqualTo(thresholdTargetLow)
        assertThat(sharedPreferenceStorage.getThresholdTargetHigh()).isEqualTo(thresholdTargetHigh)
    }

    @Test
    fun testUserSignedIn() {
        sharedPreferenceStorage.setUserSignedIn(true)
        assertThat(sharedPreferenceStorage.userSignedIn()).isTrue()
    }

    @Test
    fun testGlucoseNotificationEnabled() {
        sharedPreferenceStorage.setGlucoseNotificationEnabled(false)
        assertThat(sharedPreferenceStorage.getGlucoseNotificationEnabled()).isFalse()
    }

    @Test
    fun testCriticalNotificationEnabled() {
        sharedPreferenceStorage.setGlucoseAlarmLowEnabled(true)
        assertThat(sharedPreferenceStorage.getGlucoseAlarmLowEnabled()).isTrue()
    }

    @Test
    fun testCriticalNotificationInterval() {
        val interval = 23L
        sharedPreferenceStorage.setCriticalNotificationInterval(interval)
        assertThat(sharedPreferenceStorage.getCriticalNotificationInterval()).isEqualTo(interval)
    }

    @Test
    fun testClear() {
        val baseUrl = "volvocars.com"
        sharedPreferenceStorage.setBaseUrl(baseUrl)
        assertThat(sharedPreferenceStorage.getBaseUrl()).isNotEmpty()
        sharedPreferenceStorage.clear()
        assertThat(sharedPreferenceStorage.getBaseUrl()).isEmpty()
    }


}