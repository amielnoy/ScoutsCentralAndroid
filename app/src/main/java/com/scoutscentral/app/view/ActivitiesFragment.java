package com.scoutscentral.app.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.data.DataAccsesLayer;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.view_model.ActivitiesViewModel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;

public class ActivitiesFragment extends Fragment {
  private ActivitiesViewModel viewModel;
  private final DataAccsesLayer repository = DataAccsesLayer.getInstance();
  private List<Activity> allActivities = new ArrayList<>();

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

    MaterialButton btnTuesday = view.findViewById(R.id.btn_attendance_tuesday);
    MaterialButton btnFriday = view.findViewById(R.id.btn_attendance_friday);
    MaterialButton deleteAllButton = view.findViewById(R.id.delete_all_activities);

    btnTuesday.setOnClickListener(v -> showMeetingDatePicker("יום שלישי", Calendar.TUESDAY));
    btnFriday.setOnClickListener(v -> showMeetingDatePicker("יום שישי", Calendar.FRIDAY));

    if (deleteAllButton != null) {
        deleteAllButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("מחיקת כל הפעילויות")
                .setMessage("האם למחוק את כל הפעילויות? פעולה זו תמחק גם את הנתונים מהענן.")
                .setPositiveButton("מחק הכל", (dialog, which) -> {
                    viewModel.deleteAllActivities();
                    Snackbar.make(requireView(), "כל הפעילויות נמחקו", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("ביטול", null)
                .show();
        });
    }

    viewModel.getActivities().observe(getViewLifecycleOwner(), activities -> {
        if (activities != null) {
            this.allActivities = activities;
        }
    });
  }

  private void showMeetingDatePicker(String dayName, int dayOfWeek) {
    if (!isAdded()) return;

    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
    constraintsBuilder.setValidator(DateValidatorPointBackward.before(System.currentTimeMillis()));

    MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
    builder.setTitleText("בחר תאריך לפעולת " + dayName);
    builder.setCalendarConstraints(constraintsBuilder.build());
    builder.setTheme(R.style.Theme_ScoutsCentral_DatePicker);
    
    final MaterialDatePicker<Long> picker = builder.build();
    
    picker.addOnPositiveButtonClickListener(selection -> {
        Calendar selectedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        selectedDate.setTimeInMillis(selection);
        
        if (selectedDate.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            Snackbar.make(requireView(), "נא לבחור תאריך שחל ב" + dayName, Snackbar.LENGTH_LONG).show();
            return;
        }

        handleDateSelection(selectedDate, dayName);
    });
    
    picker.show(getChildFragmentManager(), "meeting_picker");
  }

  private void handleDateSelection(Calendar calendar, String meetingType) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      String dateStr = sdf.format(calendar.getTime());
      
      Activity existing = null;
      for (Activity act : allActivities) {
          if (act.getDate() != null && act.getDate().startsWith(dateStr)) {
              existing = act;
              break;
          }
      }

      if (existing != null) {
          onAttendance(existing);
      } else {
          new AlertDialog.Builder(requireContext())
              .setTitle("פעולה חדשה")
              .setMessage("לא נמצאה פעולה רשומה לתאריך זה. האם תרצה ליצור פעולה חדשה מסוג " + meetingType + "?")
              .setPositiveButton("צור וסמן נוכחות", (dialog, which) -> {
                  viewModel.addActivity(meetingType, dateStr + "T16:00:00Z", "שבט מוצקין", "פעולה קבועה");
                  Snackbar.make(requireView(), "פעולה נוצרה. נא לבחור שוב את התאריך לסימון נוכחות.", Snackbar.LENGTH_SHORT).show();
              })
              .setNegativeButton("ביטול", null)
              .show();
      }
  }

  public void onAttendance(Activity activity) {
    List<Scout> scouts = repository.getScouts().getValue();
    if (scouts == null) return;

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

      if (isAdded()) {
          requireActivity().runOnUiThread(() -> {
            loading.dismiss();
            new AlertDialog.Builder(requireContext())
              .setTitle("נוכחות - " + activity.getTitle() + " (" + formatDate(activity.getDate()) + ")")
              .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
              .setPositiveButton("שמור", (dialog, which) -> {
                List<String> updatedIds = new ArrayList<>();
                for (int i = 0; i < scouts.size(); i++) {
                  if (checked[i]) updatedIds.add(scouts.get(i).getId());
                }
                repository.saveAttendance(activity.getId(), updatedIds);
                Snackbar.make(requireView(), "נוכחות נשמרה", Snackbar.LENGTH_SHORT).show();
              })
              .setNegativeButton("ביטול", null)
              .show();
          });
      }
    }).start();
  }

  private String formatDate(String isoDate) {
      if (isoDate == null || isoDate.isEmpty()) return "";
      try {
          return LocalDate.parse(isoDate.split("T")[0]).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      } catch (Exception e) {
          return isoDate;
      }
  }
}
