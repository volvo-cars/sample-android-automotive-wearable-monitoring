package com.volvocars.wearable_monitor.feature_glucose.data.storage

import android.content.SharedPreferences
import com.volvocars.wearable_monitor.core.util.Constants.DEFAULT_CRITICAL_ALARM_INTERVAL
import com.volvocars.wearable_monitor.core.util.Constants.GLUCOSE_FETCH_INTERVAL_DEFAULT_VALUE
import com.volvocars.wearable_monitor.core.util.Constants.KEY_ALARM_LOW
import com.volvocars.wearable_monitor.core.util.Constants.KEY_ALARM_NOTIFICATION_INTERVAL
import com.volvocars.wearable_monitor.core.util.Constants.KEY_GLUCOSE_FETCH_INTERVAL
import com.volvocars.wearable_monitor.core.util.Constants.KEY_INSTANCE
import com.volvocars.wearable_monitor.core.util.Constants.KEY_MMOL
import com.volvocars.wearable_monitor.core.util.Constants.KEY_NOTIFICATION_ENABLED
import com.volvocars.wearable_monitor.core.util.Constants.KEY_THRESHOLD_HIGH
import com.volvocars.wearable_monitor.core.util.Constants.KEY_THRESHOLD_LOW
import com.volvocars.wearable_monitor.core.util.Constants.KEY_THRESHOLD_TARGET_HIGH
import com.volvocars.wearable_monitor.core.util.Constants.KEY_THRESHOLD_TARGET_LOW
import com.volvocars.wearable_monitor.core.util.Constants.KEY_TIME_FORMAT
import com.volvocars.wearable_monitor.core.util.Constants.KEY_UNIT
import com.volvocars.wearable_monitor.core.util.Constants.KEY_USER_EXISTS
import com.volvocars.wearable_monitor.core.util.Constants.THRESHOLD_HIGH_DEFAULT_VALUE
import com.volvocars.wearable_monitor.core.util.Constants.THRESHOLD_LOW_DEFAULT_VALUE
import com.volvocars.wearable_monitor.core.util.Constants.THRESHOLD_TARGET_HIGH_DEFAULT_VALUE
import com.volvocars.wearable_monitor.core.util.Constants.THRESHOLD_TARGET_LOW_DEFAULT_VALUE
import com.volvocars.wearable_monitor.core.util.Constants.TIME_FORMAT_DEFAULT_VALUE
import com.volvocars.wearable_monitor.core.util.get
import com.volvocars.wearable_monitor.core.util.put
import com.volvocars.wearable_monitor.feature_glucose.domain.model.ServerStatus
import com.volvocars.wearable_monitor.feature_glucose.domain.storage.Storage
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Implements functions from [Storage]
 * Handle the communication with SharedPreferences
 */
class SharedPreferenceStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : Storage {

    override fun setPreferenceFromServerStatus(serverStatus: ServerStatus) {
        setUnit(serverStatus.settings.units)
        setTimeFormat(serverStatus.settings.timeFormat)
        val (bgHigh, bgLow, bgTargetBottom, bgTargetTop) = serverStatus.settings.thresholds
        setThresholdLow(bgLow)
        setThresholdTargetLow(bgTargetBottom)
        setThresholdTargetHigh(bgTargetTop)
        setThresholdHigh(bgHigh)
        setCriticalNotificationInterval(DEFAULT_CRITICAL_ALARM_INTERVAL)
        setGlucoseNotificationEnabled(true)
    }

    /**
     * Get unit value
     * @return value of the unit
     */
    override fun getUnit(): String {
        return sharedPreferences.get(KEY_UNIT, KEY_MMOL)
    }

    /**
     * Set unit value
     * @param value Unit value
     */
    override fun setUnit(value: String) {
        sharedPreferences.put(KEY_UNIT, value)
    }

    /**
     * Get time format value from shared preference
     * @return time format set value
     */
    override fun getTimeFormat(): Long {
        return sharedPreferences.get(KEY_TIME_FORMAT, TIME_FORMAT_DEFAULT_VALUE)
    }

    override fun setGlucoseFetchInterval(value: Int) {
        sharedPreferences.put(KEY_GLUCOSE_FETCH_INTERVAL, value)
    }

    override fun getGlucoseFetchInterval(): Int {
        return sharedPreferences.get(
            KEY_GLUCOSE_FETCH_INTERVAL,
            GLUCOSE_FETCH_INTERVAL_DEFAULT_VALUE
        )
    }

    /**
     * Set time format in shared preference
     * @param value time format set value
     */
    override fun setTimeFormat(value: Long) {
        sharedPreferences.put(KEY_TIME_FORMAT, value)
    }

    /**
     * Set base URL of instance to storage
     * @param value Url to be stored
     */
    override fun setBaseUrl(value: String) {
        sharedPreferences.put(KEY_INSTANCE, value)
    }

    /**
     * Return the base url from storage
     * @return url from shared preferences
     */
    override fun getBaseUrl(): String {
        return sharedPreferences.get(KEY_INSTANCE, "")
    }

