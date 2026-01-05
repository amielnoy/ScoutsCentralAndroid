package com.scoutscentral.app.view_model;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scoutscentral.app.model.AttendanceRecord;
import com.scoutscentral.app.model.data.Dal;
import com.scoutscentral.app.model.Scout;

import java.util.List;

public class ReportsViewModel extends ViewModel {
  private static final String TAG = "ReportsViewModel";
  private final Dal repository;
  private final MutableLiveData<String> summary = new MutableLiveData<>();

  public ReportsViewModel() {
    this(Dal.getInstance());
  }

  public ReportsViewModel(Dal repository) {
    this.repository = repository;
  }

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public LiveData<String> getSummary() {
    return summary;
  }

  public void generateSummary(Scout scout, String from, String to) {
    summary.setValue("מייצר סיכום...");
    new Thread(() -> {
      try {
        String historyJson = repository.fetchScoutActivityHistory(scout.getId(), from, to);
        JsonArray array = JsonParser.parseString(historyJson).getAsJsonArray();
        
        StringBuilder activityList = new StringBuilder();
        int count = 0;
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("activities")) {
                JsonObject act = obj.getAsJsonObject("activities");
                String title = act.get("title").getAsString();
                String date = act.get("date").getAsString();
                activityList.append("• ").append(title).append(" (").append(date.split("T")[0]).append(")\n");
                count++;
            }
        }

        String result = "סיכום השתתפות עבור " + scout.getName() + "\n" +
          "טווח: " + from + " - " + to + "\n" +
          "סך הכל פעילויות: " + count + "\n\n" +
          "רשימת פעילויות:\n" + 
          (activityList.length() > 0 ? activityList.toString() : "לא נמצאו פעילויות בטווח זה.");
          
        summary.postValue(result);
      } catch (Exception e) {
        Log.e(TAG, "Error generating report summary", e);
        summary.postValue("שגיאה ביצירת הסיכום.");
      }
    }).start();
  }

  public LiveData<List<AttendanceRecord>> getAttendanceRecords() {
    return repository.getAttendanceRecords();
  }
}
