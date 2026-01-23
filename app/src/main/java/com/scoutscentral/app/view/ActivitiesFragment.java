package com.scoutscentral.app.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DayViewDecorator;
import com.google.android.material.datepicker.MaterialDatePicker;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

    try {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointBackward.before(System.currentTimeMillis()));

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("בחר תאריך לפעולת " + dayName);
        builder.setCalendarConstraints(constraintsBuilder.build());
        builder.setTheme(R.style.Theme_ScoutsCentral_DatePicker);
        
        // Add the decorator to highlight existing activity dates
        builder.setDayViewDecorator(new ActivityDayDecorator(allActivities));
        
        final MaterialDatePicker<Long> picker = builder.build();
        
        picker.addOnPositiveButtonClickListener(selection -> {
            if (!isAdded()) return;
            
            Calendar selectedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            selectedDate.setTimeInMillis(selection);
            
            if (selectedDate.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                Snackbar.make(requireView(), "נא לבחור תאריך שחל ב" + dayName, Snackbar.LENGTH_LONG).show();
                return;
            }

            handleDateSelection(selectedDate, dayName);
        });
        
        picker.show(getChildFragmentManager(), "meeting_picker");
    } catch (Exception e) {
        Snackbar.make(requireView(), "שגיאה בפתיחת לוח השנה", Snackbar.LENGTH_SHORT).show();
    }
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
                  Snackbar.make(requireView(), "פעולה נוצרה. נא לבחור שוב את התאריך.", Snackbar.LENGTH_SHORT).show();
              })
              .setNegativeButton("ביטול", null)
              .show();
      }
  }

  public void onAttendance(Activity activity) {
    if (!isAdded()) return;
    
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

  /**
   * Decorator to highlight existing activity dates in the MaterialDatePicker.
   */
  public static class ActivityDayDecorator extends DayViewDecorator {
      private final HashSet<String> activityDates;

      public ActivityDayDecorator(List<Activity> activities) {
          this.activityDates = new HashSet<>();
          if (activities != null) {
              for (Activity act : activities) {
                  if (act.getDate() != null) {
                      activityDates.add(act.getDate().split("T")[0]);
                  }
              }
          }
      }

      protected ActivityDayDecorator(Parcel in) {
          activityDates = new HashSet<>();
          ArrayList<String> list = in.createStringArrayList();
          if (list != null) {
              activityDates.addAll(list);
          }
      }

      public static final Creator<ActivityDayDecorator> CREATOR = new Creator<ActivityDayDecorator>() {
          @Override
          public ActivityDayDecorator createFromParcel(Parcel in) {
              return new ActivityDayDecorator(in);
          }

          @Override
          public ActivityDayDecorator[] newArray(int size) {
              return new ActivityDayDecorator[size];
          }
      };

      @Override
      public int describeContents() {
          return 0;
      }

      @Override
      public void writeToParcel(@NonNull Parcel dest, int flags) {
          dest.writeStringList(new ArrayList<>(activityDates));
      }

      @Nullable
      @Override
      public ColorStateList getBackgroundColor(@NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
          String dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
          if (activityDates.contains(dateKey)) {
              return ColorStateList.valueOf(context.getColor(R.color.primary));
          }
          return null;
      }

      @Nullable
      @Override
      public ColorStateList getTextColor(@NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
          String dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
          if (activityDates.contains(dateKey)) {
              return ColorStateList.valueOf(Color.WHITE);
          }
          return null;
      }
  }
}
