package com.khorshed.mybank.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "MyBankPreferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_LAST_EMAIL = "last_email";

    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setDarkMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkMode() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setRememberMe(boolean remember) {
        preferences.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();
    }

    public boolean isRememberMe() {
        return preferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void setLastEmail(String email) {
        preferences.edit().putString(KEY_LAST_EMAIL, email).apply();
    }

    public String getLastEmail() {
        return preferences.getString(KEY_LAST_EMAIL, "");
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
