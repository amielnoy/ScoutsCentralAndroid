package com.scoutscentral.app.view_model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.scoutscentral.app.model.auth.AuthStore;
import com.scoutscentral.app.model.data.SupabaseService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginViewModel extends AndroidViewModel {
    private final SupabaseService supabaseService;
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.supabaseService = new SupabaseService();
    }

    // For testing
    public LoginViewModel(@NonNull Application application, SupabaseService supabaseService) {
        super(application);
        this.supabaseService = supabaseService;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public void login(String email, String password) {
        if (!supabaseService.isConfigured()) {
            errorMessage.setValue("Supabase לא מוגדר");
            return;
        }

        isLoading.setValue(true);
        backgroundExecutor.execute(() -> {
            try {
                SupabaseService.Instructor instructor = supabaseService.authenticateInstructor(email, password);
                if (instructor != null) {
                    AuthStore.saveInstructor(getApplication(), instructor.id, instructor.name);
                    loginSuccess.postValue(true);
                } else {
                    errorMessage.postValue("פרטי התחברות שגויים");
                }
            } catch (Exception ex) {
                errorMessage.postValue("שגיאה בהתחברות");
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}
