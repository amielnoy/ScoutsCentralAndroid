package com.scoutscentral.app.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthStore {
  private static final String PREFS = "scouts_auth";
  private static final String KEY_INSTRUCTOR_ID = "instructor_id";
  private static final String KEY_INSTRUCTOR_NAME = "instructor_name";

  private AuthStore() {}

  public static boolean isLoggedIn(Context context) {
    return getPrefs(context).contains(KEY_INSTRUCTOR_ID);
  }

  public static void saveInstructor(Context context, String id, String name) {
    getPrefs(context).edit()
      .putString(KEY_INSTRUCTOR_ID, id)
      .putString(KEY_INSTRUCTOR_NAME, name)
      .apply();
  }

  public static void clear(Context context) {
    getPrefs(context).edit().clear().apply();
  }

  public static String getInstructorName(Context context) {
    return getPrefs(context).getString(KEY_INSTRUCTOR_NAME, "");
  }

  private static SharedPreferences getPrefs(Context context) {
    return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
  }
}
