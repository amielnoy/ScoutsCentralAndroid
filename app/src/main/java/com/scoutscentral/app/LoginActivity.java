package com.scoutscentral.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.auth.AuthStore;
import com.scoutscentral.app.data.SupabaseService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
  private SupabaseService supabaseService;
  private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    if (supabaseService == null) {
      supabaseService = new SupabaseService();
    }

    EditText emailInput = findViewById(R.id.login_email);
    EditText passwordInput = findViewById(R.id.login_password);
    MaterialButton loginButton = findViewById(R.id.login_submit);

    loginButton.setOnClickListener(v -> {
      String email = emailInput.getText().toString().trim();
      String password = passwordInput.getText().toString();
      if (email.isEmpty() || password.isEmpty()) {
        Snackbar.make(v, "אנא מלאו אימייל וסיסמה", Snackbar.LENGTH_SHORT).show();
        return;
      }
      attemptLogin(v, email, password);
    });
  }

  @VisibleForTesting
  public void setSupabaseService(SupabaseService service) {
    this.supabaseService = service;
  }

  private void attemptLogin(View view, String email, String password) {
    if (!supabaseService.isConfigured()) {
      Snackbar.make(view, "Supabase לא מוגדר", Snackbar.LENGTH_SHORT).show();
      return;
    }
    backgroundExecutor.execute(() -> {
      try {
        SupabaseService.Instructor instructor = supabaseService.authenticateInstructor(email, password);
        if (instructor != null) {
          AuthStore.saveInstructor(this, instructor.id, instructor.name);
          runOnUiThread(this::goToMain);
        } else {
          runOnUiThread(() ->
            Snackbar.make(view, "פרטי התחברות שגויים", Snackbar.LENGTH_SHORT).show());
        }
      } catch (Exception ex) {
        runOnUiThread(() ->
          Snackbar.make(view, "שגיאה בהתחברות", Snackbar.LENGTH_SHORT).show());
      }
    });
  }

  private void goToMain() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }
}
