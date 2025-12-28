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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.data.Scout;
import com.scoutscentral.app.ui.ReportsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ReportsSummaryFragment extends Fragment {
  private ReportsViewModel viewModel;
  private final List<Scout> scouts = new ArrayList<>();
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    start.setOnClickListener(v -> showDatePicker(start, "בחר/י תאריך התחלה"));
    end.setOnClickListener(v -> showDatePicker(end, "בחר/י תאריך סיום"));

    generate.setOnClickListener(v -> {
      int index = spinner.getSelectedItemPosition();
      if (index < 0 || index >= scouts.size()) {
        Snackbar.make(v, "אנא בחר/י חניך", Snackbar.LENGTH_SHORT).show();
        return;
      }
      String fromStr = start.getText().toString().trim();
      String toStr = end.getText().toString().trim();
      
      if (fromStr.isEmpty() || toStr.isEmpty()) {
        Snackbar.make(v, "אנא בחר/י טווח תאריכים", Snackbar.LENGTH_SHORT).show();
        return;
      }

      try {
        LocalDate fromDate = LocalDate.parse(fromStr, dateFormatter);
        LocalDate toDate = LocalDate.parse(toStr, dateFormatter);
        if (toDate.isBefore(fromDate)) {
          Snackbar.make(v, "תאריך סיום לא יכול להיות לפני תאריך התחלה", Snackbar.LENGTH_SHORT).show();
          return;
        }
        viewModel.generateSummary(scouts.get(index), fromStr, toStr);
      } catch (Exception e) {
        Snackbar.make(v, "פורמט תאריך לא תקין", Snackbar.LENGTH_SHORT).show();
      }
    });

    viewModel.getSummary().observe(getViewLifecycleOwner(), result::setText);
  }

  private void showDatePicker(EditText target, String title) {
    long initialSelection = MaterialDatePicker.todayInUtcMilliseconds();
    String currentText = target.getText().toString();
    if (!currentText.isEmpty()) {
      try {
        LocalDate current = LocalDate.parse(currentText, dateFormatter);
        initialSelection = current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
      } catch (Exception ignored) {}
    }

    MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
      .setTitleText(title)
      .setSelection(initialSelection)
      .build();

    picker.addOnPositiveButtonClickListener(selection -> {
      if (selection != null) {
        LocalDate date = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
        target.setText(dateFormatter.format(date));
      }
    });
    picker.show(getChildFragmentManager(), "report-date");
  }

  private List<String> getScoutNames() {
    List<String> names = new ArrayList<>();
    for (Scout scout : scouts) {
      names.add(scout.getName());
    }
    return names;
  }
}
