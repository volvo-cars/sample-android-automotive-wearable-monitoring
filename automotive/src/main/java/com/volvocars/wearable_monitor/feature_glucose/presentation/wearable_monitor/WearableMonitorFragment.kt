package com.volvocars.wearable_monitor.feature_glucose.presentation.wearable_monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.MenuItem
import com.android.car.ui.toolbar.ToolbarController
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.volvocars.wearable_monitor.R
import com.volvocars.wearable_monitor.core.util.Constants
import com.volvocars.wearable_monitor.core.util.GlucoseUtils
import com.volvocars.wearable_monitor.databinding.FragmentWearableMonitorBinding
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose
import com.volvocars.wearable_monitor.feature_glucose.presentation.settings.WearableSettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WearableMonitorFragment : Fragment() {

    private var _binding: FragmentWearableMonitorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WearableMonitorViewModel by viewModels()
    private lateinit var _locale: Locale

    private lateinit var minuteUpdateReceiver: BroadcastReceiver

    private lateinit var toolbarController: ToolbarController

    lateinit var glucoseFetchHandler: Handler
    private val glucoseFetcher = object : Runnable {
        override fun run() {
            val url = viewModel.sharedPreferenceStorage.getBaseUrl()
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.fetchGlucoseValues.invoke(url, 24).collect()
            }

            val timeInMillis = TimeUnit.MINUTES.toMillis(
                viewModel.sharedPreferenceStorage.getGlucoseFetchInterval().toLong()
            )
            glucoseFetchHandler.postDelayed(this, timeInMillis)
        }
    }

    @Inject
    lateinit var glucoseUtil: GlucoseUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWearableMonitorBinding.inflate(inflater)
        glucoseFetchHandler = Handler(Looper.getMainLooper())
        _locale = Locale.getDefault()

        toolbarController = CarUi.requireToolbar(activity!!)
        toolbarController.setMenuItems(initMenuItems())

        return binding.root
    }

    private fun initMenuItems() = listOf(
        MenuItem.builder(requireContext()).setToSettings().setOnClickListener {
            Intent(requireContext(), WearableSettingsActivity::class.java).also {
                startActivity(it)
            }
        }.build()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchCachedValues()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.glucoseValues.collect { state ->
                setData(state.glucoseValues)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        glucoseFetchHandler.post(glucoseFetcher)
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            Constants.GLUCOSE_FETCH_WORK_ID,
            ExistingPeriodicWorkPolicy.REPLACE,
            viewModel.glucoseFetchWorker
        )
        renderChart()
        startMinuteUpdater()
    }

    override fun onPause() {
        super.onPause()
        glucoseFetchHandler.removeCallbacksAndMessages(glucoseFetcher)
        requireActivity().unregisterReceiver(minuteUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Get notified when system locale changes
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _locale = newConfig.locales[0]
    }

    /**
     * Update UI components in [WearableMonitorFragment]
     *
     * @param points List of sorted [Glucose] values
     */
    private fun setData(points: List<Glucose>) {
        if (points.isEmpty()) {
            return
        }

        val isMmol = glucoseUtil.checkIsMmol()

        val entries = points.map { point ->
            val xAxis = point.date.toFloat()
            val yAxis = if (isMmol) point.sgvMmol else point.sgvUnit

            Entry(xAxis, yAxis)
        }

        val circleColors = points.map { point ->
            glucoseUtil.sgvColor(point.sgv)
        }

        // create a dataset and give it a type
        val glucoseDataSet = LineDataSet(entries, "GlucoseEntries").apply {
            axisDependency = YAxis.AxisDependency.LEFT
            color = Color.TRANSPARENT
            lineWidth = 3.2f
            setCircleColors(circleColors)
            setDrawCircles(true)
            setDrawValues(false)
            fillAlpha = 65
            circleRadius = 10f
            highLightColor = Color.WHITE
            setDrawCircleHole(false)
        }

        // create a data object with the data sets
        val data = LineData(glucoseDataSet).apply {
            setValueTextColor(Color.WHITE)
            setValueTextSize(4f)
        }

        // set data to graph
        binding.glucoseChart.apply {
            setData(data)
            notifyDataSetChanged()
            invalidate()
            animate()
        }

        // calculate difference between the latest two fetched values
        val valueDiff = glucoseUtil.calculateDifference(
            points.last().sgv, points[points.lastIndex - 1].sgv
        )

        /*
         Set glucose text to the latest fetched glucose value and
         also set the text color to depending on what the value is
        */
        binding.glucoseValueTxt.apply {
            text =
                if (isMmol) points.last().sgvMmol.toString() else points.last().sgvUnit.toString()
            setTextColor(glucoseUtil.sgvColor(points.last().sgv))
        }

        /*
         Set the glucose direction to the the latest fetched glucose value
         and also set the color to depending on what the value is
         */
        binding.glucoseDirection.apply {
            text = points.last().direction
            setTextColor(glucoseUtil.sgvColor(points.last().sgv))
        }

        // Get string array from resource
        val units = resources.getStringArray(R.array.unit_entries)
        // Write the right unit entry depending on settings
        binding.glucoseDiff.text =
            getString(R.string.glucoseDiffText, valueDiff, (if (isMmol) units[0] else units[1]))


        binding.lastUpdated.text = getLastUpdated(points.last())
    }

    /**
     * Get notified when system minute changes
     */
    private fun startMinuteUpdater() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
        }

        minuteUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val lastFetchedGlucose = viewModel.glucoseValues.value.glucoseValues.last()
                binding.lastUpdated.text = getLastUpdated(lastFetchedGlucose)
            }
        }

        requireActivity().registerReceiver(minuteUpdateReceiver, intentFilter)
    }

    /**
     * Get time since last updated glucose value
     * @param glucose Last fetched glucose value
     */
    private fun getLastUpdated(glucose: Glucose) =
        DateUtils.getRelativeTimeSpanString(glucose.date, Calendar.getInstance().timeInMillis, 0)

    /**
     * Render the chart in the monitoring view.
     */
    private fun renderChart() {
        configureGlucoseChart()
        configureXAxis()
        configureYAxis()
        binding.glucoseChart.axisRight.isEnabled = false
    }

    private fun configureGlucoseChart() {
        binding.glucoseChart.apply {
            // no description yet
            description.isEnabled = false

            // disable touch gestures
            setTouchEnabled(false)
            dragDecelerationFrictionCoef = 0.9f

            // enable scaling and dragging
            setDrawGridBackground(false)
            keepScreenOn = true

            // set an alternative background color
            setBackgroundColor(Color.TRANSPARENT)
//            setViewPortOffsets(0f, 0f, 0f, 0f)
        }
    }

    /**
     * Configure xAxis of glucose-chart
     */
    private fun configureXAxis() {
        binding.glucoseChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM_INSIDE
            typeface = Typeface.DEFAULT_BOLD
            textSize = 28f
            textColor = Color.WHITE
            setDrawAxisLine(false)
            setDrawGridLines(false)
            setCenterAxisLabels(false)
            granularity = 6f // one hour
            isGranularityEnabled = true
            valueFormatter = xAxisValueFormatter()
        }

    }

    /**
     * Create a limit line
     */
    private fun createLimitLine(thresholdValue: Long, color: String): LimitLine {
        val (dashedLineLength, dashedSpaceLength, dashedLinePhase) = Triple(10f, 12f, 15f)

        return LimitLine(
            glucoseUtil.sgvToUnit(thresholdValue.toInt()), ""
        ).apply {
            lineColor = Color.parseColor(color)
            textSize = 18f
            enableDashedLine(dashedLineLength, dashedSpaceLength, dashedLinePhase)
        }
    }

    /**
     * Create a list of limit lines
     */
    private fun createLimitLines(): List<LimitLine> {
        val (thresholdLow, thresholdHigh, thresholdTargetLow, thresholdTargetHigh) = getThresholdValues()

        val limitLineAlertLow = createLimitLine(
            thresholdLow, requireContext().resources.getString(R.color.value_out_of_range)
        )

        val limitLineWarningLow = createLimitLine(
            thresholdTargetLow,
            requireContext().resources.getString(R.color.value_is_in_range_and_nok)
        )

        val limitLineAlertHigh = createLimitLine(
            thresholdHigh, requireContext().resources.getString(R.color.value_out_of_range)
        )

        val limitLineWarningHigh = createLimitLine(
            thresholdTargetHigh,
            requireContext().resources.getString(R.color.value_is_in_range_and_nok)
        )

        return listOf(
            limitLineAlertLow,
            limitLineWarningLow,
            limitLineAlertHigh,
            limitLineWarningHigh
        )
    }

    /**
     * Configure YAxis of glucose-chart
     */
    private fun configureYAxis() {
        val (warningLow, alertLow, warningHigh, alertHigh) = createLimitLines()
        binding.glucoseChart.axisLeft.apply {
            removeAllLimitLines()
            addLimitLine(warningLow)
            addLimitLine(alertLow)
            addLimitLine(warningHigh)
            addLimitLine(alertHigh)
            setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            typeface = Typeface.DEFAULT
            textColor = Color.WHITE
            textSize = 28f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            isGranularityEnabled = false
            axisMinimum = if (glucoseUtil.checkIsMmol()) -1f else 18f
            axisMaximum = if (glucoseUtil.checkIsMmol()) 22f else 360f
            textColor = Color.WHITE
        }
    }

    /**
     * Date formatter of XAxis values
     */
    private fun xAxisValueFormatter() = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return when (viewModel.sharedPreferenceStorage.getTimeFormat()) {
                Constants.TIME_FORMAT_DEFAULT_VALUE -> {
                    SimpleDateFormat("H:mm", _locale)
                }
                else -> {
                    SimpleDateFormat("hh.mm aa", _locale)
                }
            }.apply {
                timeZone = TimeZone.getDefault()
            }.format(Calendar.getInstance().apply { timeInMillis = value.toLong() }.time)
        }
    }


    /**
     * Read the threshold values from shared preferences and return it as a list
     *
     * @return threshold low, threshold high, threshold target high, threshold target low
     */
    private fun getThresholdValues(): List<Long> {
        val thresholdLow: Long = viewModel.sharedPreferenceStorage.getThresholdLow()
        val thresholdHigh: Long = viewModel.sharedPreferenceStorage.getThresholdHigh()
        val thresholdTargetLow: Long = viewModel.sharedPreferenceStorage.getThresholdTargetLow()
        val thresholdTargetHigh: Long = viewModel.sharedPreferenceStorage.getThresholdTargetHigh()

        return listOf(thresholdLow, thresholdHigh, thresholdTargetLow, thresholdTargetHigh)
    }

    companion object {
        val TAG = WearableMonitorFragment::class.simpleName
    }
}