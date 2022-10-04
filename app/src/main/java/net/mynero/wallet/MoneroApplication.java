package net.mynero.wallet;

import android.app.Application;

import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.NightmodeHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoneroApplication extends Application {
    private ExecutorService executor = null;
    @Override
    public void onCreate() {
        super.onCreate();
        new PrefService(this);
        NightmodeHelper.getAndSetPreferredNightmode();
        executor = Executors.newFixedThreadPool(16);
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
