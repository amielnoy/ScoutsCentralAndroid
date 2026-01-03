package com.scoutscentral.app.view_model;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.data.DataRepository;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.data.GeminiService;

import java.util.List;

public class ProgressViewModel extends ViewModel {
  private static final String TAG = "ProgressViewModel";
  private final DataRepository repository = DataRepository.getInstance();
  private final GeminiService geminiService = new GeminiService();
  private final MutableLiveData<String> generatedPlan = new MutableLiveData<>();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public LiveData<String> getGeneratedPlan() {
    return generatedPlan;
  }

  public void generatePlan(Scout scout, String additionalInterests, String additionalSkills) {
    generatedPlan.setValue("Gemini AI מנתח את נתוני החניך ומכין תוכנית אישית...");
    
    new Thread(() -> {
      String combinedInterests = mergeData(scout.getInterests(), additionalInterests);
      String combinedSkills = mergeData(scout.getSkills(), additionalSkills);
      
      String plan;
      try {
        String aiPlan = geminiService.generateProgressPlan(scout, combinedInterests, combinedSkills);
        plan = (aiPlan == null || aiPlan.isEmpty())
          ? buildFallbackPlan(scout, combinedInterests, combinedSkills)
          : aiPlan;
      } catch (Exception ex) {
        Log.e(TAG, "Gemini connection failed", ex);
        
        // Use the specific error message from the service if available (e.g., 429 quota error)
        String errorMsg = ex.getMessage() != null ? ex.getMessage() : "שגיאה בתקשורת עם Gemini AI. וודא שיש חיבור אינטרנט תקין.";
        
        plan = errorMsg + "\n\n" + 
               buildFallbackPlan(scout, combinedInterests, combinedSkills);
      }
      generatedPlan.postValue(plan);
    }).start();
  }

  private String mergeData(String existing, String additional) {
    String e = (existing != null) ? existing.trim() : "";
    String a = (additional != null) ? additional.trim() : "";
    if (e.isEmpty()) return a;
    if (a.isEmpty()) return e;
    return e + ", " + a;
  }

  private String buildFallbackPlan(Scout scout, String interests, String skills) {
    return "מסלול התקדמות מוצע (גרסת גיבוי) עבור " + scout.getName() + ":\n\n" +
      "בהתבסס על תחומי עניין: " + (interests.isEmpty() ? "כללי" : interests) + "\n" +
      "ועל כישורים נוכחיים: " + (skills.isEmpty() ? "מתחיל" : skills) + "\n\n" +
      "1. השלמת דרגת " + scout.getLevel().name() + " באמצעות פרויקט מעשי.\n" +
      "2. השתתפות בפעילות שטח מורחבת.\n" +
      "3. השגת תג מומחיות חדש בתחום העניין המרכזי.";
  }
}
