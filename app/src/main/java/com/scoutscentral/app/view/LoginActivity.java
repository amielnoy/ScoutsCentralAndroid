package com.scoutscentral.app.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.databinding.ActivityLoginBinding;
import com.scoutscentral.app.model.auth.AuthStore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Pre-fill the fields with saved credentials
        String savedEmail = AuthStore.getEmail(this);
        String savedPassword = AuthStore.getPassword(this);

        if (!savedEmail.isEmpty()) {
            binding.loginEmail.setText(savedEmail);
        }
        if (!savedPassword.isEmpty()) {
            binding.loginPassword.setText(savedPassword);
        }

        // Set the click listener for the login button
        binding.loginSubmit.setOnClickListener(v -> {
            String email = binding.loginEmail.getText().toString().trim();
            String password = binding.loginPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(v, "אנא מלאו אימייל וסיסמה", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // --- THIS IS THE LOCAL-ONLY LOGIN LOGIC ---
            // WARNING: This is an insecure login method.
            // It only checks against what's saved on the device.
            String storedEmail = AuthStore.getEmail(this);
            String storedPassword = AuthStore.getPassword(this);

            if (email.equals(storedEmail) && password.equals(storedPassword) && !storedEmail.isEmpty()) {
                // If credentials match, go to the main activity
                goToMain();
            } else {
                // If they don't match, show an error
                Snackbar.make(v, "פרטי התחברות שגויים", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
