/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet.service;

import static com.m2049r.xmrwallet.model.Wallet.SWEEP_ALL;

import com.m2049r.xmrwallet.data.DefaultNodes;
import com.m2049r.xmrwallet.data.Node;
import com.m2049r.xmrwallet.data.TxData;
import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletListener;
import com.m2049r.xmrwallet.model.WalletManager;


/**
 * Handy class for starting a new thread that has a looper. The looper can then be
 * used to create handler classes. Note that start() must still be called.
 * The started Thread has a stck size of STACK_SIZE (=5MB)
 */
public class MoneroHandlerThread extends Thread implements WalletListener {
    private Listener listener = null;
    private Wallet wallet = null;
    // from src/cryptonote_config.h
    static public final long THREAD_STACK_SIZE = 5 * 1024 * 1024;

    public MoneroHandlerThread(String name, Wallet wallet, Listener listener) {
        super(null, null, name, THREAD_STACK_SIZE);
        this.wallet = wallet;
        this.listener = listener;
    }

    @Override
    public synchronized void start() {
        super.start();
        this.listener.onRefresh();
    }

    @Override
    public void run() {
        WalletManager.getInstance().setDaemon(Node.fromString(DefaultNodes.XMRTW.getUri()));
        System.out.println(WalletManager.getInstance().getBlockchainHeight());
        System.out.println(wallet.getSeed(""));
        wallet.init(0);
        wallet.setListener(this);
        wallet.startRefresh();
    }

    @Override
    public void moneySpent(String txId, long amount) {
    }

    @Override
    public void moneyReceived(String txId, long amount) {
    }

    @Override
    public void unconfirmedMoneyReceived(String txId, long amount) {
    }

    @Override
    public void newBlock(long height) {
        if (height % 1000 == 0) {
            refresh();
        }
    }

    @Override
    public void updated() {
        refresh();
    }

    @Override
    public void refreshed() {
        wallet.setSynchronized();
        refresh();
    }

    private void refresh() {
        wallet.refreshHistory();
        wallet.store();
        listener.onRefresh();
    }

    public boolean sendTx(String address, String amountStr, boolean sendAll) {
        long amount = sendAll ? SWEEP_ALL : Wallet.getAmountFromString(amountStr);
        PendingTransaction pendingTx = wallet.createTransaction(new TxData(address, amount, 0, PendingTransaction.Priority.Priority_Default));
        return pendingTx.commit("", true);
    }

    public interface Listener {
        void onRefresh();
    }
}
