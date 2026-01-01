package com.scoutscentral.app.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.Announcement;
import com.scoutscentral.app.model.data.DataRepository;

import java.util.List;

public class CommunicationViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();

  public LiveData<List<Announcement>> getAnnouncements() {
    return repository.getAnnouncements();
  }

  public void sendAnnouncement(String title, String message) {
    repository.addAnnouncement(title, message);
  }
}
