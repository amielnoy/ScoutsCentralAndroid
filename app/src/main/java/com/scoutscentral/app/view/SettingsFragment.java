package com.scoutscentral.app.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.scoutscentral.app.R;
import com.scoutscentral.app.model.data.Dal;

public class SettingsFragment extends Fragment {
  private final Dal repository = Dal.getInstance();

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_settings, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    MaterialButton syncNow = view.findViewById(R.id.settings_sync_now);
    MaterialButton clearLocal = view.findViewById(R.id.settings_clear_local);

    syncNow.setOnClickListener(v -> {
      repository.refreshFromSupabase();
      Snackbar.make(view, "סנכרון נשלח", Snackbar.LENGTH_SHORT).show();
    });

    clearLocal.setOnClickListener(v -> {
      repository.clearLocalData();
      Snackbar.make(view, "הנתונים המקומיים אופסו", Snackbar.LENGTH_SHORT).show();
    });
  }
}
