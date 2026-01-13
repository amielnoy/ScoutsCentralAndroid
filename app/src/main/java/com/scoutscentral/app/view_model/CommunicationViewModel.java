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

  /**
   * Sends a message via WhatsApp. 
   * It attempts to open the specific "צופי מוצקין" group if possible, 
   * or falls back to a general share intent.
   */
  public void sendWhatsappToScouts(Context context, String message) {
    // Note: Direct linking to a specific group by name is not supported by WhatsApp's public API.
    // However, we can use a general sharing intent which allows the user to select the "צופי מוצקין" group.
    
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.setPackage("com.whatsapp");
    intent.putExtra(Intent.EXTRA_TEXT, message);
    
    try {
        // We set the title of the chooser to remind the user where to send it
        Intent chooser = Intent.createChooser(intent, "שלח לקבוצת 'צופי מוצקין'");
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
