package com.scoutscentral.app.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.scoutscentral.app.R;
import com.scoutscentral.app.data.Scout;
import com.scoutscentral.app.ui.ReportsViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReportsSummaryFragment extends Fragment {
  private ReportsViewModel viewModel;
  private final List<Scout> scouts = new ArrayList<>();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reports_summary, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

    Spinner spinner = view.findViewById(R.id.report_scout_spinner);
    EditText start = view.findViewById(R.id.report_start_date);
    EditText end = view.findViewById(R.id.report_end_date);
    MaterialButton generate = view.findViewById(R.id.report_generate);
    TextView result = view.findViewById(R.id.report_result);

    viewModel.getScouts().observe(getViewLifecycleOwner(), list -> {
      scouts.clear();
      scouts.addAll(list);
      ArrayAdapter<String> adapter = new ArrayAdapter<>(
        requireContext(), android.R.layout.simple_spinner_dropdown_item, getScoutNames());
      spinner.setAdapter(adapter);
    });

    generate.setOnClickListener(v -> {
      int index = spinner.getSelectedItemPosition();
      if (index < 0 || index >= scouts.size()) {
        return;
      }
      String from = start.getText().toString().trim();
      String to = end.getText().toString().trim();
      if (from.isEmpty() || to.isEmpty()) {
        return;
      }
      viewModel.generateSummary(scouts.get(index), from, to);
    });

    viewModel.getSummary().observe(getViewLifecycleOwner(), result::setText);
  }

  private List<String> getScoutNames() {
    List<String> names = new ArrayList<>();
    for (Scout scout : scouts) {
      names.add(scout.getName());
    }
    return names;
  }
}
