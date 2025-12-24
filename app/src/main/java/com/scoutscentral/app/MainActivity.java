package com.scoutscentral.app;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.scoutscentral.app.ui.ActivitiesFragment;
import com.scoutscentral.app.ui.CommunicationFragment;
import com.scoutscentral.app.ui.DashboardFragment;
import com.scoutscentral.app.ui.MembersFragment;
import com.scoutscentral.app.ui.ProgressFragment;
import com.scoutscentral.app.ui.ReportsFragment;
import com.scoutscentral.app.ui.SettingsFragment;
import com.scoutscentral.app.auth.AuthStore;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private MaterialToolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!AuthStore.isLoggedIn(this)) {
      startActivity(new android.content.Intent(this, LoginActivity.class));
      finish();
      return;
    }
    setContentView(R.layout.activity_main);

    drawerLayout = findViewById(R.id.drawer_layout);
    navigationView = findViewById(R.id.navigation_view);
    toolbar = findViewById(R.id.top_app_bar);

    setSupportActionBar(toolbar);

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);

    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
          drawerLayout.closeDrawer(GravityCompat.START);
        } else {
          setEnabled(false);
          getOnBackPressedDispatcher().onBackPressed();
        }
      }
    });

    if (savedInstanceState == null) {
      navigationView.setCheckedItem(R.id.nav_dashboard);
      switchFragment(new DashboardFragment(), getString(R.string.nav_dashboard));
    }
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    Fragment fragment = null;
    String title = getString(R.string.app_name);

    int id = item.getItemId();
    if (id == R.id.nav_dashboard) {
      fragment = new DashboardFragment();
      title = getString(R.string.nav_dashboard);
    } else if (id == R.id.nav_members) {
      fragment = new MembersFragment();
      title = getString(R.string.nav_members);
    } else if (id == R.id.nav_activities) {
      fragment = new ActivitiesFragment();
      title = getString(R.string.nav_activities);
    } else if (id == R.id.nav_progress) {
      fragment = new ProgressFragment();
      title = getString(R.string.nav_progress);
    } else if (id == R.id.nav_communication) {
      fragment = new CommunicationFragment();
      title = getString(R.string.nav_communication);
    } else if (id == R.id.nav_reports) {
      fragment = new ReportsFragment();
      title = getString(R.string.nav_reports);
    } else if (id == R.id.nav_settings) {
      fragment = new SettingsFragment();
      title = getString(R.string.nav_settings);
    } else if (id == R.id.nav_logout) {
      new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("התנתקות")
        .setMessage("לצאת מהאפליקציה?")
        .setPositiveButton("התנתק", (dialog, which) -> {
          AuthStore.clear(this);
          startActivity(new android.content.Intent(this, LoginActivity.class));
          finish();
        })
        .setNegativeButton("ביטול", null)
        .show();
      drawerLayout.closeDrawer(GravityCompat.START);
      return true;
    } else {
      drawerLayout.closeDrawer(GravityCompat.START);
      return true;
    }

    switchFragment(fragment, title);
    drawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

  private void switchFragment(Fragment fragment, String title) {
    toolbar.setTitle(title);
    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .commit();
  }

}
