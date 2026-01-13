package com.scoutscentral.app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.databinding.ActivityLoginBinding;
import com.scoutscentral.app.model.data.SupabaseService;
import com.scoutscentral.app.view_model.LoginViewModel;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @VisibleForTesting
    public static SupabaseService testSupabaseService;
    @VisibleForTesting
    public static Executor testExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel with test hooks if present
        if (testSupabaseService != null) {
            viewModel = new LoginViewModel(getApplication(), testSupabaseService, 
                    testExecutor != null ? testExecutor : Runnable::run);
        } else {
            viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        }

        binding.loginSubmit.setOnClickListener(v -> {
            String email = binding.loginEmail.getText().toString().trim();
            String password = binding.loginPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(v, "אנא מלאו אימייל וסיסמה", Snackbar.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.loginProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
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
