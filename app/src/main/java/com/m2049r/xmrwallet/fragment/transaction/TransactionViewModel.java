package com.m2049r.xmrwallet.fragment.transaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Transfer;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.HistoryService;

public class TransactionViewModel extends ViewModel {
    private final MutableLiveData<TransactionInfo> _transaction = new MutableLiveData<>(null);
    public LiveData<TransactionInfo> transaction = _transaction;
    private final MutableLiveData<String> _destination = new MutableLiveData<>(null);
    public LiveData<String> destination = _destination;

    public void init(TransactionInfo info) {
        Wallet wallet = WalletManager.getInstance().getWallet();
        if(info.txKey == null) {
            info.txKey = wallet.getTxKey(info.hash);
        }
        if(info.address == null && info.direction == TransactionInfo.Direction.Direction_In) {
            _destination.setValue(wallet.getSubaddress(info.accountIndex, info.addressIndex));
        } else if(info.address != null && info.direction == TransactionInfo.Direction.Direction_In) {
            _destination.setValue(info.address);
        } else if(info.transfers != null && info.direction == TransactionInfo.Direction.Direction_Out) {
            if(info.transfers.size() == 1) {
                _destination.setValue(info.transfers.get(0).address);
            }
        }
        this._transaction.setValue(info);
    }
}