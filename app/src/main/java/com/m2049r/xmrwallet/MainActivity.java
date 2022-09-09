package com.m2049r.xmrwallet;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import com.m2049r.xmrwallet.fragment.dialog.PasswordBottomSheetDialog;
import com.m2049r.xmrwallet.fragment.dialog.SendBottomSheetDialog;
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
import com.m2049r.xmrwallet.util.NightmodeHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MoneroHandlerThread.Listener, PasswordBottomSheetDialog.PasswordListener {
    public final SingleLiveEvent restartEvents = new SingleLiveEvent();
    private MoneroHandlerThread thread = null;
    private TxService txService = null;
    private BalanceService balanceService = null;
    private AddressService addressService = null;
    private HistoryService historyService = null;
    private BlockchainService blockchainService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NightmodeHelper.getAndSetPreferredNightmode(this);
        File walletFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME);
        new PrefService(this);

        if(walletFile.exists()) {
            boolean promptPassword = PrefService.getInstance().getBoolean(Constants.PREF_USES_PASSWORD, false);
            if(!promptPassword) {
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
        this.txService = new TxService(this, thread);
        this.balanceService = new BalanceService(this, thread);
        this.addressService = new AddressService(this, thread);
        this.historyService = new HistoryService(this, thread);
        this.blockchainService = new BlockchainService(this, thread);
        thread.start();
    }

    @Override
    public void onRefresh() {
        this.historyService.refreshHistory();
        this.balanceService.refreshBalance();
        this.blockchainService.refreshBlockchain();
    }

    @Override
    public void onConnectionFail() {
        System.out.println("CONNECT FAILED");
    }

    @Override
    public void onPasswordSuccess(String password) {
        File walletFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME);
        init(walletFile, password);
        restartEvents.call();
    }

    @Override
    public void onPasswordFail() {
        Toast.makeText(this, R.string.bad_password, Toast.LENGTH_SHORT).show();
    }
}