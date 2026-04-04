package com.volvocars.wearablemonitor.presentation.wearablemonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.PeriodicWorkRequest
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
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WearableMonitorFragment : Fragment() {
    private var _binding: FragmentWearableMonitorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WearableMonitorViewModel by viewModels()
    private lateinit var locale: Locale
    private lateinit var minuteUpdateReceiver: BroadcastReceiver
    private lateinit var toolbarController: ToolbarController

    @Inject
    lateinit var glucoseFetchWorker: PeriodicWorkRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWearableMonitorBinding.inflate(inflater)
        locale = Locale.getDefault()
        startForegroundService()
        // Schedule the periodic WorkManager job once when the view is first created.
        GlucoseFetchWorker.create(requireContext(), glucoseFetchWorker)

        toolbarController = CarUi.requireToolbar(requireActivity()).apply {
            setMenuItems(initMenuItems())
        }

        return binding.root
    }

    private fun initMenuItems() = listOf(
        MenuItem.builder(requireContext()).setToSettings().setOnClickListener {
            WearableSettingsActivity.startActivity(requireContext())
        }.build(),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeGlucosePoints()
    }

    override fun onResume() {
        super.onResume()
        // Trigger one immediate refresh; WorkManager handles the periodic cadence.
        viewModel.fetchGlucoseValues()
        renderChart()
        startMinuteUpdater()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(minuteUpdateReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        locale = newConfig.locales[0]
    }

    private fun observeGlucosePoints() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.glucoseValues.collect { state ->
                state.error?.let { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                if (state.glucoseValues.isNotEmpty()) {
                    setData(state.glucoseValues, viewModel.isUnitMmol())
                }
            }
        }
    }

    /**
     * Update UI components in [WearableMonitorFragment]
     *
     * @param points List of sorted [GlucoseSummary] values
     */
    private fun setData(points: List<GlucoseSummary>, isUnitMmol: Boolean) {
        val entries = points.map { point ->
            Entry(point.date.toFloat(), if (isUnitMmol) point.sgvMmol else point.sgvUnit)
        }

        val circleColors = points.map { point ->
            point.sgv.toColor(requireContext(), viewModel.getThresholds())
        }

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

    private fun startMinuteUpdater() {
        minuteUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.glucoseValues.value.glucoseValues.lastOrNull()?.let { glucose ->
                    binding.lastUpdated.text = glucose.getRelativeTimeSpan()
                }
            }
        }
        requireActivity().registerReceiver(
            minuteUpdateReceiver,
            IntentFilter(Intent.ACTION_TIME_TICK),
        )
    }

    private fun renderChart() {
        configureGlucoseChart()
        Log.d(TAG, "renderChart: ${viewModel.getThresholds()}")
        binding.glucoseChart.xAxis.configure(viewModel.getTimeFormat(), locale)
        binding.glucoseChart.axisLeft.configureGlucoseValues(
            viewModel.getThresholds().toLimitLines(requireContext(), viewModel.isUnitMmol()),
            viewModel.isUnitMmol(),
        )
        binding.glucoseChart.axisRight.isEnabled = false
    }

    private fun configureGlucoseChart() {
        binding.glucoseChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            dragDecelerationFrictionCoef = 0.9f
            setDrawGridBackground(false)
            keepScreenOn = true
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
                lastFetchedGlucoseValue.sgv.toColor(requireContext(), viewModel.getThresholds()),
            )
        }
    }

    private fun modifyGlucoseDiff(
        beforeLastFetchedGlucoseValue: GlucoseSummary,
        lastFetchedGlucoseValue: GlucoseSummary,
        isUnitMmol: Boolean,
    ) {
        val valueDiff = calculateDifference(
            lastFetchedGlucoseValue.sgv,
            beforeLastFetchedGlucoseValue.sgv,
            isUnitMmol,
        )
        val units = resources.getStringArray(R.array.unit_entries)

        binding.glucoseDiff.text = getString(
            R.string.glucoseDiffText,
            valueDiff,
            (if (isUnitMmol) units[0] else units[1]),
        )
    }

    private fun startForegroundService() {
        WearableMonitorService.startAsForeground(
            requireContext(),
            NotificationConstants.ACTION_SHOW_GLUCOSE_VALUES,
        )
    }

    companion object {
        private const val TAG = "WearableMonitorFragment"
    }
}
