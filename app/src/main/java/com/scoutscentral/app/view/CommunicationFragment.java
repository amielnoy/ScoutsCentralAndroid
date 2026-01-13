package com.scoutscentral.app.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.view.adapter.AnnouncementAdapter;
import com.scoutscentral.app.view_model.CommunicationViewModel;

public class CommunicationFragment extends Fragment {
  private CommunicationViewModel viewModel;
  private AnnouncementAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_communication, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(CommunicationViewModel.class);

    adapter = new AnnouncementAdapter();
    androidx.recyclerview.widget.RecyclerView list = view.findViewById(R.id.sent_list);
    list.setLayoutManager(new LinearLayoutManager(getContext()));
    list.setAdapter(adapter);

    EditText title = view.findViewById(R.id.announcement_title);
    EditText message = view.findViewById(R.id.announcement_message);
    CheckBox checkSendHere = view.findViewById(R.id.check_send_here);
    
    MaterialButtonToggleGroup externalGroup = view.findViewById(R.id.external_channel_group);
    MaterialButton btnSendAll = view.findViewById(R.id.send_announcement);

    // עדכון טקסט הכפתור בהתאם לבחירה ב-ToggleGroup
    externalGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
        if (isChecked) {
            if (checkedId == R.id.btn_select_email) {
                btnSendAll.setText("שלח בדוא״ל כעת");
            } else if (checkedId == R.id.btn_select_whatsapp) {
                btnSendAll.setText("שלח ב-WhatsApp");
            }
        }
    });

    // כפתור השליחה הראשי
    btnSendAll.setOnClickListener(v -> {
        int checkedId = externalGroup.getCheckedButtonId();
        handleSend(title, message, checkSendHere, 
                  checkedId == R.id.btn_select_email, 
                  checkedId == R.id.btn_select_whatsapp);
    });

    viewModel.getAnnouncements().observe(getViewLifecycleOwner(), adapter::submitList);
  }

  private void handleSend(EditText titleField, EditText messageField, CheckBox internalCheck, 
                          boolean sendEmail, boolean sendWhatsapp) {
      String title = titleField.getText().toString().trim();
      String message = messageField.getText().toString().trim();

      if (title.isEmpty() || message.isEmpty()) {
          Snackbar.make(requireView(), "אנא מלא כותרת והודעה", Snackbar.LENGTH_SHORT).show();
          return;
      }

      // שליחה פנימית
      if (internalCheck.isChecked()) {
          viewModel.sendAnnouncement(title, message);
      }

      // שליחה חיצונית
      if (sendEmail) {
          viewModel.sendEmailToScouts(requireContext(), title, message);
      } else if (sendWhatsapp) {
          viewModel.sendWhatsappToScouts(requireContext(), message);
      }

      // ניקוי וסגירה
      titleField.setText("");
      messageField.setText("");
      Snackbar.make(requireView(), "ההודעה בדרך!", Snackbar.LENGTH_SHORT).show();
  }
}
