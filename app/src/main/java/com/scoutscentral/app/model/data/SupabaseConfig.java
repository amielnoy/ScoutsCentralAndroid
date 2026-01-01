package com.scoutscentral.app.model.data;

import com.scoutscentral.app.BuildConfig;

public final class SupabaseConfig {
  public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
  public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

  private SupabaseConfig() {}
}
