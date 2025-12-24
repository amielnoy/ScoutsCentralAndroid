package com.scoutscentral.app.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scoutscentral.app.R;
import com.scoutscentral.app.ui.ReportsViewModel;
import com.scoutscentral.app.ui.adapter.AttendanceAdapter;

public class ReportsAttendanceFragment extends Fragment {
  private ReportsViewModel viewModel;

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

    AttendanceAdapter adapter = new AttendanceAdapter();
    androidx.recyclerview.widget.RecyclerView list = view.findViewById(R.id.attendance_list);
    list.setLayoutManager(new LinearLayoutManager(getContext()));
    list.setAdapter(adapter);

    adapter.submitList(viewModel.getAttendanceData());
  }
}
