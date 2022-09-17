package net.mynero.wallet.service;

import android.content.Context;
import android.content.SharedPreferences;

import net.mynero.wallet.MoneroApplication;

public class PrefService extends ServiceBase {
    public static SharedPreferences instance = null;

    public PrefService(MoneroApplication application) {
        super(null);
        instance = application.getSharedPreferences(application.getApplicationInfo().packageName, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getInstance() {
        return instance;
    }
}
