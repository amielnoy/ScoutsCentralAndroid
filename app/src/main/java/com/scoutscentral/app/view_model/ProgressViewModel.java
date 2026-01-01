package com.scoutscentral.app.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.model.data.DataRepository;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.data.SupabaseService;

import java.util.List;

public class ProgressViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();
  private final SupabaseService supabaseService = new SupabaseService();
  private final MutableLiveData<String> generatedPlan = new MutableLiveData<>();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public LiveData<String> getGeneratedPlan() {
    return generatedPlan;
  }

  public void generatePlan(Scout scout, String interests, String skills) {
    generatedPlan.setValue("מכין תוכנית מותאמת אישית...");
    new Thread(() -> {
      String plan;
      try {
        if (supabaseService.isConfigured()) {
          String aiPlan = supabaseService.generateProgressPlan(scout, interests, skills);
          plan = aiPlan == null || aiPlan.isEmpty()
            ? buildFallbackPlan(scout, interests, skills)
            : aiPlan;
        } else {
          plan = buildFallbackPlan(scout, interests, skills);
        }
      } catch (Exception ex) {
        plan = buildFallbackPlan(scout, interests, skills);
      }
      generatedPlan.postValue(plan);
    }).start();
  }

  private String buildFallbackPlan(Scout scout, String interests, String skills) {
    return "תגים מוצעים: סייר מתקדם, מוביל צוות\n\n" +
      "מסלול מותאם אישית עבור " + scout.getName() + ":\n" +
      "1. פעילות מחנאות בנושא " + interests + "\n" +
      "2. שדרוג כישורים קיימים: " + skills + "\n" +
      "3. פרויקט קהילתי קצר להשלמת התג.";
  }
}
