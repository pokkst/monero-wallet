package net.mynero.wallet;

import android.app.Application;

import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.NightmodeHelper;

public class MoneroApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new PrefService(this);
        NightmodeHelper.getAndSetPreferredNightmode();
    }
}
