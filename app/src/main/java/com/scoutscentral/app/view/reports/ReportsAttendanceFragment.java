package com.scoutscentral.app.view.reports;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.AttendanceRecord;
import com.scoutscentral.app.view_model.ReportsViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReportsAttendanceFragment extends Fragment {
  private ReportsViewModel viewModel;
  private HorizontalBarChart chart;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reports_attendance, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);
    chart = view.findViewById(R.id.attendance_chart);

    setupChart();

    viewModel.getAttendanceRecords().observe(getViewLifecycleOwner(), this::updateChartData);
  }

  private void setupChart() {
    chart.setDrawBarShadow(false);
    chart.setDrawValueAboveBar(true);
    chart.getDescription().setEnabled(false);
    chart.setPinchZoom(false);
    chart.setDrawGridBackground(false);
    chart.getLegend().setEnabled(false);

    XAxis xAxis = chart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawGridLines(false);
    xAxis.setGranularity(1f);
    xAxis.setLabelCount(5);
    xAxis.setTypeface(Typeface.DEFAULT_BOLD);
    xAxis.setTextColor(Color.parseColor("#1A1C1E"));
    xAxis.setTextSize(12f);

    YAxis leftAxis = chart.getAxisLeft();
    leftAxis.setDrawGridLines(false);
    leftAxis.setAxisMinimum(0f);
    leftAxis.setTypeface(Typeface.DEFAULT_BOLD);
    leftAxis.setGranularity(1f); // Only steps of 1
    leftAxis.setGranularityEnabled(true);
    
    // Format axis labels as integers
    leftAxis.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return String.valueOf((int) value);
      }
    });

    chart.getAxisRight().setEnabled(false);
  }

  private void updateChartData(List<AttendanceRecord> records) {
    if (records == null || records.isEmpty()) return;

    List<BarEntry> entries = new ArrayList<>();
    List<String> labels = new ArrayList<>();

    int limit = Math.min(5, records.size());
    for (int i = 0; i < limit; i++) {
      AttendanceRecord record = records.get(i);
      entries.add(new BarEntry(i, record.getAttendance()));
      labels.add(record.getActivityName());
    }

    BarDataSet dataSet = new BarDataSet(entries, "Attendance");
    dataSet.setColor(Color.parseColor("#3D6A4B"));
    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
    dataSet.setValueTextColor(Color.parseColor("#1A1C1E"));
    dataSet.setValueTextSize(12f);
    
    // Ensure values on bars are integers too
    dataSet.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return String.valueOf((int) value);
      }
    });

    BarData data = new BarData(dataSet);
    data.setBarWidth(0.6f);

    chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
    chart.setData(data);
    chart.setFitBars(true);
    chart.invalidate();
  }
}
