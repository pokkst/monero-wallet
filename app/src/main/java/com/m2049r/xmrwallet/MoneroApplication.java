package com.m2049r.xmrwallet;

import android.app.Application;

import com.m2049r.xmrwallet.service.PrefService;
import com.m2049r.xmrwallet.util.NightmodeHelper;

public class MoneroApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new PrefService(this);
        NightmodeHelper.getAndSetPreferredNightmode();
    }
}
