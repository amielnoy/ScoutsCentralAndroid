package com.scoutscentral.app.view_model;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.data.DataAccsesLayer;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.data.GeminiService;

import java.util.List;

public class ProgressViewModel extends ViewModel {
  private static final String TAG = "ProgressViewModel";
  private final DataAccsesLayer repository = DataAccsesLayer.getInstance();
  private final GeminiService geminiService = new GeminiService();
  private final MutableLiveData<String> generatedPlan = new MutableLiveData<>();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public LiveData<String> getGeneratedPlan() {
    return generatedPlan;
  }

  public void generatePlan(Scout scout, String positiveTraits, String negativeTraits) {
    generatedPlan.setValue("Gemini AI מנתח את נתוני החניך ומכין תוכנית אישית...");
    
    new Thread(() -> {
      String plan;
      try {
        String aiPlan = geminiService.generateProgressPlan(scout, positiveTraits, negativeTraits);
        plan = (aiPlan == null || aiPlan.isEmpty())
          ? buildFallbackPlan(scout, positiveTraits, negativeTraits)
          : aiPlan;
      } catch (Exception ex) {
        Log.e(TAG, "Gemini connection failed", ex);
        
        String errorMsg = ex.getMessage() != null ? ex.getMessage() : "שגיאה בתקשורת עם Gemini AI. וודא שיש חיבור אינטרנט תקין.";
        
        plan = errorMsg + "\n\n" + 
               buildFallbackPlan(scout, positiveTraits, negativeTraits);
      }
      generatedPlan.postValue(plan);
    }).start();
  }

  private String buildFallbackPlan(Scout scout, String positiveTraits, String negativeTraits) {
    return "מסלול התקדמות מוצע (גרסת גיבוי) עבור " + scout.getName() + ":\n\n" +
      "בהתבסס על תכונות חיוביות: " + (positiveTraits.isEmpty() ? "כללי" : positiveTraits) + "\n" +
      "ותכונות שליליות לשיפור: " + (negativeTraits.isEmpty() ? "אין" : negativeTraits) + "\n\n" +
      "1. השלמת דרגת " + scout.getLevel().name() + " באמצעות פרויקט מעשי.\n" +
      "2. השתתפות בפעילות שטח מורחבת.\n" +
      "3. השגת תג מומחיות חדש בתחום המבוסס על תכונותיו החיוביות.";
  }
}
