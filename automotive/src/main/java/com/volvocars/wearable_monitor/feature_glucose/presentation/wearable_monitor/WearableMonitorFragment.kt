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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.volvocars.wearable_monitor.databinding.FragmentWearableMonitorBinding
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Thresholds
import com.volvocars.wearable_monitor.feature_glucose.presentation.model.GlucoseSummary
import com.volvocars.wearable_monitor.feature_glucose.presentation.settings.WearableSettingsActivity
import com.volvocars.wearable_monitor.feature_glucose.presentation.util.calculateDifference
import com.volvocars.wearable_monitor.feature_glucose.presentation.util.oneDecimalPrecision
import com.volvocars.wearable_monitor.feature_glucose.presentation.util.sgvToUnit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

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
            viewModel.fetchGlucoseValues()

            val timeInMillis = TimeUnit.MINUTES.toMillis(
                viewModel.getGlucoseFetchInterval().toLong()
            )

            glucoseFetchHandler.postDelayed(this, timeInMillis)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWearableMonitorBinding.inflate(inflater)
        glucoseFetchHandler = Handler(Looper.getMainLooper())
        _locale = Locale.getDefault()

        toolbarController = CarUi.requireToolbar(requireActivity())
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
        getGlucosePoints()
    }

    override fun onResume() {
        super.onResume()
        glucoseFetchHandler.post(glucoseFetcher)

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            Constants.GLUCOSE_FETCH_WORK_ID,
            ExistingPeriodicWorkPolicy.REPLACE,
            viewModel.getGlucoseFetchWorker()
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

    private fun getGlucosePoints() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.glucoseValues.collect { state ->
                if (state.glucoseValues.isNotEmpty()) {
                    setData(state.glucoseValues, viewModel.isUnitMmol())
                }
            }
        }
    }

    /**
     * Update UI components in [WearableMonitorFragment]
     *
     * @param points List of sorted [Glucose] values
     */
    private fun setData(points: List<GlucoseSummary>, isUnitMmol: Boolean) {
        val entries = points.map { point ->
            val xAxis = point.date.toFloat()
            val yAxis = if (isUnitMmol) point.sgvMmol else point.sgvUnit

            Entry(xAxis, yAxis)
        }

        val circleColors = points.map { point ->
            point.sgv.toColor()
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

        val lastFetchedValues = points.slice(points.lastIndex - 1..points.lastIndex)
        val beforeLastFetchedValue = lastFetchedValues.first()
        val lastFetchedGlucoseValue = lastFetchedValues.last()

        modifyChartData(glucoseDataSet)
        modifyGlucoseValue(lastFetchedGlucoseValue, isUnitMmol)
        modifyGlucoseDirection(lastFetchedGlucoseValue)
        modifyGlucoseDiff(beforeLastFetchedValue, lastFetchedGlucoseValue, isUnitMmol)
        binding.lastUpdated.text = points.last().getRelativeTimeSpan()
    }

    /**
     * Get notified when system minute changes
     */
    private fun startMinuteUpdater() {
        IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
        }.also {
            minuteUpdateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    viewModel.glucoseValues.value.glucoseValues.lastOrNull()?.let { glucose ->
                        binding.lastUpdated.text = glucose.getRelativeTimeSpan()
                    }
                }
            }
            requireActivity().registerReceiver(minuteUpdateReceiver, it)
        }
    }

    private fun GlucoseSummary.getRelativeTimeSpan(): String {
        return DateUtils.getRelativeTimeSpanString(
            date, Calendar.getInstance().timeInMillis, 0
        ).toString()
    }

    /**
     * Render the chart in the monitoring view.
     */
    private fun renderChart() {
        configureGlucoseChart()
        Log.d(TAG, "renderChart: ${viewModel.getThresholds()}")
        binding.glucoseChart.xAxis.configure()
        binding.glucoseChart.axisLeft.configureGlucoseValues(
            viewModel.getThresholds().toLimitLines(),
            viewModel.isUnitMmol()
        )
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
        }
    }

    /**
     * Create a limit line
     */
    private fun createLimitLine(thresholdValue: Long, color: String): LimitLine {
        val (dashedLineLength, dashedSpaceLength, dashedLinePhase) = Triple(10f, 12f, 15f)
        val limit = sgvToUnit(thresholdValue.toInt(), viewModel.isUnitMmol())

        return LimitLine(
            limit, ""
        ).apply {
            lineColor = Color.parseColor(color)
            textSize = 18f
            enableDashedLine(dashedLineLength, dashedSpaceLength, dashedLinePhase)
        }
    }

    /**
     * Date formatter of XAxis values
     */
    private fun xAxisValueFormatter() = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return when (viewModel.getTimeFormat()) {
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

    private fun modifyChartData(glucoseDataSet: LineDataSet) {
        val data = LineData(glucoseDataSet).apply {
            setValueTextColor(Color.WHITE)
            setValueTextSize(4f)
        }

        binding.glucoseChart.apply {
            setData(data)
            notifyDataSetChanged()
            invalidate()
            animate()
        }
    }

    private fun modifyGlucoseDirection(glucoseSummary: GlucoseSummary) {
        binding.glucoseDirection.apply {
            text = glucoseSummary.direction
            setTextColor(glucoseSummary.sgv.toColor())
        }
    }

    private fun modifyGlucoseValue(
        lastFetchedGlucoseValue: GlucoseSummary,
        isUnitMmol: Boolean
    ) {
        binding.glucoseValueTxt.apply {
            text = if (viewModel.isUnitMmol()) {
                lastFetchedGlucoseValue.sgvMmol.oneDecimalPrecision()
            } else {
                lastFetchedGlucoseValue.sgvUnit.toString()
            }
            setTextColor(lastFetchedGlucoseValue.sgv.toColor())
        }
    }

    private fun modifyGlucoseDiff(
        beforeLastFetchedGlucoseValue: GlucoseSummary,
        lastFetchedGlucoseValue: GlucoseSummary,
        isUnitMmol: Boolean
    ) {
        val valueDiff = calculateDifference(
            lastFetchedGlucoseValue.sgv,
            beforeLastFetchedGlucoseValue.sgv,
            isUnitMmol
        )
        val units = resources.getStringArray(R.array.unit_entries)

        binding.glucoseDiff.text = getString(
            R.string.glucoseDiffText,
            valueDiff, (if (isUnitMmol) units[0] else units[1])
        )
    }

    private fun Thresholds.toLimitLines(): List<LimitLine> {
        return listOf(
            bgLow.toLimitLine(requireContext().resources.getString(R.color.value_out_of_range)),
            bgTargetBottom.toLimitLine(requireContext().resources.getString(R.color.value_is_in_range_and_nok)),
            bgHigh.toLimitLine(requireContext().resources.getString(R.color.value_out_of_range)),
            bgTargetTop.toLimitLine(requireContext().resources.getString(R.color.value_is_in_range_and_nok))
        )
    }

    private fun Long.toLimitLine(color: String): LimitLine {
        return createLimitLine(this, color)
    }

    private fun Int.toColor(): Int {
        val colorValueOutOfRange = requireContext().getColor(R.color.value_out_of_range)
        val colorValueInRangeAndOk = requireContext().getColor(R.color.value_is_in_range_and_ok)
        val colorValueInRangeAndNok = requireContext().getColor(R.color.value_is_in_range_and_nok)
        val threshold = viewModel.getThresholds()

        return when {
            this >= threshold.bgHigh || this <= threshold.bgLow -> colorValueOutOfRange
            this in threshold.bgTargetBottom..threshold.bgTargetTop -> colorValueInRangeAndOk
            else -> colorValueInRangeAndNok
        }
    }

    private fun XAxis.configure() {
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

    private fun YAxis.configureGlucoseValues(
        limitLines: List<LimitLine>,
        isUnitMmol: Boolean
    ) {
        removeAllLimitLines()
        addLimitLine(limitLines[0])
        addLimitLine(limitLines[1])
        addLimitLine(limitLines[2])
        addLimitLine(limitLines[3])
        setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        typeface = Typeface.DEFAULT
        textColor = Color.WHITE
        textSize = 28f
        setDrawGridLines(false)
        setDrawAxisLine(false)
        isGranularityEnabled = false
        axisMinimum = if (isUnitMmol) -1f else 18f
        axisMaximum = if (isUnitMmol) 22f else 360f
        textColor = Color.WHITE
    }

    companion object {
        val TAG = WearableMonitorFragment::class.simpleName
    }
}