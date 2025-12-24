package com.scoutscentral.app.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.scoutscentral.app.data.DataRepository;
import com.scoutscentral.app.data.Scout;

import java.util.List;

public class ProgressViewModel extends ViewModel {
  private final DataRepository repository = DataRepository.getInstance();
  private final MutableLiveData<String> generatedPlan = new MutableLiveData<>();

  public LiveData<List<Scout>> getScouts() {
    return repository.getScouts();
  }

  public LiveData<String> getGeneratedPlan() {
    return generatedPlan;
  }

  public void generatePlan(Scout scout, String interests, String skills) {
    String plan = "תגים מוצעים: סייר מתקדם, מוביל צוות\n\n" +
      "מסלול מותאם אישית עבור " + scout.getName() + ":\n" +
      "1. פעילות מחנאות בנושא " + interests + "\n" +
      "2. שדרוג כישורים קיימים: " + skills + "\n" +
      "3. פרויקט קהילתי קצר להשלמת התג.";
    generatedPlan.setValue(plan);
  }
}
