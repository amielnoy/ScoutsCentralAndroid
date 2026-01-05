package com.scoutscentral.app.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.data.Dal;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;

import java.util.List;

public class MembersViewModel extends ViewModel {
  private final Dal repository = Dal.getInstance();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public void addScout(String name, ScoutLevel level, String contact) {
    repository.addScout(name, level, contact, null);
  }
  
  public void addScout(String name, ScoutLevel level, String contact, String avatarBase64) {
    repository.addScout(name, level, contact, avatarBase64);
  }

  public void updateScout(Scout scout) {
    repository.updateScout(scout);
  }

  public void removeScout(String id) {
    repository.removeScout(id);
  }
}
