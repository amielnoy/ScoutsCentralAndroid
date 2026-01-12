package com.scoutscentral.app.view_model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.Announcement;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.data.DataAccsesLayer;

import java.util.ArrayList;
import java.util.List;

public class CommunicationViewModel extends ViewModel {
  private final DataAccsesLayer repository;

  public CommunicationViewModel() {
    this(DataAccsesLayer.getInstance());
  }

  public CommunicationViewModel(DataAccsesLayer repository) {
    this.repository = repository;
  }

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
    
    Intent chooser = Intent.createChooser(intent, "בחר אפליקציית דוא\"ל");
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(chooser);
  }

  public void sendWhatsappToScouts(Context context, String message) {
    List<Scout> scouts = repository.getScouts().getValue();
    if (scouts == null || scouts.isEmpty()) return;

    // WhatsApp doesn't support multiple recipients via Intent API directly without a business API
    // The "Free API" approach for personal WhatsApp is the wa.me link or standard share intent
    // To send to multiple people at once, we use the standard Android Share intent which allows selecting recipients
    
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.setPackage("com.whatsapp");
    intent.putExtra(Intent.EXTRA_TEXT, message);
    
    try {
        Intent chooser = Intent.createChooser(intent, "שלח הודעה ב-WhatsApp");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    } catch (Exception e) {
        // Fallback if WhatsApp is not installed
        Intent fallback = new Intent(Intent.ACTION_SEND);
        fallback.setType("text/plain");
        fallback.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(fallback, "שתף הודעה"));
    }
  }
}
