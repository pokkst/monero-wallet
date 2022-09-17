package net.mynero.wallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.model.WalletManager;

import java.util.List;

public class HistoryService extends ServiceBase {
    public static HistoryService instance = null;
    private final MutableLiveData<List<TransactionInfo>> _history = new MutableLiveData<>();
    public LiveData<List<TransactionInfo>> history = _history;
    public HistoryService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public static HistoryService getInstance() {
        return instance;
    }

    public void refreshHistory() {
        _history.postValue(getHistory());
    }

    public List<TransactionInfo> getHistory() {
        return WalletManager.getInstance().getWallet().getHistory().getAll();
    }
}
