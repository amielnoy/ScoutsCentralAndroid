package com.scoutscentral.app.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
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
    RadioGroup externalGroup = view.findViewById(R.id.external_channel_group);
    RadioButton radioEmail = view.findViewById(R.id.radio_send_email);
    RadioButton radioWhatsapp = view.findViewById(R.id.radio_send_whatsapp);
    MaterialButton send = view.findViewById(R.id.send_announcement);

    send.setOnClickListener(v -> {
      String titleText = title.getText().toString().trim();
      String messageText = message.getText().toString().trim();
      
      if (titleText.isEmpty() || messageText.isEmpty()) {
        Snackbar.make(view, "אנא מלא כותרת והודעה", Snackbar.LENGTH_SHORT).show();
        return;
      }

      boolean sendHere = checkSendHere.isChecked();
      boolean sendEmail = radioEmail.isChecked();
      boolean sendWhatsup = radioWhatsapp.isChecked();

      // Since one of the radio buttons is checked by default, 
      // we only need to check if internal sending is also needed.
      if (sendHere) {
        viewModel.sendAnnouncement(titleText, messageText);
      }

      if (sendEmail) {
        viewModel.sendEmailToScouts(requireContext(), titleText, messageText);
      } else if (sendWhatsup) {
        viewModel.sendWhatsappToScouts(requireContext(), messageText);
      }

      title.setText("");
      message.setText("");
      Snackbar.make(view, "ההודעה נשלחה!", Snackbar.LENGTH_SHORT).show();
    });

    viewModel.getAnnouncements().observe(getViewLifecycleOwner(), adapter::submitList);
  }
}
