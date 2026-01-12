package com.scoutscentral.app.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.Announcement;
import com.scoutscentral.app.model.data.DataAccsesLayer;
import com.scoutscentral.app.model.Scout;

import java.util.List;

public class DashboardViewModel extends ViewModel {
  private final DataAccsesLayer repository = DataAccsesLayer.getInstance();

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