    /**
     * Set the threshold values
     * @param threshold Which threshold to set
     * @param value threshold set value
     */
    override fun setThresholdValue(threshold: String, value: Long) {
        when (threshold) {
            KEY_THRESHOLD_LOW -> setThresholdLow(value)
            KEY_THRESHOLD_TARGET_LOW -> setThresholdTargetLow(value)
            KEY_THRESHOLD_HIGH -> setThresholdHigh(value)
            KEY_THRESHOLD_TARGET_HIGH -> setThresholdTargetHigh(value)
        }
    }

    /**
     * Get the threshold low value from shared preferences
     * @return stored threshold low value - if no value is stored 55 is default value
     */
    override fun getThresholdLow(): Long {
        return sharedPreferences.get(KEY_THRESHOLD_LOW, THRESHOLD_LOW_DEFAULT_VALUE)
    }

    /**
     * Set threshold low value in shared preference
     * @param value Threshold low set value
     */
    override fun setThresholdLow(value: Long) {
        sharedPreferences.put(KEY_THRESHOLD_LOW, value)
    }

    /**
     * Get the threshold high value from shared preferences
     * @return stored threshold high value - if no value is stored 260 is default value
     */
    override fun getThresholdHigh(): Long {
        return sharedPreferences.get(KEY_THRESHOLD_HIGH, THRESHOLD_HIGH_DEFAULT_VALUE)
    }

    /**
     * Set threshold high value in shared preference
     * @param value Threshold high set value
     */
    override fun setThresholdHigh(value: Long) {
        sharedPreferences.put(KEY_THRESHOLD_HIGH, value)
    }

    /**
     * Get the threshold target low value from shared preferences
     * @return stored threshold target low value - if note value is stored 80 is default value
     */
    override fun getThresholdTargetLow(): Long {
        return sharedPreferences.get(KEY_THRESHOLD_TARGET_LOW, THRESHOLD_TARGET_LOW_DEFAULT_VALUE)
    }

    /**
     * Set threshold target low value in shared preferences
     * @param value target low set value
     */
    override fun setThresholdTargetLow(value: Long) {
        sharedPreferences.put(KEY_THRESHOLD_TARGET_LOW, value)
    }

    /**
     * Get the threshold target high value from shared preferences
     * @return stored threshold target high value - if no value is stored 180 is default value
     */
    override fun getThresholdTargetHigh(): Long {
        return sharedPreferences.get(KEY_THRESHOLD_TARGET_HIGH, THRESHOLD_TARGET_HIGH_DEFAULT_VALUE)
    }

    /**
     * Set the threshold target high value
     * @param value target high set value
     */
    override fun setThresholdTargetHigh(value: Long) {
        sharedPreferences.put(KEY_THRESHOLD_TARGET_HIGH, value)
    }

    /**
     * Find out if a user is already signed in or not
     * @return true if user is signed otherwise false
     */
    override fun userSignedIn(): Boolean =
        sharedPreferences.get(KEY_USER_EXISTS, false)

    /**
     * Save to shared preferences if user is signed in or not
     * @param value true or false
     */
    override fun setUserSignedIn(value: Boolean) {
        sharedPreferences.put(KEY_USER_EXISTS, value)
    }

    /**
     * Get shared preference if user have notification enabled
     * @return true if notification is enabled otherwise false
     */
    override fun getGlucoseNotificationEnabled(): Boolean {
        return sharedPreferences.get(KEY_NOTIFICATION_ENABLED, true)
    }

    /**
     * Enable/disable glucose notification
     * @param value set to true to enable notification, false to disable them
     */
    override fun setGlucoseNotificationEnabled(value: Boolean) {
        sharedPreferences.put(KEY_NOTIFICATION_ENABLED, value)
    }

    /**
     *  Get shared preference if user have enabled notification of alarm low
     *  @return true if alarm low notification is enabled otherwise false
     */
    override fun getGlucoseAlarmLowEnabled(): Boolean {
        return sharedPreferences.get(KEY_ALARM_LOW, true)
    }

    /**
     * Enable/disable heads-up notification when glucose value is going outside the threshold
     * @param value set to true to enable notification, false to disable them
     */
    override fun setGlucoseAlarmLowEnabled(value: Boolean) {
        return sharedPreferences.put(KEY_ALARM_LOW, value)
    }

    /**
     * Get the critical notification interval in millis stored in shared preference
     * @return interval as millis
     */
    override fun getCriticalNotificationIntervalMillis(): Long {
        return TimeUnit.MINUTES.toMillis(
            sharedPreferences.get(
                KEY_ALARM_NOTIFICATION_INTERVAL,
                DEFAULT_CRITICAL_ALARM_INTERVAL
            )
        )
    }

    /**
     * Set interval of how often the critical notification should show up
     * @param value interval in ms
     */
    override fun setCriticalNotificationInterval(value: Long) {
        sharedPreferences.put(KEY_ALARM_NOTIFICATION_INTERVAL, value)
    }

    /**
     * Get the critical notification interval value stored in shared preference
     * @return interval
     */
    override fun getCriticalNotificationInterval(): Long {
        return sharedPreferences.get(
            KEY_ALARM_NOTIFICATION_INTERVAL,
            DEFAULT_CRITICAL_ALARM_INTERVAL
        )
    }

    /**
     * Clear all data from shared preferences
     */
    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        val TAG = SharedPreferenceStorage::class.simpleName
    }
}