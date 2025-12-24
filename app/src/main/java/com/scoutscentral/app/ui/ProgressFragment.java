package com.scoutscentral.app.ui;

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

import java.util.ArrayList;
import java.util.List;

public class ProgressFragment extends Fragment {
  private ProgressViewModel viewModel;
  private final List<Scout> scouts = new ArrayList<>();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_progress, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

    Spinner spinner = view.findViewById(R.id.progress_scout_spinner);
    EditText interests = view.findViewById(R.id.progress_interests);
    EditText skills = view.findViewById(R.id.progress_skills);
    TextView result = view.findViewById(R.id.progress_result);
    MaterialButton generate = view.findViewById(R.id.progress_generate);

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
      Scout selected = scouts.get(index);
      viewModel.generatePlan(selected, interests.getText().toString(), skills.getText().toString());
    });

    viewModel.getGeneratedPlan().observe(getViewLifecycleOwner(), result::setText);
  }

  private List<String> getScoutNames() {
    List<String> names = new ArrayList<>();
    for (Scout scout : scouts) {
      names.add(scout.getName());
    }
    return names;
  }
}
