package com.m2049r.xmrwallet.fragment.settings;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Patterns;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.PrefService;
import com.m2049r.xmrwallet.service.TxService;
import com.m2049r.xmrwallet.util.Constants;

public class SettingsViewModel extends ViewModel {

    private String proxyAddress = "";
    private String proxyPort = "";
    public void updateProxy() {
        AsyncTask.execute(() -> {
            boolean usesProxy = PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false);
            if(!usesProxy) {
                WalletManager.getInstance().setProxy("");
                WalletManager.getInstance().getWallet().setProxy("");
                return;
            }

            if(proxyAddress.isEmpty()) proxyAddress = "127.0.0.1";
            if(proxyPort.isEmpty()) proxyPort = "9050";
            boolean validIpAddress = Patterns.IP_ADDRESS.matcher(proxyAddress).matches();
            if(validIpAddress) {
                String proxy = proxyAddress + ":" + proxyPort;
                PrefService.getInstance().edit().putString(Constants.PREF_PROXY, proxy).apply();
                WalletManager.getInstance().setProxy(proxy);
                WalletManager.getInstance().getWallet().setProxy(proxy);
            }
        });
    }

    public void setProxyAddress(String address) {
        this.proxyAddress = address;
    }

    public void setProxyPort(String port) {
        this.proxyPort = port;
    }
}