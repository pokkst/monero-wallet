package net.mynero.wallet.fragment.settings;

import android.util.Patterns;

import androidx.lifecycle.ViewModel;

import net.mynero.wallet.MoneroApplication;
import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsViewModel extends ViewModel {

    private String proxyAddress = "";
    private String proxyPort = "";

    public void updateProxy(MoneroApplication application) {
        application.getExecutor().execute(() -> {
            boolean usesProxy = PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false);
            String currentNodeString = PrefService.getInstance().getString(Constants.PREF_NODE, DefaultNodes.XMRTW.getAddress());
            boolean isNodeLocalIp = currentNodeString.startsWith("10.") || currentNodeString.startsWith("192.168.") || currentNodeString.equals("localhost") || currentNodeString.equals("127.0.0.1");

            if (!usesProxy || isNodeLocalIp) {
                WalletManager.getInstance().setProxy("");
                WalletManager.getInstance().getWallet().setProxy("");
                return;
            }

            if (proxyAddress.isEmpty()) proxyAddress = "127.0.0.1";
            if (proxyPort.isEmpty()) proxyPort = "9050";
            boolean validIpAddress = Patterns.IP_ADDRESS.matcher(proxyAddress).matches();

            if (validIpAddress) {
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