package com.scoutscentral.app.view;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;
import com.scoutscentral.app.view.adapter.MemberAdapter;
import com.scoutscentral.app.view_model.MembersViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment implements MemberAdapter.MemberActionListener {
  private MembersViewModel viewModel;
  private MemberAdapter adapter;
  private List<ScoutLevel> levels = new ArrayList<>();
  private ActivityResultLauncher<Void> takePictureLauncher;
  private ImageView currentDialogImageView;
  private Bitmap currentCapturedImage;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_members, container, false);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    takePictureLauncher = registerForActivityResult(
      new ActivityResultContracts.TakePicturePreview(),
      bitmap -> {
        if (bitmap != null) {
          currentCapturedImage = bitmap;
          if (currentDialogImageView != null) {
            currentDialogImageView.setImageBitmap(bitmap);
          }
        }
      }
    );
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
    ImageView imageInput = dialogView.findViewById(R.id.member_image_input);
    Button captureButton = dialogView.findViewById(R.id.btn_capture_image);

    currentDialogImageView = imageInput;
    currentCapturedImage = null; // Reset for new dialog

    captureButton.setOnClickListener(v -> takePictureLauncher.launch(null));

    ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(
      requireContext(), android.R.layout.simple_spinner_dropdown_item, getLevelLabels());
    levelInput.setAdapter(levelAdapter);

    if (scout != null) {
      nameInput.setText(scout.getName());
      contactInput.setText(scout.getContact());
      levelInput.setSelection(levels.indexOf(scout.getLevel()));
      
      String avatarUrl = scout.getAvatarUrl();
      if (avatarUrl != null && !avatarUrl.isEmpty()) {
          // If it's a base64 string
          if (!avatarUrl.startsWith("http")) {
             try {
                 byte[] decodedString = Base64.decode(avatarUrl, Base64.DEFAULT);
                 Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                 imageInput.setImageBitmap(decodedByte);
             } catch (Exception e) {
                 imageInput.setImageResource(R.drawable.avatar_placeholder);
             }
          } else {
              // It's a real URL
              Glide.with(this)
                   .load(avatarUrl)
                   .placeholder(R.drawable.avatar_placeholder)
                   .into(imageInput);
          }
      }
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
        
        String avatarBase64 = null;
        if (currentCapturedImage != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            currentCapturedImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream); // Compress to 50% quality
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            avatarBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        if (scout == null) {
          viewModel.addScout(name, level, contact, avatarBase64);
        } else {
          scout.setName(name);
          scout.setContact(contact);
          scout.setLevel(level);
          if (avatarBase64 != null) {
              scout.setAvatarUrl(avatarBase64);
          }
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
