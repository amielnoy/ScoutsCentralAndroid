package com.scoutscentral.app.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.AttendanceRecord;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.view.adapter.ActivityRowAdapter;
import com.scoutscentral.app.view.adapter.AnnouncementAdapter;
import com.scoutscentral.app.view_model.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
  private DashboardViewModel viewModel;
  private ActivityRowAdapter activityAdapter;
  private AnnouncementAdapter announcementAdapter;
  private BarChart attendanceChart;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

    attendanceChart = view.findViewById(R.id.attendance_chart);
    activityAdapter = new ActivityRowAdapter();
    announcementAdapter = new AnnouncementAdapter();

    androidx.recyclerview.widget.RecyclerView upcoming = view.findViewById(R.id.upcoming_list);
    upcoming.setLayoutManager(new GridLayoutManager(getContext(), 2));
    upcoming.setAdapter(activityAdapter);

    androidx.recyclerview.widget.RecyclerView announcements = view.findViewById(R.id.announcement_list);
    announcements.setLayoutManager(new LinearLayoutManager(getContext()));
    announcements.setAdapter(announcementAdapter);

    View syncNowButton = view.findViewById(R.id.sync_now_button);
    syncNowButton.setOnClickListener(v -> viewModel.refresh());

    ViewGroup statsContainer = view.findViewById(R.id.dashboard_stats);

    viewModel.getScouts().observe(getViewLifecycleOwner(), scouts -> {
      updateStats(statsContainer, scouts, viewModel.getActivities().getValue());
    });

    viewModel.getActivities().observe(getViewLifecycleOwner(), activities -> {
      updateStats(statsContainer, viewModel.getScouts().getValue(), activities);
      if (activities != null) {
          // Limit to 6 activities for 3x2 grid balance
          List<Activity> limited = activities.subList(0, Math.min(6, activities.size()));
          activityAdapter.submitList(limited);
      }
    });

    viewModel.getAnnouncements().observe(getViewLifecycleOwner(), announcementAdapter::submitList);

    viewModel.getAttendanceRecords().observe(getViewLifecycleOwner(), this::updateChart);

    viewModel.getSyncCompletedAt().observe(getViewLifecycleOwner(), completedAt -> {
      if (completedAt != null) {
        Toast.makeText(getContext(), "סנכרון הושלם", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void updateChart(List<AttendanceRecord> records) {
    if (records == null || records.isEmpty() || attendanceChart == null) return;

    List<BarEntry> entries = new ArrayList<>();
    for (int i = 0; i < records.size(); i++) {
      entries.add(new BarEntry(i, records.get(i).getAttendance()));
    }

    BarDataSet dataSet = new BarDataSet(entries, "נוכחות חניכים");
    dataSet.setColor(requireContext().getColor(R.color.primary));
    // Display the date above each bar
    dataSet.setValueFormatter(new ValueFormatter() {
        @Override
        public String getBarLabel(BarEntry barEntry) {
            int index = (int) barEntry.getX();
            if (index >= 0 && index < records.size()) {
                return records.get(index).getDate();
            }
            return "";
        }
    });
    dataSet.setValueTextSize(10f);
    dataSet.setValueTextColor(Color.DKGRAY);

    BarData barData = new BarData(dataSet);
    barData.setBarWidth(0.6f);

    attendanceChart.setData(barData);
    attendanceChart.getDescription().setEnabled(false);
    attendanceChart.getLegend().setEnabled(false);
    attendanceChart.setDrawValueAboveBar(true);
    
    XAxis xAxis = attendanceChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawGridLines(false);
    xAxis.setGranularity(1f);
    xAxis.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        int index = (int) value;
        if (index >= 0 && index < records.size()) {
          return records.get(index).getActivityName();
        }
        return "";
      }
    });

    attendanceChart.getAxisLeft().setAxisMinimum(0f);
    attendanceChart.getAxisRight().setEnabled(false);
    attendanceChart.animateY(1000);
    attendanceChart.invalidate();
  }

  private void updateStats(ViewGroup container, List<Scout> scouts, List<Activity> activities) {
    container.removeAllViews();
    int totalScouts = scouts == null ? 0 : scouts.size();
    int totalActivities = activities == null ? 0 : activities.size();

    addStatCard(container, "סך הכל חניכים", String.valueOf(totalScouts), "פעילים השנה");
    addStatCard(container, "פעילויות מתוכננות", String.valueOf(totalActivities), "בחודש הקרוב");
    addStatCard(container, "ימי פעילות", "שלישי, שישי", "נוכחות חובה");
    addStatCard(container, "מצב סנכרון", "מחובר לענן", "עדכני");
  }

  private void addStatCard(ViewGroup container, String label, String value, String subtitle) {
    LayoutInflater inflater = LayoutInflater.from(getContext());
    View card = inflater.inflate(R.layout.item_stat_card, container, false);
    TextView labelView = card.findViewById(R.id.stat_label);
    TextView valueView = card.findViewById(R.id.stat_value);
    TextView subtitleView = card.findViewById(R.id.stat_subtitle);

    labelView.setText(label);
    valueView.setText(value);
    subtitleView.setText(subtitle);

    container.addView(card);
  }
}
