package com.scoutscentral.app.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.data.Activity;
import com.scoutscentral.app.data.Announcement;
import com.scoutscentral.app.data.DataRepository;
import com.scoutscentral.app.data.Scout;

import java.util.List;

public class DashboardViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public LiveData<List<Activity>> getActivities() {
    return repository.getActivities();
  }

  public LiveData<List<Announcement>> getAnnouncements() {
    return repository.getAnnouncements();
  }

  public LiveData<Long> getSyncCompletedAt() {
    return repository.getSyncCompletedAt();
  }

  public void refresh() {
    repository.refreshFromSupabase();
  }
}
