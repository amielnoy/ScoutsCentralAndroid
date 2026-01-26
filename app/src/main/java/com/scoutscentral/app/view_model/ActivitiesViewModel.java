package com.scoutscentral.app.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.data.DataAccessLayer;

import java.util.List;

public class ActivitiesViewModel extends ViewModel {
  private final DataAccessLayer repository = DataAccessLayer.getInstance();

  public LiveData<List<Activity>> getActivities() {
    return repository.getActivities();
  }

  public String addActivity(String title, String date, String location, String description) {
    return repository.addActivity(title, date, location, description);
  }

  public void updateActivity(Activity activity) {
    repository.updateActivity(activity);
  }

  public void deleteActivity(String id) {
    repository.deleteActivity(id);
  }

  public void deleteAllActivities() {
    repository.deleteAllActivities();
  }
}
