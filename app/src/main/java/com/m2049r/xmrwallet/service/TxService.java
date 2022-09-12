package com.m2049r.xmrwallet.service;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.livedata.SingleLiveEvent;
import com.m2049r.xmrwallet.model.PendingTransaction;

public class TxService extends ServiceBase {
    public static TxService instance = null;

    public static TxService getInstance() {
        return instance;
    }

    public TxService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public PendingTransaction createTx(String address, String amount, boolean sendAll) {
        return this.getThread().createTx(address, amount, sendAll);
    }

    public boolean sendTx(PendingTransaction pendingTransaction) {
        return this.getThread().sendTx(pendingTransaction);
    }
}
