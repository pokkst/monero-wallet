package com.m2049r.xmrwallet;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import com.m2049r.xmrwallet.fragment.dialog.PasswordBottomSheetDialog;
import com.m2049r.xmrwallet.livedata.SingleLiveEvent;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.AddressService;
import com.m2049r.xmrwallet.service.BalanceService;
import com.m2049r.xmrwallet.service.BlockchainService;
import com.m2049r.xmrwallet.service.HistoryService;
import com.m2049r.xmrwallet.service.MoneroHandlerThread;
import com.m2049r.xmrwallet.service.PrefService;
import com.m2049r.xmrwallet.service.TxService;
import com.m2049r.xmrwallet.util.Constants;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MoneroHandlerThread.Listener, PasswordBottomSheetDialog.PasswordListener {
    public final SingleLiveEvent restartEvents = new SingleLiveEvent();
    private MoneroHandlerThread thread = null;
    private BalanceService balanceService = null;
    private AddressService addressService = null;
    private HistoryService historyService = null;
    private BlockchainService blockchainService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File walletFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME);
        File walletKeysFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME + ".keys");
        if (walletKeysFile.exists()) {
            boolean promptPassword = PrefService.getInstance().getBoolean(Constants.PREF_USES_PASSWORD, false);
            if (!promptPassword) {
                init(walletFile, "");
            } else {
                PasswordBottomSheetDialog passwordDialog = new PasswordBottomSheetDialog();
                passwordDialog.listener = this;
                passwordDialog.show(getSupportFragmentManager(), "password_dialog");
            }
        } else {
            navigate(R.id.onboarding_fragment);
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    private void navigate(int destination) {
        FragmentActivity activity = this;
        FragmentManager fm = activity.getSupportFragmentManager();
        NavHostFragment navHostFragment =
                (NavHostFragment) fm.findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navHostFragment.getNavController().navigate(destination);
        }
    }

    public MoneroHandlerThread getThread() {
        return thread;
    }

    public void init(File walletFile, String password) {
        Wallet wallet = WalletManager.getInstance().openWallet(walletFile.getAbsolutePath(), password);
        thread = new MoneroHandlerThread("WalletService", this, wallet);
        new TxService(thread);
        this.balanceService = new BalanceService(thread);
        this.addressService = new AddressService(thread);
        this.historyService = new HistoryService(thread);
        this.blockchainService = new BlockchainService(thread);
        thread.start();
    }

    @Override
    public void onRefresh() {
        this.historyService.refreshHistory();
        this.balanceService.refreshBalance();
        this.blockchainService.refreshBlockchain();
        this.addressService.refreshAddresses();
    }

    @Override
    public void onConnectionFail() {
        runOnUiThread(() -> Toast.makeText(getApplication(), R.string.connection_failed, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onPasswordSuccess(String password) {
        File walletFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME);
        init(walletFile, password);
        restartEvents.call();
    }

    @Override
    public void onPasswordFail() {
        runOnUiThread(() -> Toast.makeText(getApplication(), R.string.bad_password, Toast.LENGTH_SHORT).show());
    }
}