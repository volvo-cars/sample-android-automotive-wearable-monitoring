package com.volvocars.wearablemonitor.core.util

object NotificationConstants {


    // Notification service
    const val GLUCOSE_CRITICAL_NOTIFICATION_ID = "999"
    const val GLUCOSE_CRITICAL_NOTIFICATION_NAME = "com.volvocars.wearablemonitor.glucose_critical"
    const val GLUCOSE_INFO_NOTIFICATION_CHANNEL_ID = "1"
    const val GLUCOSE_INFO_NOTIFICATION_CHANNEL_NAME =
        "com.volvocars.wearablemonitor.show_glucose_values"
    const val KEY_IMPORTANCE_HIGH = "notify_importance_high"
    const val KEY_IMPORTANCE_DEFAULT = "notify_importance_default"
    const val KEY_CONTENT_INTENT = "content_intent"
    const val KEY_FIND_PARK_INTENT = "find_parking_intent"
    const val KEY_FIND_SNACK_INTENT = "find_snack_intent"
    const val KEY_CRITICAL_NOTIFICATION = "notify_critical"
    const val KEY_INFORMATION_NOTIFICATION = "notify_default"

    const val ACTION_REQUIRE_CONFIGURATION = "require_configuration"
    const val ACTION_REQUIRE_NETWORK = "require_network"
    const val ACTION_SHOW_GLUCOSE_VALUES = "show_glucose_values"

    const val GOOGLE_MAPS_FIND_NEAREST_PARKING_URI = "geo:0,0?q=parking"
    const val GOOGLE_MAPS_FIND_NEAREST_CONVENIENCE_STORE_URI = "geo:0,0?q=convenience store"
    const val GOOGLE_MAPS_PACKAGE_NAME = "com.google.android.apps.maps"
}