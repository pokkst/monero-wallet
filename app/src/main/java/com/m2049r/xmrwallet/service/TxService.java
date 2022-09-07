package com.m2049r.xmrwallet.service;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.livedata.SingleLiveEvent;

public class TxService extends ServiceBase {
    public static TxService instance = null;
    public static TxService getInstance() {
        return instance;
    }
    
    private final SingleLiveEvent _clearSendEvent = new SingleLiveEvent();
    public SingleLiveEvent clearSendEvent = _clearSendEvent;

    public TxService(MainActivity mainActivity, MoneroHandlerThread thread) {
        super(mainActivity, thread);
        instance = this;
    }

    public void sendTx(String address, String amount) {
        boolean success = this.getThread().sendTx(address, amount);
        if(success) {
            _clearSendEvent.call();
        }
    }
}
