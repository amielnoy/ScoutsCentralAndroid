package com.scoutscentral.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.data.Scout;
import com.scoutscentral.app.data.ScoutLevel;
import com.scoutscentral.app.ui.adapter.MemberAdapter;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment implements MemberAdapter.MemberActionListener {
  private MembersViewModel viewModel;
  private MemberAdapter adapter;
  private List<ScoutLevel> levels = new ArrayList<>();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_members, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

    adapter = new MemberAdapter(this);
    androidx.recyclerview.widget.RecyclerView list = view.findViewById(R.id.member_list);
    list.setLayoutManager(new LinearLayoutManager(getContext()));
    list.setAdapter(adapter);

    MaterialButton addButton = view.findViewById(R.id.add_member);
    addButton.setOnClickListener(v -> showMemberDialog(null));

    viewModel.getScouts().observe(getViewLifecycleOwner(), adapter::submitList);

    for (ScoutLevel level : ScoutLevel.values()) {
      levels.add(level);
    }
  }

  @Override
  public void onEdit(Scout scout) {
    showMemberDialog(scout);
  }

  @Override
  public void onDelete(Scout scout) {
    viewModel.removeScout(scout.getId());
    Snackbar.make(requireView(), "חבר נמחק", Snackbar.LENGTH_SHORT).show();
  }

  private void showMemberDialog(Scout scout) {
    LayoutInflater inflater = LayoutInflater.from(getContext());
    View dialogView = inflater.inflate(R.layout.dialog_member, null, false);

    EditText nameInput = dialogView.findViewById(R.id.member_name_input);
    Spinner levelInput = dialogView.findViewById(R.id.member_level_input);
    EditText contactInput = dialogView.findViewById(R.id.member_contact_input);

    ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(
      requireContext(), android.R.layout.simple_spinner_dropdown_item, getLevelLabels());
    levelInput.setAdapter(levelAdapter);

    if (scout != null) {
      nameInput.setText(scout.getName());
      contactInput.setText(scout.getContact());
      levelInput.setSelection(levels.indexOf(scout.getLevel()));
    }

    new AlertDialog.Builder(getContext())
      .setTitle(scout == null ? "הוסף חבר חדש" : "ערוך חבר")
      .setView(dialogView)
      .setPositiveButton("שמור", (dialog, which) -> {
        String name = nameInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        ScoutLevel level = levels.get(levelInput.getSelectedItemPosition());

        if (name.isEmpty() || contact.isEmpty()) {
          Snackbar.make(requireView(), "אנא מלא את כל השדות", Snackbar.LENGTH_SHORT).show();
          return;
        }

        if (scout == null) {
          viewModel.addScout(name, level, contact);
        } else {
          scout.setName(name);
          scout.setContact(contact);
          scout.setLevel(level);
          viewModel.updateScout(scout);
        }
      })
      .setNegativeButton("ביטול", null)
      .show();
  }

  private List<String> getLevelLabels() {
    List<String> labels = new ArrayList<>();
    for (ScoutLevel level : levels) {
      labels.add(level.getLabel());
    }
    return labels;
  }
}
