package com.volvocars.wearablemonitor.presentation.wearablemonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.MenuItem
import com.android.car.ui.toolbar.ToolbarController
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.core.service.WearableMonitorService
import com.volvocars.wearablemonitor.core.util.NotificationConstants
import com.volvocars.wearablemonitor.core.worker.GlucoseFetchWorker
import com.volvocars.wearablemonitor.databinding.FragmentWearableMonitorBinding
import com.volvocars.wearablemonitor.domain.model.Glucose
import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary
import com.volvocars.wearablemonitor.presentation.settings.WearableSettingsActivity
import com.volvocars.wearablemonitor.presentation.util.calculateDifference
import com.volvocars.wearablemonitor.presentation.util.configure
import com.volvocars.wearablemonitor.presentation.util.configureGlucoseValues
import com.volvocars.wearablemonitor.presentation.util.getRelativeTimeSpan
import com.volvocars.wearablemonitor.presentation.util.oneDecimalPrecision
import com.volvocars.wearablemonitor.presentation.util.toColor
import com.volvocars.wearablemonitor.presentation.util.toLimitLines
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
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
        startForegroundService()

        toolbarController = CarUi.requireToolbar(requireActivity()).apply {
            setMenuItems(initMenuItems())
        }

        return binding.root
    }

    private fun initMenuItems() = listOf(
        MenuItem.builder(requireContext()).setToSettings().setOnClickListener {
            WearableSettingsActivity.startActivity(requireContext())
        }.build()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getGlucosePoints()
    }

    override fun onResume() {
        super.onResume()
        glucoseFetchHandler.post(glucoseFetcher)
        GlucoseFetchWorker.create(requireContext(), viewModel.getGlucoseFetchWorker())
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
            point.sgv.toColor(requireContext(), viewModel.getThresholds())
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
        modifyGlucoseValue(lastFetchedGlucoseValue)
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

    /**
     * Render the chart in the monitoring view.
     */
    private fun renderChart() {
        configureGlucoseChart()
        Log.d(TAG, "renderChart: ${viewModel.getThresholds()}")
        binding.glucoseChart.xAxis.configure(viewModel.getTimeFormat(), _locale)
        binding.glucoseChart.axisLeft.configureGlucoseValues(
            viewModel.getThresholds().toLimitLines(requireContext(), viewModel.isUnitMmol()),
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
            setTextColor(glucoseSummary.sgv.toColor(requireContext(), viewModel.getThresholds()))
        }
    }

    private fun modifyGlucoseValue(lastFetchedGlucoseValue: GlucoseSummary) {
        binding.glucoseValueTxt.apply {
            text = if (viewModel.isUnitMmol()) {
                lastFetchedGlucoseValue.sgvMmol.oneDecimalPrecision()
            } else {
                lastFetchedGlucoseValue.sgvUnit.toString()
            }
            setTextColor(
                lastFetchedGlucoseValue.sgv.toColor(
                    requireContext(),
                    viewModel.getThresholds()
                )
            )
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

    private fun startForegroundService() {
        WearableMonitorService.startAsForeground(
            requireContext(),
            NotificationConstants.ACTION_SHOW_GLUCOSE_VALUES
        )
    }

    companion object {
        val TAG = WearableMonitorFragment::class.simpleName
    }
}