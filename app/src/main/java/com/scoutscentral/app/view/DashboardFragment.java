package com.scoutscentral.app.view;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.view.adapter.ActivityRowAdapter;
import com.scoutscentral.app.view.adapter.AnnouncementAdapter;
import com.scoutscentral.app.view_model.DashboardViewModel;

import java.util.List;

public class DashboardFragment extends Fragment {
  private DashboardViewModel viewModel;
  private ActivityRowAdapter activityAdapter;
  private AnnouncementAdapter announcementAdapter;

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

    activityAdapter = new ActivityRowAdapter();
    announcementAdapter = new AnnouncementAdapter();

    androidx.recyclerview.widget.RecyclerView upcoming = view.findViewById(R.id.upcoming_list);
    upcoming.setLayoutManager(new LinearLayoutManager(getContext()));
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
      activityAdapter.submitList(activities);
    });

    viewModel.getAnnouncements().observe(getViewLifecycleOwner(), announcementAdapter::submitList);

    viewModel.getSyncCompletedAt().observe(getViewLifecycleOwner(), completedAt -> {
      if (completedAt != null) {
        Toast.makeText(getContext(), "סנכרון הושלם", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void updateStats(ViewGroup container, List<Scout> scouts, List<Activity> activities) {
    container.removeAllViews();
    int totalScouts = scouts == null ? 0 : scouts.size();
    int totalActivities = activities == null ? 0 : activities.size();

    addStatCard(container, "סך הכל חניכים", String.valueOf(totalScouts), "+2 מהחודש שעבר");
    addStatCard(container, "פעילויות קרובות", String.valueOf(totalActivities), "3 מתוכננות החודש");
    addStatCard(container, "תגים שנצברו", "+12", "+15% מהחודש שעבר");
    addStatCard(container, "שיעור נוכחות", "92.5%", "-1.2% מהחודש שעבר");
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
