package com.m2049r.xmrwallet;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.AddressService;
import com.m2049r.xmrwallet.service.BalanceService;
import com.m2049r.xmrwallet.service.HistoryService;
import com.m2049r.xmrwallet.service.MoneroHandlerThread;
import com.m2049r.xmrwallet.service.TxService;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MoneroHandlerThread.Listener {
    private MoneroHandlerThread thread = null;
    private TxService txService = null;
    private BalanceService balanceService = null;
    private AddressService addressService = null;
    private HistoryService historyService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public MoneroHandlerThread getThread() {
        return thread;
    }

    private void init() {
        File walletFile = new File(getApplicationInfo().dataDir, "xmr_wallet");
        Wallet wallet = null;
        if (walletFile.exists()) {
            wallet = WalletManager.getInstance().openWallet(walletFile.getAbsolutePath(), "");
        } else {
            wallet = WalletManager.getInstance().createWallet(walletFile, "", "English", 0);
        }
        WalletManager.getInstance().setProxy("127.0.0.1:9050");
        thread = new MoneroHandlerThread("WalletService", wallet, this);
        thread.start();
        this.txService = new TxService(this, thread);
        this.balanceService = new BalanceService(this, thread);
        this.addressService = new AddressService(this, thread);
        this.historyService = new HistoryService(this, thread);
    }

    @Override
    public void onRefresh() {
        this.historyService.refreshHistory();
        this.balanceService.refreshBalance();
        this.addressService.refreshAddress();
    }
}