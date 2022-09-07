package com.m2049r.xmrwallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.model.WalletManager;

public class BalanceService extends ServiceBase {
    public static BalanceService instance = null;

    public static BalanceService getInstance() {
        return instance;
    }

    private final MutableLiveData<Long> _balance = new MutableLiveData<>(0L);
    public LiveData<Long> balance = _balance;

    public BalanceService(MainActivity mainActivity, MoneroHandlerThread thread) {
        super(mainActivity, thread);
        instance = this;
    }

    public void refreshBalance() {
        _balance.postValue(WalletManager.getInstance().getWallet().getBalance());
    }
}
