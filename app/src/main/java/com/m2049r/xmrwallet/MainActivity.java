package com.m2049r.xmrwallet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.MoneroHandlerThread;
import com.m2049r.xmrwallet.service.TxService;
import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoneroHandlerThread.Listener {
    private final MutableLiveData<String> _address = new MutableLiveData<>("");
    public LiveData<String> address = _address;
    private final MutableLiveData<Long> _balance = new MutableLiveData<>(0L);
    public LiveData<Long> balance = _balance;
    private final MutableLiveData<List<TransactionInfo>> _history = new MutableLiveData<>();
    public LiveData<List<TransactionInfo>> history = _history;

    private MoneroHandlerThread thread = null;
    private TxService txService = null;

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
        if(walletFile.exists()) {
            wallet = WalletManager.getInstance().openWallet(walletFile.getAbsolutePath(), "");
        } else {
            wallet = WalletManager.getInstance().createWallet(walletFile, "", "English", 0);
        }
        WalletManager.getInstance().setProxy("127.0.0.1:9050");
        thread = new MoneroHandlerThread("WalletService", wallet, this);
        thread.start();
        this.txService = new TxService(this, thread);
    }

    @Override
    public void onRefresh() {
        WalletManager walletManager = WalletManager.getInstance();
        Wallet wallet = walletManager.getWallet();
        if(wallet != null) {
            String address = wallet.getLastSubaddress(0);
            _history.postValue(wallet.getHistory().getAll());
            _balance.postValue(wallet.getBalance());
            _address.postValue(address);
        }
    }
}