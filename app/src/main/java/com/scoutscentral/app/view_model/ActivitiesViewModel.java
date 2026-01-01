package com.scoutscentral.app.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.data.DataRepository;

import java.util.List;

public class ActivitiesViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();

  public LiveData<List<Activity>> getActivities() {
    return repository.getActivities();
  }

  public void addActivity(String title, String date, String location, String description) {
    repository.addActivity(title, date, location, description);
  }
}
