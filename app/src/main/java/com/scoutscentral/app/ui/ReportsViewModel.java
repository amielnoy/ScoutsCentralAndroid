package com.scoutscentral.app.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.data.AttendanceRecord;
import com.scoutscentral.app.data.DataRepository;
import com.scoutscentral.app.data.Scout;

import java.util.List;

public class ReportsViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();
  private final MutableLiveData<String> summary = new MutableLiveData<>();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public LiveData<String> getSummary() {
    return summary;
  }

  public void generateSummary(Scout scout, String from, String to) {
    String result = "סיכום השתתפות עבור " + scout.getName() + "\n" +
      "טווח: " + from + " - " + to + "\n" +
      "החניך השתתף בפעילויות מפתח והראה עקביות מרשימה.";
    summary.setValue(result);
  }

  public LiveData<List<AttendanceRecord>> getAttendanceRecords() {
    return repository.getAttendanceRecords();
  }
}
