package com.scoutscentral.app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.auth.AuthStore;
import com.scoutscentral.app.model.data.SupabaseService;
import com.scoutscentral.app.view_model.LoginViewModel;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;
    private ProgressBar progressBar;
    
    @VisibleForTesting
    public static SupabaseService testSupabaseService;
    @VisibleForTesting
    public static Executor testExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (testSupabaseService != null) {
            if (testExecutor != null) {
                viewModel = new LoginViewModel(getApplication(), testSupabaseService, testExecutor);
            } else {
                viewModel = new LoginViewModel(getApplication(), testSupabaseService);
            }
        } else {
            viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        }

        EditText emailInput = findViewById(R.id.login_email);
        EditText passwordInput = findViewById(R.id.login_password);
        MaterialButton loginButton = findViewById(R.id.login_submit);
        progressBar = findViewById(R.id.login_progress);

        String savedEmail = AuthStore.getEmail(this);
        String savedPassword = AuthStore.getPassword(this);

        if (!savedEmail.isEmpty()) {
            emailInput.setText(savedEmail);
        }
        if (!savedPassword.isEmpty()) {
            passwordInput.setText(savedPassword);
        }

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(v, "אנא מלאו אימייל וסיסמה", Snackbar.LENGTH_SHORT).show();
                return;
            }
            // Logic moved to ViewModel to save only on success
            viewModel.login(email, password);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                goToMain();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
