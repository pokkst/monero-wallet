package net.mynero.wallet.service;

import android.content.Context;
import android.content.SharedPreferences;

import net.mynero.wallet.MoneroApplication;

public class PrefService extends ServiceBase {
    public static SharedPreferences preferences = null;
    public static PrefService instance = null;

    public PrefService(MoneroApplication application) {
        super(null);
        preferences = application.getSharedPreferences(application.getApplicationInfo().packageName, Context.MODE_PRIVATE);
        instance = this;
    }

    public SharedPreferences.Editor edit() {
        return preferences.edit();
    }

    public String getString(String key, String defaultValue) {
        String value = preferences.getString(key, "");
        if(value.isEmpty() && !defaultValue.isEmpty()) {
            edit().putString(key, defaultValue).apply();
            return defaultValue;
        }
        return value;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        boolean value = preferences.getBoolean(key, false);
        if(!value && defaultValue) {
            edit().putBoolean(key, true).apply();
            return true;
        }
        return value;
    }

    public static PrefService getInstance() {
        return instance;
    }
}
