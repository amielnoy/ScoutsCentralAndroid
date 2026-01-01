package com.scoutscentral.app.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.data.DataRepository;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;

import java.util.List;

public class MembersViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public void addScout(String name, ScoutLevel level, String contact) {
    repository.addScout(name, level, contact);
  }

  public void updateScout(Scout scout) {
    repository.updateScout(scout);
  }

  public void removeScout(String id) {
    repository.removeScout(id);
  }
}
