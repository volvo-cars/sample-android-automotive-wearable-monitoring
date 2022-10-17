package com.volvocars.wearable_monitor.core.util

object Constants {
    // Default keys SharedPreferences
    const val THRESHOLD_LOW_DEFAULT_VALUE = 55L
    const val THRESHOLD_HIGH_DEFAULT_VALUE = 260L
    const val THRESHOLD_TARGET_LOW_DEFAULT_VALUE = 80L
    const val THRESHOLD_TARGET_HIGH_DEFAULT_VALUE = 180L
    const val TIME_FORMAT_DEFAULT_VALUE = 24L
    const val DEFAULT_CRITICAL_ALARM_INTERVAL = 5L
    const val GLUCOSE_FETCH_INTERVAL_DEFAULT_VALUE = 5

    // Notification service
    const val KEY_CRITICAL_NOTIFICATION_CHANNEL_ID = "com.volvocars.diabetesmonitor.critical_notification"
    const val KEY_CRITICAL_NOTIFICATION_CHANNEL_NAME = "glucose_out_of_range"
    const val KEY_NOTIFICATION_CHANNEL_ID = "com.volvocars.diabetesmonitor.information_notification"
    const val KEY_NOTIFICATION_CHANNEL_NAME = "show_glucose_values"
    const val KEY_IMPORTANCE_HIGH = "notify_importance_high"
    const val KEY_IMPORTANCE_DEFAULT = "notify_importance_default"
    const val KEY_CONTENT_INTENT = "content_intent"
    const val KEY_FIND_PARK_INTENT = "find_parking_intent"
    const val KEY_FIND_SNACK_INTENT = "find_snack_intent"
    const val KEY_CRITICAL_NOTIFICATION = "notify_critical"
    const val KEY_INFORMATION_NOTIFICATION = "notify_default"

    const val SHOW_INFO_NOTIFICATION_ID = 1
    const val SHOW_CRITICAL_NOTIFICATION_ID = 999

    const val ACTION_REQUIRE_CONFIGURATION = "require_configuration"
    const val ACTION_REQUIRE_NETWORK = "require_network"
    const val ACTION_SHOW_GLUCOSE_VALUES = "show_glucose_values"

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

    // Retrofit BaseURL
    const val BASE_URL = "https://volvocars.com"
}