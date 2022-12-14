package net.mynero.wallet.service;

import net.mynero.wallet.model.PendingTransaction;

import java.util.ArrayList;

public class TxService extends ServiceBase {
    public static TxService instance = null;

    public TxService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public static TxService getInstance() {
        return instance;
    }

    public PendingTransaction createTx(String address, String amount, boolean sendAll, PendingTransaction.Priority feePriority, ArrayList<String> selectedUtxos) throws Exception {
        return this.getThread().createTx(address, amount, sendAll, feePriority, selectedUtxos);
    }

    public boolean sendTx(PendingTransaction pendingTransaction) {
        return this.getThread().sendTx(pendingTransaction);
    }
}
