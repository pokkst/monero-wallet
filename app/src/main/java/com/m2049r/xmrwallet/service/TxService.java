package com.m2049r.xmrwallet.service;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.livedata.SingleLiveEvent;

public class TxService extends ServiceBase {
    public static TxService instance = null;

    public static TxService getInstance() {
        return instance;
    }

    public TxService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public boolean sendTx(String address, String amount, boolean sendAll) {
        return this.getThread().sendTx(address, amount, sendAll);
    }
}
