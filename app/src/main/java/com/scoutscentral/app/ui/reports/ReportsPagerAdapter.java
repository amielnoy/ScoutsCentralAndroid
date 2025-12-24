package com.scoutscentral.app.ui.reports;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReportsPagerAdapter extends FragmentStateAdapter {
  public ReportsPagerAdapter(@NonNull Fragment fragment) {
    super(fragment);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    if (position == 0) {
      return new ReportsSummaryFragment();
    }
    return new ReportsAttendanceFragment();
  }

  @Override
  public int getItemCount() {
    return 2;
  }
}
