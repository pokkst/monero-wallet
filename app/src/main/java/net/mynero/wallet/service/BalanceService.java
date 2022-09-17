package net.mynero.wallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.mynero.wallet.model.WalletManager;

public class BalanceService extends ServiceBase {
    public static BalanceService instance = null;
    private final MutableLiveData<Long> _balance = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> _lockedBalance = new MutableLiveData<>(0L);
    public LiveData<Long> balance = _balance;
    public LiveData<Long> lockedBalance = _lockedBalance;
    public BalanceService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public static BalanceService getInstance() {
        return instance;
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
