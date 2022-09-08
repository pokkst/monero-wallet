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
    private final MutableLiveData<Long> _lockedBalance = new MutableLiveData<>(0L);
    public LiveData<Long> lockedBalance = _lockedBalance;

    public BalanceService(MainActivity mainActivity, MoneroHandlerThread thread) {
        super(mainActivity, thread);
        instance = this;
    }

    public void refreshBalance() {
        _balance.postValue(getUnlockedBalanceRaw());
        _lockedBalance.postValue(getLockedBalanceRaw());
    }

    public long getUnlockedBalanceRaw() {
        return WalletManager.getInstance().getWallet().getUnlockedBalance();
    }

    public long getTotalBalanceRaw() {
        return WalletManager.getInstance().getWallet().getBalance();
    }

    public long getLockedBalanceRaw() {
        return getTotalBalanceRaw() - getUnlockedBalanceRaw();
    }
}
