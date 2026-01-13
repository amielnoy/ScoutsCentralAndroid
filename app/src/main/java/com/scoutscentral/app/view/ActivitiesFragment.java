package com.scoutscentral.app.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.data.DataAccsesLayer;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.view.adapter.ActivityCardAdapter;
import com.scoutscentral.app.view_model.ActivitiesViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ActivitiesFragment extends Fragment implements ActivityCardAdapter.ActivityActionListener {
  private ActivitiesViewModel viewModel;
  private ActivityCardAdapter adapter;
  private final DataAccsesLayer repository = DataAccsesLayer.getInstance();
  
  private ActivityResultLauncher<String> galleryLauncher;
  private Activity activeActivityForImage;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_activities, container, false);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      galleryLauncher = registerForActivityResult(
          new ActivityResultContracts.GetContent(),
          uri -> {
              if (uri != null && activeActivityForImage != null) {
                  handleGalleryResult(uri);
              }
          }
      );
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

  private void handleGalleryResult(Uri uri) {
      try {
          InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
          Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
          
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
          byte[] byteArray = byteArrayOutputStream.toByteArray();
          String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
          
          activeActivityForImage.setImageUrl(base64Image);
          viewModel.updateActivity(activeActivityForImage);
          Snackbar.make(requireView(), "תמונת הפעילות עודכנה", Snackbar.LENGTH_SHORT).show();
          
      } catch (Exception e) {
          Snackbar.make(requireView(), "שגיאה בטעינת התמונה", Snackbar.LENGTH_SHORT).show();
      } finally {
          activeActivityForImage = null;
      }
  }

  private void showAddDialog() {
    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_activity, null, false);
    EditText title = dialogView.findViewById(R.id.activity_title_input);
    EditText dateInput = dialogView.findViewById(R.id.activity_date_input);
    EditText location = dialogView.findViewById(R.id.activity_location_input);
    EditText description = dialogView.findViewById(R.id.activity_description_input);

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    dateInput.setFocusable(false);
    dateInput.setOnClickListener(v -> {
      new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
          calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
          calendar.set(Calendar.MINUTE, minute);
          dateInput.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

      }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    });

    new AlertDialog.Builder(getContext())
      .setTitle("הוסף פעילות חדשה")
      .setView(dialogView)
      .setPositiveButton("צור", (dialog, which) -> {
        String titleText = title.getText().toString().trim();
        String dateText = dateInput.getText().toString().trim();
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

    Snackbar loading = Snackbar.make(requireView(), "טוען נוכחות...", Snackbar.LENGTH_INDEFINITE);
    loading.show();

    new Thread(() -> {
      List<String> presentIds = repository.fetchAttendanceForActivity(activity.getId());
      String[] names = new String[scouts.size()];
      boolean[] checked = new boolean[scouts.size()];
      for (int i = 0; i < scouts.size(); i++) {
        Scout scout = scouts.get(i);
        names[i] = scout.getName();
        checked[i] = presentIds.contains(scout.getId());
      }

      requireActivity().runOnUiThread(() -> {
        loading.dismiss();
        new AlertDialog.Builder(getContext())
          .setTitle("נוכחות עבור " + activity.getTitle())
          .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
          .setPositiveButton("שמור נוכחות", (dialog, which) -> {
            List<String> updatedIds = new ArrayList<>();
            for (int i = 0; i < scouts.size(); i++) {
              if (checked[i]) {
                updatedIds.add(scouts.get(i).getId());
              }
            }
            repository.saveAttendance(activity.getId(), updatedIds);
            Snackbar.make(requireView(), "נוכחות נשמרה", Snackbar.LENGTH_SHORT).show();
          })
          .setNegativeButton("ביטול", null)
          .show();
      });
    }).start();
  }
  
  @Override
  public void onDelete(Activity activity) {
      new AlertDialog.Builder(getContext())
          .setTitle("מחק פעילות")
          .setMessage("האם אתה בטוח שברצונך למחוק את הפעילות \"" + activity.getTitle() + "\"?")
          .setPositiveButton("מחק", (dialog, which) -> {
              viewModel.deleteActivity(activity.getId());
              Snackbar.make(requireView(), "הפעילות נמחקה", Snackbar.LENGTH_SHORT).show();
          })
          .setNegativeButton("ביטול", null)
          .show();
  }

  @Override
  public void onImageClick(Activity activity) {
      activeActivityForImage = activity;
      galleryLauncher.launch("image/*");
  }
}
