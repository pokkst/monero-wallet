package net.mynero.wallet.fragment.onboarding;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import net.mynero.wallet.MoneroApplication;
import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;

public class OnboardingViewModel extends ViewModel {
    private final MutableLiveData<Boolean> _showMoreOptions = new MutableLiveData<>(false);
    public LiveData<Boolean> showMoreOptions = _showMoreOptions;
    private String proxyAddress = "";
    private String proxyPort = "";

    public void onMoreOptionsClicked() {
        boolean currentValue = showMoreOptions.getValue() != null ? showMoreOptions.getValue() : false;
        boolean newValue = !currentValue;
        _showMoreOptions.setValue(newValue);
    }

    public void updateProxy(MoneroApplication application) {
        application.getExecutor().execute(() -> {
            boolean usesProxy = PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false);

            if (!usesProxy) {
                return;
            }

            if (proxyAddress.isEmpty()) proxyAddress = "127.0.0.1";
            if (proxyPort.isEmpty()) proxyPort = "9050";
            boolean validIpAddress = Patterns.IP_ADDRESS.matcher(proxyAddress).matches();

            if (validIpAddress) {
                String proxy = proxyAddress + ":" + proxyPort;
                PrefService.getInstance().edit().putString(Constants.PREF_PROXY, proxy).apply();
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