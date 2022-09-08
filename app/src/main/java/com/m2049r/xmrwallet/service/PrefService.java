package com.m2049r.xmrwallet.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.m2049r.xmrwallet.MainActivity;

public class PrefService extends ServiceBase {
    public static SharedPreferences instance = null;

    public static SharedPreferences getInstance() {
        return instance;
    }

    public PrefService(MainActivity mainActivity) {
        super(mainActivity, null);
        instance = mainActivity.getSharedPreferences(mainActivity.getApplicationInfo().packageName, Context.MODE_PRIVATE);
    }
}
