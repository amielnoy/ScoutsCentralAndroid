package com.scoutscentral.app.model.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AuthStore {
  private static final String TAG = "AuthStore";
  private static final String PREFS = "scouts_auth";
  private static final String KEY_INSTRUCTOR_ID = "instructor_id";
  private static final String KEY_INSTRUCTOR_NAME = "instructor_name";

  private AuthStore() {}

  public static boolean isLoggedIn(Context context) {
    boolean loggedIn = getPrefs(context).contains(KEY_INSTRUCTOR_ID);
    Log.d(TAG, "isLoggedIn: " + loggedIn);
    return loggedIn;
  }

  public static void saveInstructor(Context context, String id, String name) {
    Log.d(TAG, "Saving instructor: " + name + " (ID: " + id + ")");
    boolean success = getPrefs(context).edit()
      .putString(KEY_INSTRUCTOR_ID, id)
      .putString(KEY_INSTRUCTOR_NAME, name)
      .commit(); // Use commit for immediate write
    Log.d(TAG, "Save success: " + success);
  }

  public static void clear(Context context) {
    getPrefs(context).edit().clear().commit();
  }

  public static String getInstructorName(Context context) {
    return getPrefs(context).getString(KEY_INSTRUCTOR_NAME, "");
  }

  private static SharedPreferences getPrefs(Context context) {
    return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
  }
}
