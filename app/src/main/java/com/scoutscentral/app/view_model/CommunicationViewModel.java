package com.scoutscentral.app.view_model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.Announcement;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.data.DataRepository;

import java.util.ArrayList;
import java.util.List;

public class CommunicationViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();

  public LiveData<List<Announcement>> getAnnouncements() {
    return repository.getAnnouncements();
  }

  public void sendAnnouncement(String title, String message) {
    repository.addAnnouncement(title, message);
  }

  public void sendEmailToScouts(Context context, String title, String message) {
    List<Scout> scouts = repository.getScouts().getValue();
    if (scouts == null || scouts.isEmpty()) return;

    List<String> emails = new ArrayList<>();
    for (Scout scout : scouts) {
      String contact = scout.getContact();
      if (contact != null && contact.contains("@")) {
        emails.add(contact);
      }
    }

    if (emails.isEmpty()) return;

    Intent intent = new Intent(Intent.ACTION_SENDTO);
    intent.setData(Uri.parse("mailto:"));
    intent.putExtra(Intent.EXTRA_EMAIL, emails.toArray(new String[0]));
    intent.putExtra(Intent.EXTRA_SUBJECT, title);
    intent.putExtra(Intent.EXTRA_TEXT, message);
    
    // Create chooser to let user pick email client
    Intent chooser = Intent.createChooser(intent, "בחר אפליקציית דוא\"ל");
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(chooser);
  }
}
