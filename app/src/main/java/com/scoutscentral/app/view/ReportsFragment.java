package com.scoutscentral.app.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.scoutscentral.app.R;
import com.scoutscentral.app.view.reports.ReportsPagerAdapter;

public class ReportsFragment extends Fragment {
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reports, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ViewPager2 pager = view.findViewById(R.id.reports_pager);
    TabLayout tabs = view.findViewById(R.id.reports_tabs);

    ReportsPagerAdapter adapter = new ReportsPagerAdapter(this);
    pager.setAdapter(adapter);

    new TabLayoutMediator(tabs, pager, (tab, position) -> {
      if (position == 0) {
        tab.setText("סיכום השתתפות");
      } else {
        tab.setText("סקירת נוכחות");
      }
    }).attach();
  }
}
