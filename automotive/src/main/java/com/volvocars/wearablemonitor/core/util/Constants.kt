package com.volvocars.wearablemonitor.core.util

object Constants {
    // Default keys SharedPreferences
    const val THRESHOLD_LOW_DEFAULT_VALUE = 55L
    const val THRESHOLD_HIGH_DEFAULT_VALUE = 260L
    const val THRESHOLD_TARGET_LOW_DEFAULT_VALUE = 80L
    const val THRESHOLD_TARGET_HIGH_DEFAULT_VALUE = 180L
    const val TIME_FORMAT_DEFAULT_VALUE = 24L
    const val DEFAULT_CRITICAL_ALARM_INTERVAL = 5L
    const val GLUCOSE_FETCH_INTERVAL_DEFAULT_VALUE = 5


    // PK for sharedPreferenceStorage
    const val KEY_INSTANCE = "instance_setting"
    const val KEY_TIME_FORMAT = "time_format"
    const val KEY_MMOL = "mmol"
    const val KEY_UNIT = "unit"
    const val KEY_GLUCOSE_FETCH_INTERVAL = "glucose_fetch_interval"
    const val KEY_THRESHOLD_HIGH = "thresholds_high"
    const val KEY_THRESHOLD_TARGET_HIGH = "thresholds_targetHigh"
    const val KEY_THRESHOLD_LOW = "thresholds_low"
    const val KEY_THRESHOLD_TARGET_LOW = "thresholds_targetLow"
    const val KEY_ALARM_LOW = "alarmLow_setting"
    const val KEY_ALARM_NOTIFICATION_INTERVAL = "alarmLow_notification_interval"
    const val KEY_USER_EXISTS = "user_exists"
    const val KEY_NOTIFICATION_ENABLED = "glucose_notification_setting"

    // WorkManger ID
    const val GLUCOSE_FETCH_WORK_ID = "FETCH_GLUCOSE"
}