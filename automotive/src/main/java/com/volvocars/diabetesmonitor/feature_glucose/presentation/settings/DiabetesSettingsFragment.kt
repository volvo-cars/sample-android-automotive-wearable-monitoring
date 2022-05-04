package com.volvocars.diabetesmonitor.feature_glucose.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.work.WorkManager
import com.android.car.ui.preference.CarUiEditTextPreference
import com.android.car.ui.preference.CarUiListPreference
import com.android.car.ui.preference.PreferenceFragment
import com.volvocars.diabetesmonitor.feature_glucose.presentation.MainActivity
import com.volvocars.diabetesmonitor.R
import com.volvocars.diabetesmonitor.core.util.Constants
import com.volvocars.diabetesmonitor.core.util.Constants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.diabetesmonitor.core.util.GlucoseUtils
import com.volvocars.diabetesmonitor.feature_glucose.domain.storage.Storage
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.DeleteCachedGlucoseValues
import com.volvocars.diabetesmonitor.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DiabetesSettingsFragment : PreferenceFragment() {

    private lateinit var unitPreference: CarUiListPreference
    private lateinit var timeFormatPreference: ListPreference
    private lateinit var fetchGlucoseIntervalPreference: EditTextPreference
    private lateinit var thresholdTargetLowPreference: EditTextPreference
    private lateinit var thresholdLowPreference: EditTextPreference
    private lateinit var thresholdTargetHighPreference: EditTextPreference
    private lateinit var thresholdHighPreference: EditTextPreference
    private lateinit var criticalNotificationIntervalPreference: EditTextPreference
    private lateinit var glucoseNotificationPreference: CheckBoxPreference
    private lateinit var alarmLowPreference: CheckBoxPreference
    private lateinit var logoutPreference: Preference

    @Inject
    lateinit var glucoseUtils: GlucoseUtils

    @Inject
    lateinit var sharedPreferenceStorage: Storage

    @Inject
    lateinit var deleteCachedGlucoseValues: DeleteCachedGlucoseValues

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // Set text and functionality to logout preference
        logoutPreference = findPreference(getString(R.string.pk_logout_preference))!!
        logoutPreference.summary = getString(
            R.string.you_are_logged_in_with_instance,
            sharedPreferenceStorage.getBaseUrl()
        )
        logoutPreference.setOnPreferenceClickListener {
            logout()

            // Navigate back to login page.
            startActivity(Intent(context, MainActivity::class.java))
            true
        }

        // SummaryProvider for unit/time format preference
        val dropDownPreferenceSummaryProvider =
            Preference.SummaryProvider<CarUiListPreference> { preference ->
                val value = preference.entry?.toString()
                when (!value.isNullOrEmpty()) {
                    true -> value
                    false -> getString(R.string.not_set)
                }
            }

        // SummaryProvider for all edit text preferences
        val editTextPreferenceSummaryProvider =
            Preference.SummaryProvider<EditTextPreference> { preference ->
                val text = preference.text
                if (!text.isNullOrEmpty()) {
                    text
                } else getString(R.string.not_set)
            }


        unitPreference =
            findPreference<CarUiListPreference>(Constants.KEY_UNIT)?.apply {
                summaryProvider = dropDownPreferenceSummaryProvider
                setOnPreferenceChangeListener { _, newValue ->
                    val value: String = newValue?.toString() ?: Constants.KEY_MMOL
                    sharedPreferenceStorage.setUnit(value)
                    true
                }
            }!!
        unitPreference.value = sharedPreferenceStorage.getUnit()

        timeFormatPreference =
            findPreference<ListPreference>(getString(R.string.pk_time_format))?.apply {
                summaryProvider = dropDownPreferenceSummaryProvider
                setOnPreferenceChangeListener { _, newValue ->
                    val value: Long =
                        newValue?.toString()?.toLong() ?: Constants.TIME_FORMAT_DEFAULT_VALUE
                    sharedPreferenceStorage.setTimeFormat(value)
                    true
                }
            }!!
        timeFormatPreference.value = sharedPreferenceStorage.getTimeFormat().toString()

        fetchGlucoseIntervalPreference =
            findPreference<CarUiEditTextPreference>("pk_glucose_fetch_interval")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val value = newValue?.toString()?.toInt() ?: 1
                    sharedPreferenceStorage.setGlucoseFetchInterval(value)
                    true
                }
                setSummaryProvider {
                    if (!text.isNullOrEmpty()) {
                        text
                    } else {
                        "5"
                    }
                }
                setOnPreferenceClickListener {
                    true
                }
            }!!
        fetchGlucoseIntervalPreference.text =
            sharedPreferenceStorage.getGlucoseFetchInterval().toString()

        thresholdLowPreference = initThresholdPreference(getString(R.string.pk_thresholds_low))

        thresholdTargetLowPreference =
            initThresholdPreference(getString(R.string.pk_thresholds_targetLow))

        thresholdTargetHighPreference =
            initThresholdPreference(getString(R.string.pk_thresholds_targetHigh))

        thresholdHighPreference = initThresholdPreference(getString(R.string.pk_thresholds_high))

        criticalNotificationIntervalPreference =
            findPreference(getString(R.string.pk_alarmLow_notification_interval))!!
        criticalNotificationIntervalPreference.summaryProvider = editTextPreferenceSummaryProvider

        glucoseNotificationPreference =
            findPreference(getString(R.string.pk_glucoseNotification_setting))!!
        glucoseNotificationPreference.setOnPreferenceChangeListener { _, newValue ->
            sharedPreferenceStorage.setGlucoseNotificationEnabled(newValue as Boolean)
            true
        }

        alarmLowPreference = findPreference(getString(R.string.pk_alarmLow_setting))!!
        alarmLowPreference.setOnPreferenceChangeListener { _, newValue ->
            sharedPreferenceStorage.setGlucoseAlarmLowEnabled(newValue as Boolean)
            true
        }

        updatePreferences()
    }

    /**
     * Init ThresholdPreferences
     * @param key The preference key of the preference you want to change
     */
    private fun initThresholdPreference(
        key: String,
    ): EditTextPreference {
        return findPreference<EditTextPreference>(key)?.apply {
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
                val text = preference.text
                if (!text.isNullOrEmpty()) {
                    glucoseUtils.sgvToUnitText(text)
                } else getString(R.string.not_set)
            }
            setOnPreferenceChangeListener { preference, newValue ->
                val value: Float = newValue?.toString()?.toFloat() ?: 0.0f
                sharedPreferenceStorage.setThresholdValue(
                    preference.key,
                    glucoseUtils.unitToSvg(value).toLong()
                )
                true
            }
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }!!
    }

    /**
     *  Update preference text values
     */
    private fun updatePreferences() {
        thresholdTargetHighPreference.text =
            sharedPreferenceStorage.getThresholdTargetHigh().toString()
        thresholdTargetLowPreference.text =
            sharedPreferenceStorage.getThresholdTargetLow().toString()
        thresholdHighPreference.text = sharedPreferenceStorage.getThresholdHigh().toString()
        thresholdLowPreference.text = sharedPreferenceStorage.getThresholdLow().toString()
        criticalNotificationIntervalPreference.text =
            sharedPreferenceStorage.getCriticalNotificationInterval().toString()
    }

    /**
     * Send intent to notification service
     *
     * @param action What kind of action to be sent to the service
     * @param state If the provided action should be started or stopped
     */
    private fun sendCommandToService(action: String, state: Boolean) {
        Intent(requireContext(), NotificationService::class.java).also {
            it.action = action
            if (state) requireContext().startService(it) else requireContext().stopService(it)
        }
    }

    /**
     * We need to be sure that we clear all sensitive data in the room database
     * and also in the shared preference storage.
     *
     * We also need to stop the glucose fetcher in WorkManager.
     */
    private fun logout() {
        // Cancel glucose fetcher work.
        WorkManager.getInstance(requireContext()).cancelUniqueWork(Constants.GLUCOSE_FETCH_WORK_ID)

        // Stop to the glucose notification
        sendCommandToService(ACTION_SHOW_GLUCOSE_VALUES, false)

        // Clear the preference storage.
        sharedPreferenceStorage.clear()

        // Clear all data cached in room database.
        CoroutineScope(Dispatchers.IO).launch {
            deleteCachedGlucoseValues.invoke()
        }

    }

    companion object {
        val TAG = DiabetesSettingsFragment::class.simpleName
    }
}