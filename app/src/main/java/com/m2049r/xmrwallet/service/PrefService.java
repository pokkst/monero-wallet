package com.m2049r.xmrwallet.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.MoneroApplication;

public class PrefService extends ServiceBase {
    public static SharedPreferences instance = null;

    public static SharedPreferences getInstance() {
        return instance;
    }

    public PrefService(MoneroApplication application) {
        super(null);
        instance = application.getSharedPreferences(application.getApplicationInfo().packageName, Context.MODE_PRIVATE);
    }
}
