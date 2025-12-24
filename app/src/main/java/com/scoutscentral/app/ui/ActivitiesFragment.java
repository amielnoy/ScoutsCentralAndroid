package com.scoutscentral.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.data.Activity;
import com.scoutscentral.app.data.DataRepository;
import com.scoutscentral.app.data.Scout;
import com.scoutscentral.app.ui.adapter.ActivityCardAdapter;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesFragment extends Fragment implements ActivityCardAdapter.ActivityActionListener {
  private ActivitiesViewModel viewModel;
  private ActivityCardAdapter adapter;
  private final DataRepository repository = DataRepository.getInstance();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_activities, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ActivitiesViewModel.class);

    adapter = new ActivityCardAdapter(this);
    androidx.recyclerview.widget.RecyclerView list = view.findViewById(R.id.activity_list);
    list.setLayoutManager(new LinearLayoutManager(getContext()));
    list.setAdapter(adapter);

    MaterialButton addButton = view.findViewById(R.id.add_activity);
    addButton.setOnClickListener(v -> showAddDialog());

    viewModel.getActivities().observe(getViewLifecycleOwner(), adapter::submitList);
  }

  private void showAddDialog() {
    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_activity, null, false);
    EditText title = dialogView.findViewById(R.id.activity_title_input);
    EditText date = dialogView.findViewById(R.id.activity_date_input);
    EditText location = dialogView.findViewById(R.id.activity_location_input);
    EditText description = dialogView.findViewById(R.id.activity_description_input);

    new AlertDialog.Builder(getContext())
      .setTitle("הוסף פעילות חדשה")
      .setView(dialogView)
      .setPositiveButton("צור", (dialog, which) -> {
        String titleText = title.getText().toString().trim();
        String dateText = date.getText().toString().trim();
        String locationText = location.getText().toString().trim();
        String descriptionText = description.getText().toString().trim();

        if (titleText.isEmpty() || dateText.isEmpty() || locationText.isEmpty()) {
          Snackbar.make(requireView(), "אנא מלא את כל השדות", Snackbar.LENGTH_SHORT).show();
          return;
        }

        viewModel.addActivity(titleText, dateText, locationText, descriptionText);
      })
      .setNegativeButton("ביטול", null)
      .show();
  }

  @Override
  public void onAttendance(Activity activity) {
    List<Scout> scouts = repository.getScouts().getValue();
    if (scouts == null) {
      return;
    }

    String[] names = new String[scouts.size()];
    boolean[] checked = new boolean[scouts.size()];
    for (int i = 0; i < scouts.size(); i++) {
      names[i] = scouts.get(i).getName();
      checked[i] = false;
    }

    new AlertDialog.Builder(getContext())
      .setTitle("נוכחות עבור " + activity.getTitle())
      .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
      .setPositiveButton("שמור נוכחות", (dialog, which) ->
        Snackbar.make(requireView(), "נוכחות נשמרה", Snackbar.LENGTH_SHORT).show())
      .setNegativeButton("ביטול", null)
      .show();
  }
}
