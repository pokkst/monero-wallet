package net.mynero.wallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import net.mynero.wallet.fragment.dialog.PasswordBottomSheetDialog;
import net.mynero.wallet.fragment.dialog.SendBottomSheetDialog;
import net.mynero.wallet.livedata.SingleLiveEvent;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.AddressService;
import net.mynero.wallet.service.BalanceService;
import net.mynero.wallet.service.BlockchainService;
import net.mynero.wallet.service.HistoryService;
import net.mynero.wallet.service.MoneroHandlerThread;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.service.TxService;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.UriData;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MoneroHandlerThread.Listener, PasswordBottomSheetDialog.PasswordListener {
    public final SingleLiveEvent restartEvents = new SingleLiveEvent();
    private MoneroHandlerThread thread = null;
    private BalanceService balanceService = null;
    private AddressService addressService = null;
    private HistoryService historyService = null;
    private BlockchainService blockchainService = null;

    private boolean proceedToSend = false;
    private UriData uriData = null;

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

            Intent intent = getIntent();
            Uri uri = intent.getData();
            if(uri != null) {
                uriData = UriData.parse(uri.toString());
                if (uriData != null) {
                    proceedToSend = true;
                }
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

        if(proceedToSend) {
            SendBottomSheetDialog sendDialog = new SendBottomSheetDialog();
            sendDialog.uriData = uriData;
            sendDialog.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onPasswordFail() {
        runOnUiThread(() -> Toast.makeText(getApplication(), R.string.bad_password, Toast.LENGTH_SHORT).show());
    }
}