package com.m2049r.xmrwallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.WalletManager;

import java.util.Collections;
import java.util.List;

public class HistoryService extends ServiceBase {
    public static HistoryService instance = null;

    public static HistoryService getInstance() {
        return instance;
    }

    private final MutableLiveData<List<TransactionInfo>> _history = new MutableLiveData<>();
    public LiveData<List<TransactionInfo>> history = _history;

    public HistoryService(MainActivity mainActivity, MoneroHandlerThread thread) {
        super(mainActivity, thread);
        instance = this;
    }

    public void refreshHistory() {
        _history.postValue(getHistory());
    }

    public List<TransactionInfo> getHistory() {
        return WalletManager.getInstance().getWallet().getHistory().getAll();
    }
}
