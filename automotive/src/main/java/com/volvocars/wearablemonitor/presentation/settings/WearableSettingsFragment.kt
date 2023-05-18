package com.volvocars.wearablemonitor.presentation.settings

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
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.domain.usecase.ClearPreferenceStorage
import com.volvocars.wearablemonitor.domain.usecase.DeleteCachedGlucoseValues
import com.volvocars.wearablemonitor.domain.usecase.GetBaseUrl
import com.volvocars.wearablemonitor.domain.usecase.GetCriticalNotificationInterval
import com.volvocars.wearablemonitor.domain.usecase.GetGlucoseFetchInterval
import com.volvocars.wearablemonitor.domain.usecase.GetThresholds
import com.volvocars.wearablemonitor.domain.usecase.GetTimeFormat
import com.volvocars.wearablemonitor.domain.usecase.GetUnit
import com.volvocars.wearablemonitor.domain.usecase.IsUnitMmol
import com.volvocars.wearablemonitor.domain.usecase.SetCriticalNotificationInterval
import com.volvocars.wearablemonitor.domain.usecase.SetGlucoseAlarmEnabled
import com.volvocars.wearablemonitor.domain.usecase.SetGlucoseFetchInterval
import com.volvocars.wearablemonitor.domain.usecase.SetThresholdValue
import com.volvocars.wearablemonitor.domain.usecase.SetTimeFormat
import com.volvocars.wearablemonitor.domain.usecase.SetUnit
import com.volvocars.wearablemonitor.presentation.OpenSourceLicensesDialogFragment
import com.volvocars.wearablemonitor.presentation.util.oneDecimalPrecision
import com.volvocars.wearablemonitor.presentation.util.sgvToUnit
import com.volvocars.wearablemonitor.presentation.util.unitToSvg
import com.volvocars.wearablemonitor.core.service.WearableMonitorService
import com.volvocars.wearablemonitor.core.util.Constants
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_SHOW_GLUCOSE_VALUES
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WearableSettingsFragment : PreferenceFragment() {

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
    private lateinit var openSourceLicensesPreference: Preference

    @Inject
    lateinit var isUnitMmol: IsUnitMmol

    @Inject
    lateinit var setUnit: SetUnit

    @Inject
    lateinit var getUnit: GetUnit

    @Inject
    lateinit var getBaseUrl: GetBaseUrl

    @Inject
    lateinit var setTimeFormat: SetTimeFormat

    @Inject
    lateinit var getTimeFormat: GetTimeFormat

    @Inject
    lateinit var setGlucoseFetchInterval: SetGlucoseFetchInterval

    @Inject
    lateinit var getGlucoseFetchInterval: GetGlucoseFetchInterval

    @Inject
    lateinit var getCriticalNotificationInterval: GetCriticalNotificationInterval

    @Inject
    lateinit var deleteCachedGlucoseValues: DeleteCachedGlucoseValues

    @Inject
    lateinit var setCriticalNotificationInterval: SetCriticalNotificationInterval

    @Inject
    lateinit var setGlucoseAlarmLowEnabled: SetGlucoseAlarmEnabled

    @Inject
    lateinit var setThresholdValue: SetThresholdValue

    @Inject
    lateinit var getThresholds: GetThresholds

    @Inject
    lateinit var clearPreferenceStorage: ClearPreferenceStorage

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // Set text and functionality to logout preference
        logoutPreference = findPreference(getString(R.string.pk_logout_preference))!!
        logoutPreference.summary = getString(
            R.string.you_are_logged_in_with_instance,
            getBaseUrl()
        )
        logoutPreference.setOnPreferenceClickListener {
            logout()

            // Navigate back to login page
            activity?.finish()

            true
        }

        openSourceLicensesPreference = findPreference(getString(R.string.pk_oss_licenses))!!
        openSourceLicensesPreference.apply {
            setOnPreferenceClickListener {
                OpenSourceLicensesDialogFragment().showLicenses(requireActivity())
                true
            }
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
                    setUnit(value)
                    true
                }
            }!!
        unitPreference.value = getUnit()

        timeFormatPreference = findPreference<ListPreference>(
            getString(R.string.pk_time_format)
        )?.apply {
            summaryProvider = dropDownPreferenceSummaryProvider
            setOnPreferenceChangeListener { _, newValue ->
                val value = newValue
                    ?.toString()
                    ?.toLong()
                    ?: Constants.TIME_FORMAT_DEFAULT_VALUE

                setTimeFormat(value)
                true
            }
        }!!
        timeFormatPreference.value = getTimeFormat().toString()

        fetchGlucoseIntervalPreference = findPreference<CarUiEditTextPreference>(
            "pk_glucose_fetch_interval"
        )?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                val value = newValue?.toString()?.toInt() ?: 1
                setGlucoseFetchInterval(value)
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
        fetchGlucoseIntervalPreference.text = getGlucoseFetchInterval().toString()

        thresholdLowPreference = initThresholdPreference(
            getString(R.string.pk_thresholds_low)
        )

        thresholdTargetLowPreference = initThresholdPreference(
            getString(R.string.pk_thresholds_targetLow)
        )

        thresholdTargetHighPreference = initThresholdPreference(
            getString(R.string.pk_thresholds_targetHigh)
        )

        thresholdHighPreference = initThresholdPreference(getString(R.string.pk_thresholds_high))

        criticalNotificationIntervalPreference = findPreference(
            getString(R.string.pk_alarmLow_notification_interval)
        )!!
        criticalNotificationIntervalPreference.setOnPreferenceChangeListener { _, newValue ->
            setCriticalNotificationInterval(newValue.toString().toLong())
            true
        }
        criticalNotificationIntervalPreference.summaryProvider = editTextPreferenceSummaryProvider

        glucoseNotificationPreference = findPreference(
            getString(R.string.pk_glucoseNotification_setting)
        )!!

        alarmLowPreference = findPreference(getString(R.string.pk_alarmLow_setting))!!
        alarmLowPreference.setOnPreferenceChangeListener { _, newValue ->
            setGlucoseAlarmLowEnabled(newValue as Boolean)
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
                    sgvToUnit(text.toInt(), isUnitMmol()).oneDecimalPrecision()
                } else getString(R.string.not_set)
            }
            setOnPreferenceChangeListener { preference, newValue ->
                val value: Float = newValue?.toString()?.toFloat() ?: 0.0f
                setThresholdValue(
                    preference.key,
                    unitToSvg(value, isUnitMmol()).toLong()
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
        val thresholds = getThresholds()
        thresholdTargetHighPreference.text = thresholds.bgTargetTop.toString()
        thresholdTargetLowPreference.text = thresholds.bgTargetBottom.toString()
        thresholdHighPreference.text = thresholds.bgHigh.toString()
        thresholdLowPreference.text = thresholds.bgLow.toString()
        criticalNotificationIntervalPreference.text = getCriticalNotificationInterval().toString()
    }

    /**
     * Send intent to notification service
     *
     * @param action What kind of action to be sent to the service
     * @param state If the provided action should be started or stopped
     */
    private fun sendCommandToService(action: String, state: Boolean) {
        Intent(requireContext(), WearableMonitorService::class.java).also {
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
        clearPreferenceStorage()

        // Clear all data cached in room database.
        CoroutineScope(Dispatchers.IO).launch {
            deleteCachedGlucoseValues.invoke()
        }
    }

    companion object {
        val TAG = WearableSettingsFragment::class.simpleName
    }
}